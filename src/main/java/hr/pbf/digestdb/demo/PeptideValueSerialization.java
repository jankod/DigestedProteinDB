package hr.pbf.digestdb.demo;

import hr.pbf.digestdb.exception.UnknownAminoacidException;
import hr.pbf.digestdb.util.AminoAcid5bitCoder;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class PeptideValueSerialization {

    public static byte[] toByteArray(Map<String, Set<Long>> map) throws IOException, UnknownAminoacidException {
        try (FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {

            dos.writeInt(map.size());

            for (Map.Entry<String, Set<Long>> entry : map.entrySet()) {
                String peptide = entry.getKey();
                Set<Long> positions = entry.getValue();

                byte[] peptideByteBuffer = AminoAcid5bitCoder.encodePeptide(peptide);
                dos.writeInt(peptideByteBuffer.length);
                //  dos.write(peptide.getBytes(StandardCharsets.US_ASCII));
                dos.write(peptideByteBuffer);

                // Zapis broja pozicija
                dos.writeInt(positions.size());

                // Zapis pozicija
                for (long position : positions) {
                    dos.writeLong(position);
                }
            }

            return bos.array;

        }
    }

    public static Map<String, Set<Long>> fromByteArray(byte[] bytes) throws IOException {

        try (FastByteArrayInputStream bis = new FastByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(bis)) {

            Map<String, Set<Long>> map = new HashMap<>();

            int mapSize = dis.readInt();

            for (int i = 0; i < mapSize; i++) {
                int peptideLength = dis.readInt();
                // String peptide = new String(dis.readNBytes(peptideLength), 0, peptideLength, StandardCharsets.US_ASCII);
                String peptide = AminoAcid5bitCoder.decodePeptide(dis.readNBytes(peptideLength), peptideLength);

                int positionsSize = dis.readInt();
                Set<Long> positions = new HashSet<>();

                for (int j = 0; j < positionsSize; j++) {
                    positions.add(dis.readLong());
                }

                map.put(peptide, positions);
            }

            return map;

        }
    }


    public static void main(String[] args) throws IOException, UnknownAminoacidException {
        Map<String, Set<Long>> currentPeptides = new HashMap<>();
        currentPeptides.put("IGGAA", new HashSet<>(Arrays.asList(1L, 2L, 3L)));
        currentPeptides.put("LLENAPGGTYFITENMTNELIMIAKDPVDK", new HashSet<>(Arrays.asList(4L, 5L)));
        currentPeptides.put("INQHYYINIYMYLMR", new HashSet<>(Arrays.asList(4234L, 523423L)));

        byte[] byteArray = toByteArray(currentPeptides);
        System.out.println("Veličina niza bajtova: " + byteArray.length);

        Map<String, Set<Long>> restoredMap = fromByteArray(byteArray);
        System.out.println(restoredMap);


    }


    private static int calculateByteSize(Map<String, Set<Long>> map) {
        int totalSize = Integer.BYTES; // For the map size
        for (Map.Entry<String, Set<Long>> entry : map.entrySet()) {
            String peptide = entry.getKey();
            Set<Long> positions = entry.getValue();

            totalSize += Integer.BYTES; // For peptide length
            totalSize += peptide.getBytes(StandardCharsets.US_ASCII).length; // For peptide bytes
            totalSize += Integer.BYTES; // For positions size
            totalSize += positions.size() * Long.BYTES; // For positions
        }
        return totalSize;
    }
}
