package hr.pbf.digestdb.uniprot;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xerial.snappy.Snappy;

import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;

public class UniprotFormat3 {

	private Format3Index index;

	public UniprotFormat3(String dbPath, String indexPath) {

	}

	public void read(float fromMass, float toMass) {
		long pos = index.getPos(fromMass);

	}

	public static byte[] compressFloatValue(String value) throws UnsupportedEncodingException, IOException {
		return Snappy.compress(value.getBytes("ASCII"));
	}

	public static String uncompress(byte[] compressed) throws UnsupportedEncodingException, IOException {
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

	public static class Format3Index {

		private float[] masses;
		private long[] positions;

		public Format3Index(int length) {
			// int p = 13353898
			masses = new float[length];
			positions = new long[length];
		}

		public long getPos(float from) {
			return 0;
		}
		// unique float 13.353.898 = 53 MB

	}

	public Map<Float, PeptideAccTax> search(float from, float to) {
		return null;
	}

	public static byte[] compressFloatValue(HashMap<String, List<AccTax>> cache) {

		return null;
	}

	public static HashMap<String, List<AccTax>> compressFloatValue(
			byte[] compressed) {

		return null;
	}
}
