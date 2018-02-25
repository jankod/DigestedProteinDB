package hr.pbf.digestdb.uniprot;

import static java.util.stream.Collectors.groupingBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class UniprotUtil {

	/**
	 * Remove entry with mass less on fromMass or more than toMass.
	 * 
	 * @param masses
	 * @param fromMass
	 * @param toMass
	 */
	public final static void reduceMasses(Map<String, Double> masses, int fromMass, int toMass) {
		Iterator<String> it = masses.keySet().iterator();
		while (it.hasNext()) {
			Double m = masses.get(it.next());
			if (!(fromMass <= m && m <= toMass))
				it.remove();
		}
	}

	public final static void writeOneFormat1(DataOutput out, String peptide, int tax, String acc) throws IOException {
		out.writeUTF(peptide);
		out.writeInt(tax);
		out.writeUTF(acc);
	}

	public final static PeptideAccTax readOneFormat1(DataInput in) throws IOException {
		String peptide = in.readUTF();
		int tax = in.readInt();
		String acc = in.readUTF();
		return new PeptideAccTax(peptide, acc, tax);
	}

	/**
	 * Group by peptide and write to byte array.
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public final static Map<String, List<PeptideAccTax>> groupByPeptide(ArrayList<PeptideAccTax> data) {
		Map<String, List<PeptideAccTax>> grouped = data.stream().collect(groupingBy(o -> {
			return o.getPeptide();
		}));
		return grouped;
	}

	public final static byte[] peptideToFormat2(Map<String, List<PeptideAccTax>> grouped) throws IOException {
		FastByteArrayOutputStream byteOut = new FastByteArrayOutputStream(grouped.size() * 18);

		MyDataOutputStream out = new MyDataOutputStream(byteOut);

		Set<Entry<String, List<PeptideAccTax>>> entrySet = grouped.entrySet();
		out.writeInt(entrySet.size());
		for (Entry<String, List<PeptideAccTax>> entry : entrySet) {
			String peptide = entry.getKey();
			List<PeptideAccTax> p = entry.getValue();
			out.writeUTF(peptide);
			out.writeInt(p.size());
			for (PeptideAccTax pAccTax : p) {
				out.writeUTF(pAccTax.getAcc());
				out.writeInt(pAccTax.getTax());
			}
		}
		out.close();
		return byteOut.array;
	}

	public final static Map<String, List<PeptideAccTax>> format2ToPeptides(byte[] format2) throws IOException {
		DataInputStream in = new DataInputStream(new FastByteArrayInputStream(format2));
		final int how = in.readInt();
		Map<String, List<PeptideAccTax>> result = new HashMap<String, List<PeptideAccTax>>(how);
		for (int i = 0; i < how; i++) {
			String peptide = in.readUTF();
			int howInList = in.readInt();
			ArrayList<PeptideAccTax> pepList = new ArrayList<PeptideAccTax>(howInList);
			result.put(peptide, pepList);
			for (int j = 0; j < howInList; j++) {
				pepList.add(j, new PeptideAccTax(peptide, in.readUTF(), in.readInt()));
			}
		}
		in.close();
		return result;

	}

	public final static ArrayList<PeptideAccTax> format2ToPeptidesAndUnGroup(byte[] format2) throws IOException {
		MyDataInputStream in = new MyDataInputStream(new FastByteArrayInputStream(format2));
		final int how = in.readInt();
		ArrayList<PeptideAccTax> res = new ArrayList<>(how);
		for (int i = 0; i < how; i++) {
			final String peptide = in.readUTF();
			final int howAccTax = in.readInt();
			for (int j = 0; j < howAccTax; j++) {
				final PeptideAccTax p = new PeptideAccTax();
				p.setPeptide(peptide);
				p.setAcc(in.readUTF());
				p.setTax(in.readInt());
				res.add(p);
			}
		}
		in.close();
		return res;
	}

	
	public static byte[] toByteArrayFast(File f) throws IOException {
		RandomAccessFile memoryFile = new RandomAccessFile(f.getPath(), "r");
		long length = f.length();
		MappedByteBuffer mappedByteBuffer = memoryFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, length);
		// mappedByteBuffer.array();
		byte[] all = new byte[(int) length];
		mappedByteBuffer.get(all);
		memoryFile.close();
		return all;
	}
}
