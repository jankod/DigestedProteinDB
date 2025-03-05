package hr.pbf.digestdb.util;

import hr.pbf.digestdb.exception.UnknownAminoacidException;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class AminoAcid5bitCoder {

    private static final Map<String, Integer> aminoAcidEncoding = new HashMap<>(22);
    private static final Map<Integer, String> encodingToAminoAcid = new HashMap<>(22);

    static {
        aminoAcidEncoding.put("A", 0x0);
        aminoAcidEncoding.put("C", 0x1);
        aminoAcidEncoding.put("D", 0x2);
        aminoAcidEncoding.put("E", 0x3);
        aminoAcidEncoding.put("F", 0x4);
        aminoAcidEncoding.put("G", 0x5);
        aminoAcidEncoding.put("H", 0x6);
        aminoAcidEncoding.put("I", 0x7);
        aminoAcidEncoding.put("K", 0x8);
        aminoAcidEncoding.put("L", 0x9);
        aminoAcidEncoding.put("M", 0xA);
        aminoAcidEncoding.put("N", 0xB);
        aminoAcidEncoding.put("P", 0xC);
        aminoAcidEncoding.put("Q", 0xD);
        aminoAcidEncoding.put("R", 0xE);
        aminoAcidEncoding.put("S", 0xF);
        aminoAcidEncoding.put("T", 0x10);
        aminoAcidEncoding.put("V", 0x11);
        aminoAcidEncoding.put("W", 0x12);
        aminoAcidEncoding.put("Y", 0x13);
        aminoAcidEncoding.put("U", 0x14); // Selenocistein
        aminoAcidEncoding.put("O", 0x15); // Piroglutamat


        // reverse mapping
        for (Map.Entry<String, Integer> entry : aminoAcidEncoding.entrySet()) {
            encodingToAminoAcid.put(entry.getValue(), entry.getKey());
        }
    }

    public static byte[] encodePeptide(String sequence) throws UnknownAminoacidException {
        ByteBuffer buffer = ByteBuffer.allocate((int) Math.ceil(sequence.length() * 5.0 / 8.0));
        int bitBuffer = 0;
        int bitCount = 0;

        for (int i = 0; i < sequence.length(); i++) {
            String aminoAcid = String.valueOf(sequence.charAt(i)).toUpperCase();
            Integer code = aminoAcidEncoding.get(aminoAcid);
            if (code == null) {
                throw new UnknownAminoacidException(aminoAcid, sequence);
            }

            bitBuffer = (bitBuffer << 5) | code;
            bitCount += 5;

            while (bitCount >= 8) {
                byte byteToWrite = (byte) ((bitBuffer >> (bitCount - 8)) & 0xFF);
                buffer.put(byteToWrite);
                bitCount -= 8;
                bitBuffer &= ((1 << bitCount) - 1);
            }
        }

        if (bitCount > 0) {
            buffer.put((byte) (bitBuffer << (8 - bitCount)));
        }

        return buffer.flip().array();
    }


    public static String decodePeptide(byte[] encodedBytes, int peptideLength) {
        ByteBuffer buffer = ByteBuffer.wrap(encodedBytes);
        StringBuilder decodedSequence = new StringBuilder();
        int bitBuffer = 0;
        int bitCount = 0;
        int aminoAcidsDecoded = 0;

        while (buffer.hasRemaining() && aminoAcidsDecoded < peptideLength) {
            byte encodedByte = buffer.get();
            bitBuffer = (bitBuffer << 8) | (encodedByte & 0xFF);
            bitCount += 8;

            while (bitCount >= 5 && aminoAcidsDecoded < peptideLength) {
                int code = (bitBuffer >> (bitCount - 5)) & 0x1F;
                String aminoAcid = encodingToAminoAcid.get(code);
                if (aminoAcid == null) {
                    throw new IllegalArgumentException("Unknown amino acid: " + code);
                }
                decodedSequence.append(aminoAcid);
                bitCount -= 5;
                bitBuffer &= ((1 << bitCount) - 1);
                aminoAcidsDecoded++;
            }
        }
        return decodedSequence.toString();
    }


    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static boolean isInvalidPeptide(String sequence) {
        for (int i = 0; i < sequence.length(); i++) {
            if (!aminoAcidEncoding.containsKey(String.valueOf(sequence.charAt(i)))) {
                return true;
            }
        }
        return false;
    }
}

