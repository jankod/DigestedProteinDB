package hr.pbf.digestdb.util;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.extern.slf4j.Slf4j;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class BinaryPeptideDb {

    private double[] masses;
    private long[] offsets;
    private TIntObjectHashMap<String> accessionMap;
    private final File sequenceFile;


    public BinaryPeptideDb(String accessionMapCsv, String binaryFilePath, String massesOffsetsFilePath) {
        sequenceFile = new File(binaryFilePath);
        //  loadAccessionMap(accessionMapCsv);
        // loadMassesAndOffsets(massesOffsetsFilePath);

    }


    /**
     * Read csv file accNum, accession and load it into map.
     *
     * @param accessionMapCsvPath Path to csv file with accNum, accession
     * @return Map with accNum as key and accession as value
     */
    public static TIntObjectHashMap<String> loadAccessionMapFromCsv(String accessionMapCsvPath) {
        TIntObjectHashMap<String> map = new TIntObjectHashMap<>(70_000);
        try (BufferedReader reader = new BufferedReader(new FileReader(accessionMapCsvPath))) {
            reader.readLine(); // Preskoči header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                int id = Integer.parseInt(parts[0]);
                String accession = parts[1];
                map.put(id, accession);
            }
        } catch (IOException e) {
            log.error("Error reading accession map", e);
        }
        return map;
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
            log.error("Error reading masses and offsets", e);
        }
        log.debug("Loaded " + masses.length + " masses and offsets.");
    }


    public void buildDatabase(String groupedCsv, String binaryDbPath, String binaryMassesOffsetsPath) {
        //  TIntObjectHashMap<String> accessionMap = new TIntObjectHashMap<>();
        TDoubleList massList = new TDoubleArrayList();
        TLongList offsetList = new TLongArrayList();

        // Izgradi binarnu datoteku s kompresijom
        try (BufferedReader reader = new BufferedReader(new FileReader(groupedCsv));
             RandomAccessFile raf = new RandomAccessFile(binaryDbPath, "rw")) {
            long currentOffset = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                // 503.234,SGAGAAA:15-SAAGGAA:14-TGAAAGG:16
                String[] parts = line.split(",", 2);
                if (parts.length < 2) continue;

                double mass = Double.parseDouble(parts[0]);
                String seqAccs = parts[1];

                massList.add(mass);
                offsetList.add(currentOffset);

                byte[] seqAccBytes = seqAccs.getBytes(StandardCharsets.UTF_8);

                raf.writeShort(seqAccBytes.length);
                raf.write(seqAccBytes);
                currentOffset = raf.getFilePointer();
            }
        } catch (IOException e) {
            log.error("Error building database", e);
        }

        // Spremi masses i offsets u zasebnu datoteku
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(binaryMassesOffsetsPath))) {
            dos.writeInt(massList.size()); // Broj zapisa
            for (int i = 0; i < massList.size(); i++) {
                dos.writeDouble(massList.get(i)); // Masa
                dos.writeLong(offsetList.get(i)); // Offset
            }
        } catch (IOException e) {
            log.debug("Error writing masses and offsets", e);
        }

        log.debug("Built database with " + massList.size() + " entries.");
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

        try (RandomAccessFile raf = new RandomAccessFile(sequenceFile, "r")) {
            for (int i = lower; i <= upper; i++) {
                raf.seek(offsets[i]);
                int compressedLength = raf.readInt();
                byte[] compressed = new byte[compressedLength];
                raf.readFully(compressed);

                String sequences = new String(compressed, StandardCharsets.UTF_8);

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
            log.error("Error reading sequence file", e);
        }
        return results;
    }

    public static void main(String[] args) {
        String DIR = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_human";
        String groupedCsv = DIR + "/grouped_with_ids.csv";
        String accessionMapCsv = DIR + "/accession_map.csv";
        String binaryFilePath = DIR + "/sequences.bin";
        String massesOffsetsFilePath = DIR + "/masses_offsets.bin";

        BinaryPeptideDb db = new BinaryPeptideDb(groupedCsv, accessionMapCsv, binaryFilePath);
        db.buildDatabase(groupedCsv, binaryFilePath, massesOffsetsFilePath);
        // Primjer range upita
        double massFrom = 447.23;
        double margin = 0.76;
        db.loadAccessionMapFromCsv(accessionMapCsv);
        db.loadMassesAndOffsets(massesOffsetsFilePath);
        List<String> results = db.searchWithMargin(massFrom, margin);

        System.out.println("Range query results (" + massFrom + " margin " + margin + "):");
        for (String result : results) {
            System.out.println(result);
        }
    }
}
