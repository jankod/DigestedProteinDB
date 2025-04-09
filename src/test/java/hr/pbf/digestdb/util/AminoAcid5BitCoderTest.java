package hr.pbf.digestdb.util;

import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongMaps;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AminoAcid5BitCoderTest {
	static String sequenceLong = "MGKLIVTDSSGKNSEVELTKERITIGRHADNDIPLADKAVSGHHAVVITILQDSFLEDLDSTNGTQVNGKAIAKHPLSHGDVISIGRNTLRYEGEAGVDDDFERTMILKPGQFGAAFDAQVSQAADAPPAAAPAATAARPTPPPSAKPMLGKLRVASGPNAGKELELSKALTTIGKPGVQVAAVTRRADGYYIVHVGGDSGSQRPLLNGQPIDTQARKLQHNDTVELVGTRMTFLLEA";

	@Test
	void decodePeptide() {
		String sequence = "ACDEFGHIKLMNPQRSTVWY";
		byte[] encoded = AminoAcid5bitCoder.encodePeptide(sequence);
		String decoded = AminoAcid5bitCoder.decodePeptide(encoded);
		assertEquals(sequence, decoded);
	}

	@Test
	void testLongSeqence() {
		byte[] encoded = AminoAcid5bitCoder.encodePeptide(sequenceLong);
		String decoded = AminoAcid5bitCoder.decodePeptide(encoded);
		assertEquals(sequenceLong, decoded);
	}

	@Test
	void testEmpty() {
		String sequence = "";
		byte[] encoded = AminoAcid5bitCoder.encodePeptide(sequence);
		String decoded = AminoAcid5bitCoder.decodePeptide(encoded);
		assertEquals(sequence, decoded);
	}

	@Test
	void testAllAminoAcids() {
		String sequence = "ACDEFGHIKLMNPQRSTVWY";
		byte[] encoded = AminoAcid5bitCoder.encodePeptide(sequence);
		String decoded = AminoAcid5bitCoder.decodePeptide(encoded);
		assertEquals(sequence, decoded);
	}

	public static void main(String[] args) {
		List<String> peptides = BioUtil.tripsyn1mc(sequenceLong, 7, 30);

		for(String peptide : peptides) {
			byte[] encoded = AminoAcid5bitCoder.encodePeptide(peptide);
			int lengthEncoded = encoded.length;
			int peptideLength = peptide.length();
			double ratio = (double) lengthEncoded / peptideLength;
			System.out.println("Peptide: " + peptide + " Length: " + peptideLength + " Encoded length: " + lengthEncoded + " Ratio: " + ratio);
		}

	}
}