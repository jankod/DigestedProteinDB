package hr.pbf.digestdb.util;

import hr.pbf.digestdb.exception.UnknownAminoacidException;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class AminoAcidCoder {

    private static final Map<String, Integer> aminoAcidEncoding = new HashMap<>();
    private static final Map<Integer, String> encodingToAminoAcid = new HashMap<>();

    static {
        // Mapiranje aminokiselina na 5-bitne kodove (heksadecimalni prikaz radi lakšeg čitanja)
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


        // Obrnuto mapiranje za dekodiranje
        for (Map.Entry<String, Integer> entry : aminoAcidEncoding.entrySet()) {
            encodingToAminoAcid.put(entry.getValue(), entry.getKey());
        }
    }

    public static byte[] encodePeptideByteBuffer(String sequence) throws UnknownAminoacidException {
        ByteBuffer buffer = ByteBuffer.allocate((int) Math.ceil(sequence.length() * 5.0 / 8.0)); // Izračunaj veličinu byte arraya
        int bitBuffer = 0;
        int bitCount = 0;

        for (int i = 0; i < sequence.length(); i++) {
            String aminoAcid = String.valueOf(sequence.charAt(i)).toUpperCase(); // Pretvori u uppercase radi robusnosti
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
            buffer.put((byte) (bitBuffer << (8 - bitCount))); // Padding s nulama
        }

        return buffer.flip().array(); // flip() za prebacivanje u read mode, array() za dobivanje byte arraya
    }


    public static String decodePeptideByteBuffer(byte[] encodedBytes, int peptideLength) {
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
                    throw new IllegalArgumentException("Nepoznati 5-bitni kod: " + code);
                }
                decodedSequence.append(aminoAcid);
                bitCount -= 5;
                bitBuffer &= ((1 << bitCount) - 1);
                aminoAcidsDecoded++;
            }
        }
        return decodedSequence.toString();
    }


    public static void main(String[] args) throws UnknownAminoacidException {
        String peptideSequence = "ASELTGEKDLANSSLR";
        byte[] encodedPeptideBytes = encodePeptideByteBuffer(peptideSequence);

        System.out.println("Originalni peptid: " + peptideSequence.getBytes().length);
        System.out.println("Encoded: " + encodedPeptideBytes.length);
        System.out.println("Kodirani peptid (ByteBuffer - byte array): " + byteArrayToHexString(encodedPeptideBytes)); // Ispis u heksadecimalnom formatu za čitljivost
        System.out.println("Dekodirani peptid: " + decodePeptideByteBuffer(encodedPeptideBytes, peptideSequence.length()));

        String peptideSequence_longer = "LVFFAEDVGSNK";
        byte[] encodedPeptideBytes_longer = encodePeptideByteBuffer(peptideSequence_longer);
        System.out.println("\nOriginalni peptid (duži): " + peptideSequence_longer);
        System.out.println("Kodirani peptid (ByteBuffer - byte array - duži): " + byteArrayToHexString(encodedPeptideBytes_longer));
        System.out.println("Dekodirani peptid (duži): " + decodePeptideByteBuffer(encodedPeptideBytes_longer, peptideSequence_longer.length()));
    }

    @Deprecated
    private static byte[] encodePeptideByteBufferWithLength(String sequence) {
        // Allocate extra 4 bytes to store the length
        ByteBuffer buffer = ByteBuffer.allocate(4 + (int) Math.ceil(sequence.length() * 5.0 / 8.0));
        buffer.putInt(sequence.length()); // Store length at the start

        int bitBuffer = 0, bitCount = 0;
        for (int i = 0; i < sequence.length(); i++) {
            String aminoAcidUpper = String.valueOf(sequence.charAt(i)).toUpperCase();
            if (aminoAcidEncoding.get(aminoAcidUpper) == null) {
                throw new IllegalArgumentException("Nepoznata aminokiselina: " + sequence.charAt(i));
            }
            int code = aminoAcidEncoding.get(aminoAcidUpper);
            bitBuffer = (bitBuffer << 5) | code;
            bitCount += 5;
            while (bitCount >= 8) {
                buffer.put((byte) ((bitBuffer >> (bitCount - 8)) & 0xFF));
                bitCount -= 8;
                bitBuffer &= (1 << bitCount) - 1;
            }
        }
        if (bitCount > 0) {
            buffer.put((byte) (bitBuffer << (8 - bitCount)));
        }
        return buffer.array();
    }

    @Deprecated
    private static String decodePeptideByteBufferWithLength(byte[] encodedBytes) {
        ByteBuffer buffer = ByteBuffer.wrap(encodedBytes);
        int peptideLength = buffer.getInt(); // Retrieve the stored length
        int bitBuffer = 0, bitCount = 0, decodedCount = 0;
        StringBuilder decoded = new StringBuilder();

        while (buffer.hasRemaining() && decodedCount < peptideLength) {
            bitBuffer = (bitBuffer << 8) | (buffer.get() & 0xFF);
            bitCount += 8;
            while (bitCount >= 5 && decodedCount < peptideLength) {
                int code = (bitBuffer >> (bitCount - 5)) & 0x1F;
                decoded.append(encodingToAminoAcid.get(code));
                bitCount -= 5;
                bitBuffer &= (1 << bitCount) - 1;
                decodedCount++;
            }
        }
        return decoded.toString();
    }

    // Pomoćna funkcija za ispis byte arraya u heksadecimalnom formatu (radi čitljivosti)
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

