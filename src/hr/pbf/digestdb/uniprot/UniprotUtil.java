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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.minlog.Log;

import java.util.Map.Entry;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxMass;
import hr.pbf.digestdb.util.BioUtil;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class UniprotUtil {
	private static final Logger log = LoggerFactory.getLogger(UniprotUtil.class);

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

	public final static byte[] toFormat2(Map<String, List<PeptideAccTax>> grouped) throws IOException {
		FastByteArrayOutputStream byteOut = new FastByteArrayOutputStream(grouped.size() * 18);
		// MyDataOutputStream out = new MyDataOutputStream(byteOut);
		FastOutput out = new FastOutput(byteOut);

		Set<Entry<String, List<PeptideAccTax>>> entrySet = grouped.entrySet();
		out.writeInt(entrySet.size());
		for (Entry<String, List<PeptideAccTax>> entry : entrySet) {
			String peptide = entry.getKey();
			List<PeptideAccTax> p = entry.getValue();
			out.writeAscii(peptide);

			out.writeShort(p.size());
			for (PeptideAccTax pAccTax : p) {
				out.writeAscii(pAccTax.getAcc());
				out.writeInt(pAccTax.getTax());
			}
		}
		out.close();
		return byteOut.array;
	}

	public final static Map<String, List<PeptideAccTaxMass>> fromFormat2(byte[] format2, boolean sortByMass)
			throws IOException {
		return fromFormat2(format2, sortByMass, 0, 1000000);
	}

	public final static Map<String, List<PeptideAccTaxMass>> fromFormat2(byte[] format2, boolean sortByMass,
			float fromMass, float toMass) throws IOException {

		FastInput in = new FastInput(new FastByteArrayInputStream(format2));

		final int how = in.readInt();
		Map<String, List<PeptideAccTaxMass>> result;
		if (!sortByMass) {
			result = new HashMap<String, List<PeptideAccTaxMass>>(how);
		} else {
			result = new TreeMap<String, List<PeptideAccTaxMass>>(new Comparator<String>() {

				@Override
				public int compare(String p1, String p2) {
					double m1 = BioUtil.calculateMassWidthH2O(p1);
					double m2 = BioUtil.calculateMassWidthH2O(p2);
					return Double.compare(m1, m2);
				}
			});
		}
		for (int i = 0; i < how; i++) {
			String peptide = in.readString();
			float mass = (float) BioUtil.calculateMassWidthH2O(peptide);
			boolean skipPeptide = false;
			if (mass < fromMass || toMass < mass) {
				skipPeptide = true;
			}

			int howInList = in.readShort();
			ArrayList<PeptideAccTaxMass> pepList = null;

			if (!skipPeptide) {
				pepList = new ArrayList<PeptideAccTaxMass>(howInList);
				if (result.containsKey(peptide)) {
					result.get(peptide).addAll(pepList);
				} else {
					result.put(peptide, pepList);
				}
			}
			for (int j = 0; j < howInList; j++) {
				String acc = in.readString();
//				if ("A0A1J4YX49".equals(acc)) {
//					log.debug("nasao peptide: " + peptide + " skip: " + skipPeptide);
//				}
				int tax = in.readInt();
				if (!skipPeptide)
					pepList.add(j, new PeptideAccTaxMass(peptide, acc, tax, mass));
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

	public static String getDirectorySize(String pathDir) {
		return FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectory(new File(pathDir)));
	}

	public static byte[] toByteArrayFast(String path) throws IOException {
		return toByteArrayFast(new File(path));
	}

	public static byte[] toByteArrayFast(File f) throws IOException {
		try (RandomAccessFile memoryFile = new RandomAccessFile(f.getPath(), "r")) {
			long length = f.length();
			if (length > Integer.MAX_VALUE) {
				throw new IOException("File length is more then integer: " + length);
			}
			MappedByteBuffer mappedByteBuffer = memoryFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, length);
			// mappedByteBuffer.array();
			byte[] all = new byte[(int) length];
			mappedByteBuffer.get(all);
			return all;
		}
	}

	public static ArrayList<PeptideAccTax> fromFormat1(byte[] bytes) throws IOException {
		MyDataInputStream in = new MyDataInputStream(new FastByteArrayInputStream(bytes));

		ArrayList<PeptideAccTax> pepList = new ArrayList<>();
		while (in.available() != 0) {
			PeptideAccTax pep = UniprotUtil.readOneFormat1(in);
			pepList.add(pep);
		}
		in.close();
		return pepList;
	}

	public static byte[] uncompress(byte[] b) throws IOException {
		return Snappy.uncompress(b);
	}

	public static byte[] compress(byte[] b) throws IOException {
		return Snappy.compress(b);
	}
}