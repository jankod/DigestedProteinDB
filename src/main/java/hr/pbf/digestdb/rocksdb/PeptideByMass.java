package hr.pbf.digestdb.rocksdb;

import lombok.Data;

import java.io.*;
import java.util.*;

@Data
public class PeptideByMass implements Externalizable {
    private String peptide;
    private Set<String> accessions;
    private Set<Integer> taxonomyIds;

    private static final Map<Character, Integer> aminoToCode = new HashMap<>();
    private static final Map<Integer, Character> codeToAminoAcid = new HashMap<>();

    static {
        // Definirajte mapiranje aminokiselina na 5-bitne kodove
        char[] aminoAcids = {'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y'};
        for (int i = 0; i < aminoAcids.length; i++) {
            aminoToCode.put(aminoAcids[i], i);
            codeToAminoAcid.put(i, aminoAcids[i]);
        }
    }

    public String randomPeptides(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * aminoToCode.size());
            sb.append(codeToAminoAcid.get(randomIndex));
        }
        return sb.toString();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
//        byte[] peptideArray = encodePeptideTo5Bit(peptide);
//        out.writeInt(peptideArray.length); // Zapišite veličinu niza
//        out.write(peptideArray); // Serijalizirajte 5-bitni kodirani niz

        out.writeUTF(peptide); // Serijalizirajte peptide kao UTF-8 string

        // Serijalizacija List<String> accessions ručno
        out.writeInt(accessions.size()); // Zapišite veličinu liste
        for (String accession : accessions) {
            out.writeUTF(accession); // Zapišite svaki string u listi
        }

        // Serijalizacija List<Integer> taxonomyIds ručno
        out.writeInt(taxonomyIds.size()); // Zapišite veličinu liste
        for (Integer taxonomyId : taxonomyIds) {
            out.writeInt(taxonomyId); // Zapišite svaki integer u listi
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // Not work 5 bit encoding
//        int paptideArrayLength = in.readInt();
//        byte[] peptideArray = new byte[paptideArrayLength];
//        in.readFully(peptideArray);
//        peptide = decodePeptideFrom5Bit(peptideArray); // Deserijalizirajte 5-bitni kodirani niz

        peptide = in.readUTF(); // Deserijalizirajte peptide kao UTF-8 string

        // Deserijalizacija List<String> accessions ručno
        int accessionsSize = in.readInt(); // Pročitajte veličinu liste
        accessions = new HashSet<>(accessionsSize);
        for (int i = 0; i < accessionsSize; i++) {
            accessions.add(in.readUTF()); // Pročitajte svaki string i dodajte u listu
        }

        // Deserijalizacija List<Integer> taxonomyIds ručno
        int taxonomyIdsSize = in.readInt(); // Pročitajte veličinu liste
        taxonomyIds = new HashSet<>(taxonomyIdsSize);
        for (int i = 0; i < taxonomyIdsSize; i++) {
            taxonomyIds.add(in.readInt()); // Pročitajte svaki integer i dodajte u listu
        }
    }

    private static byte[] encodePeptideTo5Bit(String peptideSequence) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int currentByte = 0;
        int bitsInBuffer = 0;

        for (char aminoAcid : peptideSequence.toCharArray()) {
            int code = aminoToCode.getOrDefault(aminoAcid, -1);
            if (code == -1) {
                throw new IllegalArgumentException("Unknown amino acid: " + aminoAcid);
            }

            currentByte |= (code << bitsInBuffer);
            bitsInBuffer += 5;

            while (bitsInBuffer >= 8) {
                byteStream.write(currentByte & 0xFF);
                bitsInBuffer -= 8;
                currentByte = 0; // Resetirajte currentByte na 0 za novi bajt
                // currentByte = (code >>> (5 - bitsInBuffer)); // OBRISATI OVU LINIJU - POGREŠNO
            }
        }

        if (bitsInBuffer > 0) {
            byteStream.write(currentByte & 0xFF);
        }

        return byteStream.toByteArray();
    }


    private static String decodePeptideFrom5Bit(byte[] encodedPeptide) {
        StringBuilder peptideBuilder = new StringBuilder();
        int currentCode = 0;
        int bitsInCode = 0;

        for (byte encodedByte : encodedPeptide) {
            int byteValue = encodedByte & 0xFF; // Pretvorite u unsigned int (0-255)

            for (int i = 0; i < 8; i++) {
                currentCode |= ((byteValue >> i) & 1) << bitsInCode; // Izdvojite i-ti bit i dodajte ga u trenutni kod
                bitsInCode++;

                if (bitsInCode == 5) {
                    if (codeToAminoAcid.containsKey(currentCode)) {
                        peptideBuilder.append(codeToAminoAcid.get(currentCode));
                    } else {
                        // Handle unknown code (error or replacement character?)
                        peptideBuilder.append('?'); // Replacement character for unknown code
                    }
                    currentCode = 0;
                    bitsInCode = 0;
                }
            }
        }
        return peptideBuilder.toString();
    }


}
