package hr.pbf.digestdb.util;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.wildfly.common.annotation.NotNull;

import java.io.*;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Utility class for working with binary peptide databases.
 * <b>It is not thread safe!</b>
 */
@Slf4j
@UtilityClass
public class BinaryPeptideDbUtil {

	private static ByteBuffer bufferCache = ByteBuffer.allocate(1024 * 1024 * 36); // 36MB

	public void writeVarInt(ByteBuffer buffer, int value) {
		// VarInt use unsigned int, so we need to make sure we are working with positive values
		while((value & 0xFFFFFF80) != 0) {
			// Use the first 7 bits and set the highest bit to 1 (continuation)
			buffer.put((byte) ((value & 0x7F) | 0x80));
			// Move the value 7 bits to the right
			value >>>= 7;
		}
		// Last byte, the highest bit is 0 (end)
		buffer.put((byte) (value & 0x7F));
	}

	public void writeVarInt(DataOutputStream dos, int value) throws IOException {
		while(value >= 128) {
			dos.writeByte((byte) ((value & 0x7F) | 0x80));
			value >>>= 7;
		}
		dos.writeByte((byte) value);
	}

	public int readVarInt(DataInputStream din) throws IOException {
		int result = 0;
		int shift = 0;
		while(true) {
			byte b = din.readByte();
			result |= (b & 0x7F) << shift;
			shift += 7;
			if((b & 0x80) == 0) {
				return result;
			}
			if(shift >= 32) {
				throw new IllegalStateException("Varint to large!");
			}
		}
	}

	public int readVarInt(ByteBuffer buffer) {
		int result = 0;
		int shift = 0;

		while(true) {
			if(!buffer.hasRemaining()) {
				throw new IllegalArgumentException("Buffer underflow: incomplete VarInt");
			}

			byte currentByte = buffer.get();
			// Use the first 7 bits and set the highest bit to 1 (continuation)
			result |= (currentByte & 0x7F) << shift;

			// If the highest bit is not 1, we are done
			if((currentByte & 0x80) == 0) {
				break;
			}

			// Move the value 7 bits to the right
			shift += 7;

			// Check for too large VarInt (maximum 5 bytes)
			if(shift > 35) {
				throw new IllegalArgumentException("VarInt too large");
			}
		}

		return result;
	}

	public Set<PeptideAcc> readGroupedRowNew(byte[] value) {
		FastByteArrayInputStream bin = new FastByteArrayInputStream(new byte[(int) (value.length * 1.3)]);
		DataInputStream in = new DataInputStream(bin);

		Set<PeptideAcc> peptides = new HashSet<>();

		// must got SGAGAAA:15-SAAGGAA:14-TGAAAGG:16;12345
		try {
			readVarInt(in);

		} catch(IOException e) {
			log.error("Error reading varint", e);
			throw new RuntimeException(e);
		}

		return peptides;

	}

	public Set<PeptideAcc> readGroupedRow(byte[] value) {
		ByteBuffer buffer = ByteBuffer.wrap(value);
		Set<PeptideAcc> peptides = new HashSet<>();

		while(buffer.hasRemaining()) {
			int seqLength = readVarInt(buffer);
			byte[] seqBytes = new byte[seqLength];
			buffer.get(seqBytes);
			String sequence = AminoAcid5bitCoder.decodePeptide(seqBytes);

			int accessionCount = readVarInt(buffer);

			PeptideAcc acc = new PeptideAcc();
			acc.seq = sequence;
			acc.acc = new int[accessionCount];
			for(int i = 0; i < accessionCount; i++) {
				//acc.acc[i] = accessions.get(i);
				acc.acc[i] = readVarInt(buffer);
			}
			peptides.add(acc);
		}

		return peptides;
	}

	/**
	 * Writes a mass and its corresponding sequences to a CSV row.
	 * The format is: mass, sequence1:ids1-sequence2:ids2-...
	 * @param buffer
	 * @param sequenceMap
	 */
	public void writeMassToCsvRow(StringBuilder buffer, Map<String, IntOpenHashSet> sequenceMap) {
		buffer.setLength(0);
		Set<Map.Entry<String, IntOpenHashSet>> entries = sequenceMap.entrySet();
		for(Map.Entry<String, IntOpenHashSet> entry : entries) {
			String sequence = entry.getKey();
			IntOpenHashSet ids = entry.getValue();

			StringBuilder idsStr = new StringBuilder(ids.size() * 6);
			for(IntIterator it = ids.iterator(); it.hasNext(); ) {
				idsStr.append(it.nextInt()).append(";");
			}
			if(!idsStr.isEmpty())
				idsStr.setLength(idsStr.length() - 1);

			buffer.append(sequence).append(":").append(idsStr).append("-");
		}
		if(!buffer.isEmpty())
			buffer.setLength(buffer.length() - 1);

		//	writer.write(mass + "," + buffer);
		//	writer.newLine();
	}

	/**
	 * @param value String in format like: SGAGAAA:15-SAAGGAA:14-TGAAAGG:16;12345
	 * @return byte[] in format: [length][data]
	 */
	public static byte[] writeGroupedRow(String value) {
		try {
			bufferCache.clear();
			int start = 0;
			while(start < value.length()) {
				int colonIndex = value.indexOf(':', start);
				String seq = value.substring(start, colonIndex);
				int dashIndex = value.indexOf('-', colonIndex);
				if(dashIndex == -1)
					dashIndex = value.length();
				String[] accessions = value.substring(colonIndex + 1, dashIndex).split(";");

				byte[] seqBytes = AminoAcid5bitCoder.encodePeptide(seq);
				// byte[] seqBytes = seq.getBytes(StandardCharsets.UTF_8);
				ensureCapacity(bufferCache, seqBytes.length + 5); // 4 bytes for length + 1 byte for data
				writeVarInt(bufferCache, seqBytes.length);

				bufferCache.put(seqBytes);

				writeVarInt(bufferCache, accessions.length);
				for(String acc : accessions) {
					writeVarInt(bufferCache, Integer.parseInt(acc));
				}
				start = dashIndex + 1;
			}
			return Arrays.copyOf(bufferCache.array(), bufferCache.position());
		} catch(Exception e) {
			log.error("Error on line: " + StringUtils.truncate(value, 20_000), e);
			throw e;
		}
	}

	private void ensureCapacity(ByteBuffer buff, int additionalCapacity) {
		if(buff.remaining() < additionalCapacity) {
			int newCapacity = Math.max((int) (buff.capacity() * 2), buff.capacity() + additionalCapacity);
			ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
			buff.flip();
			newBuffer.put(buff);
			bufferCache = newBuffer;
		}
	}

	@Data
	public static class PeptideAcc implements Comparable<PeptideAcc> {
		String seq;
		int[] acc;

		@Override
		public int compareTo(@NotNull PeptideAcc o) {
			if(seq.equals(o.seq)) {
				return 0;
			}
			return seq.compareTo(o.seq);
		}

		@Override
		public String toString() {
			return seq + " " + Arrays.toString(acc);
		}
	}

}
