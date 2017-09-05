package hr.pbf.digestdb.compact;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.eclipse.collections.api.bimap.ImmutableBiMap;
import org.eclipse.collections.impl.factory.BiMaps;

import com.google.common.collect.BiMap;

public class CompactCSV {

	public static void main(String[] args) {
		byte b1 = 1;
		System.out.println(Integer.toBinaryString(b1));

	}

	public CompactCSV() {
		// ImmutableBiMap<String, Character> of = BiMaps.immutable.ofAll(null)
	}

	public void convertCSVtoCompact(String path) {

		double mass;
		String peptide;
		long accessonNum;

		DataOutputStream o;

	}

	public byte[] peptideToCompact(String peptide) {
		
		for (int i = 0; i < peptide.length(); i++) {
			char charAt = peptide.charAt(i);
			

		}
		return null;

	}

	public String compactToPeptide(byte[] peptide) {
		return null;

	}

}
