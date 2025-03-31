package hr.pbf.digestdb.util;

import hr.pbf.digestdb.exception.UnknownAminoacidException;

import java.nio.ByteBuffer;
import java.util.Arrays;
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
		for(Map.Entry<String, Integer> entry : aminoAcidEncoding.entrySet()) {
			encodingToAminoAcid.put(entry.getValue(), entry.getKey());
		}
	}

	public static byte[] encodePeptide(String sequence) {
		ByteBuffer buffer = ByteBuffer.allocate((int) Math.ceil(sequence.length() * 5.0 / 8.0) + 10);
		BinaryPeptideDbUtil.writeVarInt(buffer, sequence.length()); // write length via varint

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
			while(bitCount >= 8) {
				buffer.put((byte) ((bitBuffer >> (bitCount - 8)) & 0xFF));
				bitCount -= 8;
				bitBuffer &= (1 << bitCount) - 1;
			}
		}
		if(bitCount > 0) {
			buffer.put((byte) (bitBuffer << (8 - bitCount)));
		}
		return Arrays.copyOf(buffer.array(), buffer.position());
	}

	public static String decodePeptide(byte[] encodedBytes) {
		ByteBuffer buffer = ByteBuffer.wrap(encodedBytes);
		int peptideLength = BinaryPeptideDbUtil.readVarInt(buffer); // read length via varint

		int bitBuffer = 0;
		int bitCount = 0;
		int aminoDecoded = 0;
		StringBuilder sb = new StringBuilder();

		while(buffer.hasRemaining() && aminoDecoded < peptideLength) {
			bitBuffer = (bitBuffer << 8) | (buffer.get() & 0xFF);
			bitCount += 8;
			while(bitCount >= 5 && aminoDecoded < peptideLength) {
				int code = (bitBuffer >> (bitCount - 5)) & 0x1F;
				bitCount -= 5;
				bitBuffer &= (1 << bitCount) - 1;
				sb.append(encodingToAminoAcid.get(code));
				aminoDecoded++;
			}
		}
		return sb.toString();
	}
}