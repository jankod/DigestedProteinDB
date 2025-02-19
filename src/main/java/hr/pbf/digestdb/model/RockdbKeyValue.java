package hr.pbf.digestdb.model;

import hr.pbf.digestdb.util.AminoAcidCoder;
import lombok.Data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Data
public class RockdbKeyValue {

    private double massAsKey = -1;

    private Map<String, Set<Integer>> peptidesAndIdsAsValue = new HashMap<>();


    public void addPeptide(String sequence, String accession) {
        peptidesAndIdsAsValue.computeIfAbsent(sequence, k -> new HashSet<>()).add(Integer.parseInt(accession));
    }

    public static byte[] encodeMapToByteBuffer(Map<String, Set<Integer>> peptidesAndIds) {
        // 1. Izračunajte ukupnu veličinu potrebnog ByteBuffer-a
        int size = Integer.BYTES; // Za broj entries u map-i

        for (Map.Entry<String, Set<Integer>> entry : peptidesAndIds.entrySet()) {
            String peptide = entry.getKey();
            Set<Integer> ids = entry.getValue();

            byte[] peptide5Bytes = AminoAcidCoder.encodePeptideByteBuffer(peptide);

            size += Integer.BYTES; // Za duljinu peptid stringa
            //size += peptide.getBytes(StandardCharsets.UTF_8).length; // Za byte-ove peptid stringa
            size += peptide5Bytes.length;
            size += Integer.BYTES; // Za broj integera u listi
            size += ids.size() * Integer.BYTES; // Za integer-e u listi
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);

        // 2. Pohranite broj entries u map-i
        buffer.putInt(peptidesAndIds.size());

        // 3. Prođite kroz svaki entry u map-i i pohranite podatke
        for (Map.Entry<String, Set<Integer>> entry : peptidesAndIds.entrySet()) {
            String peptide = entry.getKey();
            Set<Integer> ids = entry.getValue();

            // Pohranite peptid string
            //byte[] peptideBytes = peptide.getBytes(StandardCharsets.UTF_8);
            byte[] peptide5Bytes = AminoAcidCoder.encodePeptideByteBuffer(peptide);
            buffer.putInt(peptide5Bytes.length); // Duljina stringa
            buffer.put(peptide5Bytes);         // Byte-ovi stringa

            // Pohranite listu integera
            buffer.putInt(ids.size()); // Broj integera u listi
            for (Integer id : ids) {
                buffer.putInt(id);     // Svaki integer
            }
        }

        return buffer.array();
    }

    public static Map<String, Set<Integer>> decodeByteBufferToMap(byte[] byteArray) {
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);

        // 1. Pročitajte broj entries u map-i
        int entriesCount = buffer.getInt();
        Map<String, Set<Integer>> decodedMap = new HashMap<>(entriesCount);

        // 2. Prođite kroz svaki entry i dekodirajte podatke
        for (int i = 0; i < entriesCount; i++) {
            // Dekodirajte peptid string
            int peptideLength = buffer.getInt(); // Pročitajte duljinu stringa
            byte[] peptideBytes = new byte[peptideLength];
            buffer.get(peptideBytes);           // Pročitajte byte-ove stringa
            // String peptide = new String(peptideBytes, StandardCharsets.UTF_8);
            String peptide = AminoAcidCoder.decodePeptideByteBuffer(peptideBytes, peptideLength);

            // Dekodirajte listu integera
            int idsCount = buffer.getInt();   // Pročitajte broj integera u listi
            Set<Integer> ids = new HashSet<>(idsCount);
            for (int j = 0; j < idsCount; j++) {
                ids.add(buffer.getInt());    // Pročitajte svaki integer
            }
            decodedMap.put(peptide, ids);     // Dodajte u mapu
        }

        return decodedMap;
    }

    public static void main(String[] args) throws IOException {
        RockdbKeyValue keyValue = new RockdbKeyValue();
        keyValue.setMassAsKey(123.456);
        HashMap<String, Set<Integer>> v1 = new HashMap<>();
        v1.put("GGCIVIDGFYYDDLHIFITENPNLYK", Set.of(1, 73, 32, 1234, 234, 523, 2323, 432, 23));
        v1.put("MFFNVPNGTFLLTDDATNENLFIAQK", Set.of(4, 5, 6));
        v1.put("TEDIHSETGEPEEPKRPDSPTK", Set.of(1, 43, 32, 254, 234, 235, 2323, 432, 23));
        v1.put("MLVIFLGILGLLANQVLGLPTQAGGHLR", Set.of(4, 5, 6));
        keyValue.setPeptidesAndIdsAsValue(v1);

        byte[] encodedBytes = RockdbKeyValue.encodeMapToByteBuffer(v1);


        Map<String, Set<Integer>> v2 = RockdbKeyValue.decodeByteBufferToMap(encodedBytes);
        System.out.println(encodedBytes.length);
        System.out.println(v1);
        System.out.println(v2);
    }


}




