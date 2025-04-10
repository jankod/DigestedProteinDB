package hr.pbf.digestdb.util;

import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.wildfly.common.annotation.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Utility class for working with binary peptide databases.
 * <b>It is not thread safe!</b>
 */
@Slf4j
@UtilityClass
public class BinaryPeptideDbUtil {

	private static ByteBuffer bufferCache = ByteBuffer.allocate(1024 * 1024 * 32); // 32MB, prilagodite po potrebi

	public void writeVarInt(ByteBuffer buffer, int value) {
		while(value >= 128) {
			ensureCapacity(buffer, 1); // Ensure there is space for at least 1 byte
			buffer.put((byte) ((value & 0x7F) | 0x80));
			value >>>= 7;
		}
		buffer.put((byte) value);
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
		while(buffer.hasRemaining()) {
			byte b = buffer.get();
			result |= (b & 0x7F) << shift;
			shift += 7;
			if((b & 0x80) == 0) {
				return result;
			}
			if(shift >= 32) {
				throw new IllegalStateException("Varint to large!");
			}
		}
		throw new IllegalStateException("Incomplete varint in buffer! Buffer: " + buffer);
	}

	public Set<PeptideAcc> readGroupedRow(byte[] value) {
		ByteBuffer buffer = ByteBuffer.wrap(value);
		Set<PeptideAcc> peptides = new HashSet<>();

		while(buffer.hasRemaining()) {
			int seqLength = readVarInt(buffer);
			byte[] seqBytes = new byte[seqLength];
			buffer.get(seqBytes);
			String sequence = AminoAcid5bitCoder.decodePeptide(seqBytes);
			//String sequence = new String(seqBytes, StandardCharsets.UTF_8);

			int accessionCount = readVarInt(buffer);
			List<Integer> accessions = new ArrayList<>(accessionCount);
			for(int i = 0; i < accessionCount; i++) {
				int accession = readVarInt(buffer);
				accessions.add(accession);
			}

			PeptideAcc acc = new PeptideAcc();
			acc.seq = sequence;
			acc.acc = new int[accessions.size()];
			for(int i = 0; i < accessions.size(); i++) {
				acc.acc[i] = accessions.get(i);
			}
			peptides.add(acc);
		}

		return peptides;
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
			ByteBuffer newBuffer = ByteBuffer.allocate((int) (buff.capacity() * 1.2 + additionalCapacity));
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
