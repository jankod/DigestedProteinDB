package hr.pbf.digestdb.uniprot;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.xerial.snappy.Snappy;

import com.esotericsoftware.kryo.io.UnsafeInput;
import com.esotericsoftware.kryo.io.UnsafeOutput;

import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideMassAccTaxList;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class UniprotFormat3 {

	private Format3Index index;
	private String pathDB;
	private String pathIndex;
	private File fileDb;

	public UniprotFormat3(String pathDB, String pathIndex) throws FileNotFoundException, IOException {
		this.pathDB = pathDB;
		this.fileDb = new File(pathDB);
		this.pathIndex = pathIndex;
		index = new Format3Index(0);
		try (FileInputStream in = new FileInputStream(new File(pathIndex))) {
			index.readFromDataOutput(new MyDataInputStream(in));
		}
	}

	public void read(float fromMass, float toMass) {

	}

	public static byte[] compressASCII(String value) throws UnsupportedEncodingException, IOException {
		return Snappy.compress(value.getBytes("ASCII"));
	}

	public static String uncompressASCII(byte[] compressed) throws UnsupportedEncodingException, IOException {
		byte[] b = Snappy.uncompress(compressed);
		return new String(b);
	}

	public static String formatValue(String peptide, ArrayList<AccTax> accTaxList) {
		StringBuilder buffer = new StringBuilder(accTaxList.size() * 15);
		boolean first = true;
		for (AccTax accTax : accTaxList) {
			if (!first) {
				buffer.append(",");
			}
			buffer.append(accTax.getAcc()).append(":").append(accTax.getTax());
			first = false;
		}
		return peptide + "\t" + buffer.toString();
	}
	public PeptideMassAccTaxList get(float mass) throws IOException {

		long[] postiotions = index.get(mass);
		byte[] bytes = UniprotUtil.toByteArrayFast(fileDb, postiotions[0], postiotions[1]);
		String uncompress = uncompressASCII(bytes);
		Scanner s = new Scanner(uncompress);
		while (s.hasNextLine()) {
			String line = s.nextLine();
		}
		return null;
	}

	public static byte[] compressPeptidesJava(Map<String, List<AccTax>> peptidesAccTax) throws IOException {
		// UnsafeOutput out = new UnsafeOutput(new
		// FastByteArrayOutputStream(peptidesAccTax.size() * 16 * 2));
		FastByteArrayOutputStream byteOut = new FastByteArrayOutputStream(peptidesAccTax.size() * 8 * 16);
		MyDataOutputStream out = new MyDataOutputStream(byteOut);

		Set<Entry<String, List<AccTax>>> entrySet = peptidesAccTax.entrySet();
		out.writeInt(entrySet.size());
		for (Entry<String, List<AccTax>> entry : entrySet) {
			String peptide = entry.getKey();
			List<AccTax> accTaxList = entry.getValue();
			out.writeUTF(peptide);
			out.writeInt(accTaxList.size());
			for (AccTax accTax : accTaxList) {
				out.writeUTF(accTax.getAcc());
				out.writeInt(accTax.getTax());
			}
		}
		out.close();
		byteOut.trim();
		return byteOut.array;
	}

	public static byte[] compressPeptides(HashMap<String, List<AccTax>> peptidesAccTax) throws IOException {
		UnsafeOutput out = new UnsafeOutput(new FastByteArrayOutputStream(peptidesAccTax.size() * 16 * 2));

		Set<Entry<String, List<AccTax>>> entrySet = peptidesAccTax.entrySet();
		out.writeVarInt(entrySet.size(), true);
		for (Entry<String, List<AccTax>> entry : entrySet) {
			String peptide = entry.getKey();
			List<AccTax> accTaxList = entry.getValue();
			out.writeAscii(peptide);
			out.writeVarInt(accTaxList.size(), true);
			for (AccTax accTax : accTaxList) {
				out.writeAscii(accTax.getAcc());
				out.writeInt(accTax.getTax(), true);
			}
		}
		out.close();
		return Snappy.compress(out.getBuffer());
	}

	public static TreeMap<String, List<AccTax>> uncompressPeptidesJava(byte[] compressed) throws IOException {
		try (MyDataInputStream in = new MyDataInputStream(new FastByteArrayInputStream(compressed))) {
			int howPeptides = in.readInt();
			TreeMap<String, List<AccTax>> result = new TreeMap<>();
			for (int i = 0; i < howPeptides; i++) {
				String peptide = in.readUTF();
				int howAccTax = in.readInt();
				ArrayList<AccTax> accTaxs = new ArrayList<>(howAccTax);
				for (int j = 0; j < howAccTax; j++) {
					if (!accTaxs.add(new AccTax(in.readUTF(), in.readInt()))) {
						throw new RuntimeException("Not added accc");
					}
				}
				result.put(peptide, accTaxs);

			}
			return result;
		}

	}

	public static HashMap<String, List<AccTax>> uncompressPeptides(byte[] compressed) throws IOException {
		// KryoException: Buffer underflow.
		UnsafeInput in = new UnsafeInput(Snappy.uncompress(compressed));
		int howPeptides = in.readVarInt(true);
		HashMap<String, List<AccTax>> result = new HashMap<>(howPeptides);
		for (int i = 0; i < howPeptides; i++) {
			String peptide = in.readString();
			int howAccTax = in.readVarInt(true);
			ArrayList<AccTax> accTaxs = new ArrayList<>(howAccTax);
			for (int j = 0; j < howAccTax; j++) {
				accTaxs.add(new AccTax(in.readString(), in.readInt(true)));
			}
			result.put(peptide, accTaxs);

		}
		in.close();
		return result;
	}
}
