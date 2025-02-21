package hr.pbf.digestdb.util;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class BinaryPeptideDatabase {

    private double[] masses;
    private long[] offsets;
    private TIntObjectHashMap<String> accessionMap;
    private File sequenceFile;


    public BinaryPeptideDatabase(String accessionMapCsv, String binaryFilePath, String massesOffsetsFilePath) {
        sequenceFile = new File(binaryFilePath);
        //  loadAccessionMap(accessionMapCsv);
        // loadMassesAndOffsets(massesOffsetsFilePath);

    }


    private void loadAccessionMap(String accessionMapCsv) {
        accessionMap = new TIntObjectHashMap<>(70_000);
        try (BufferedReader reader = new BufferedReader(new FileReader(accessionMapCsv))) {
            reader.readLine(); // Preskoči header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                int id = Integer.parseInt(parts[0]);
                String accession = parts[1];
                accessionMap.put(id, accession);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Loaded " + accessionMap.size() + " accession mappings.");
    }

    private void loadMassesAndOffsets(String massesOffsetsFilePath) {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(massesOffsetsFilePath))) {
            int size = dis.readInt();
            masses = new double[size];
            offsets = new long[size];
            for (int i = 0; i < size; i++) {
                masses[i] = dis.readDouble();
                offsets[i] = dis.readLong();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Loaded " + masses.length + " masses and offsets.");
    }


    public void buildDatabase(String groupedCsv, String accessionMapCsv, String binaryFilePath, String massesOffsetsFilePath) {
        //  TIntObjectHashMap<String> accessionMap = new TIntObjectHashMap<>();
        List<Double> massList = new ArrayList<>();
        List<Long> offsetList = new ArrayList<>();

        loadAccessionMap(accessionMapCsv);
        System.out.println("Loaded " + accessionMap.size() + " accession mappings.");

        // Inicijalizacija LZ4 kompresora
        LZ4Factory factory = LZ4Factory.nativeInstance();
        LZ4Compressor compressor = factory.highCompressor();

        // Izgradi binarnu datoteku s kompresijom
        try (BufferedReader reader = new BufferedReader(new FileReader(groupedCsv));
             RandomAccessFile raf = new RandomAccessFile(binaryFilePath, "rw")) {
            long currentOffset = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length < 2) continue;

                double mass = Double.parseDouble(parts[0]);
                String sequences = parts[1];

                massList.add(mass);
                offsetList.add(currentOffset);

                byte[] seqBytes = sequences.getBytes(StandardCharsets.UTF_8);
                int maxCompressedLength = compressor.maxCompressedLength(seqBytes.length);
                byte[] compressed = new byte[maxCompressedLength];
                int compressedLength = compressor.compress(seqBytes, 0, seqBytes.length, compressed, 0, maxCompressedLength);

                raf.writeInt(compressedLength);
                raf.write(compressed, 0, compressedLength);
                currentOffset = raf.getFilePointer();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Spremi masses i offsets u zasebnu datoteku
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(massesOffsetsFilePath))) {
            dos.writeInt(massList.size()); // Broj zapisa
            for (int i = 0; i < massList.size(); i++) {
                dos.writeDouble(massList.get(i)); // Masa
                dos.writeLong(offsetList.get(i)); // Offset
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Built database with " + massList.size() + " entries.");
    }


    public List<String> searchWithMargin(double targetMass, double margin) {
        double massFrom = targetMass - margin;
        double massTo = targetMass + margin;
        return rangeQuery(massFrom, massTo);
    }

    private int findLowerBound(double mass) {
        int left = 0;

        int right = masses.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (masses[mid] < mass) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return left;
    }

    private int findUpperBound(double mass) {
        int left = 0;
        int right = masses.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (masses[mid] > mass) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return right;
    }

    public List<String> rangeQuery(double massFrom, double massTo) {
        int lower = findLowerBound(massFrom);
        int upper = findUpperBound(massTo);

        if (lower > upper || lower >= masses.length || upper < 0) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        LZ4Factory factory = LZ4Factory.fastestInstance();
        LZ4FastDecompressor decompressor = factory.fastDecompressor();

        try (RandomAccessFile raf = new RandomAccessFile(sequenceFile, "r")) {
            for (int i = lower; i <= upper; i++) {
                raf.seek(offsets[i]);
                int compressedLength = raf.readInt();
                byte[] compressed = new byte[compressedLength];
                raf.readFully(compressed);

                int maxDecompressedLength = 1024 * 1024; // Prilagodi prema potrebi
                byte[] restored = new byte[maxDecompressedLength];
                int decompressedLength = decompressor.decompress(compressed, 0, restored, 0, maxDecompressedLength);
                String sequences = new String(restored, 0, decompressedLength, "UTF-8");

                StringBuilder translated = new StringBuilder();
                translated.append("Mass: ").append(masses[i]).append(", ");
                String[] seqParts = sequences.split("-");
                for (String part : seqParts) {
                    String[] seqAndIds = part.split(":");
                    translated.append(seqAndIds[0]).append(":");
                    String[] ids = seqAndIds[1].split(";");
                    for (int j = 0; j < ids.length; j++) {
                        int id = Integer.parseInt(ids[j]);
                        translated.append(accessionMap.get(id));
                        if (j < ids.length - 1) translated.append(";");
                    }
                    translated.append("-");
                }
                translated.setLength(translated.length() - 1);
                results.add(translated.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static void main(String[] args) {
        String groupedCsv = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated/grouped_with_ids.csv";
        String accessionMapCsv = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated/accession_map.csv";
        String binaryFilePath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated/sequences.bin";
        String massesOffsetsFilePath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated/masses_offsets.bin";

        BinaryPeptideDatabase db = new BinaryPeptideDatabase(groupedCsv, accessionMapCsv, binaryFilePath);
        db.buildDatabase(groupedCsv, accessionMapCsv, binaryFilePath, massesOffsetsFilePath);
        // Primjer range upita
        double massFrom = 447.23;
        double margin = 0.76;
        db.loadAccessionMap(accessionMapCsv);
        db.loadMassesAndOffsets(massesOffsetsFilePath);
        List<String> results = db.searchWithMargin(massFrom, margin);

        System.out.println("Range query results (" + massFrom + " margin " + margin + "):");
        for (String result : results) {
            System.out.println(result);
        }
    }
}
