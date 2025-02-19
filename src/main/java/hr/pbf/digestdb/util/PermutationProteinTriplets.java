package hr.pbf.digestdb.util;

import java.util.ArrayList;

/**
 * Permutacija s ponavljanjem jer se pojedina aminokiselina moze pojavljivati
 * vise puta.
 * https://www.mathsisfun.com/combinatorics/combinations-permutations-calculator.html<br>
 * n<sup>r</sup>
 *
 * @author tag
 *
 */
public class PermutationProteinTriplets {

	private final int size;

	public static void main(String[] args) {

		ArrayList<String> res = new PermutationProteinTriplets(3).getAllProteinPermutation();
		System.out.println("Rezuls " + res.size());
		System.out.println(res);
		// System.out.println(Math.pow(20, 2));
	}

	public PermutationProteinTriplets(int size) {
		this.size = size;

	}

	private ArrayList<String> result;
	String aa = "ILVFMCAGPTSYWQNHEDKR";

	public ArrayList<String> getAllProteinPermutation() {
		result = new ArrayList<>((int) Math.pow(20, size) + 1);
		this.permutation(new char[size], 0, aa);
		return result;
	}

//	public ArrayList<String> getAllStringPermutation(String s) {
//		result = new ArrayList<>((int) Math.pow(20, size) + 1);
//		this.permutation(new char[size], 0, s);
//		return result;
//	}

	// private int c = 0;
	// static HashSet<String> set = new HashSet<>(8000);

	public static ArrayList<String> getAllplets(String peptide, int length) {
		ArrayList<String> parts = new ArrayList<>(peptide.length() - length + 1);

		for (int i = 0; i < peptide.length(); i++) {
			if (i + length > peptide.length()) {
				break;
			}
			String p = peptide.substring(i, i + length);
			parts.add(p);
		}

		return parts;
	}

	public final void permutation(char[] perm, int pos, String str) {
		if (pos == perm.length) {
			result.add(new String(perm));
			// System.out.println(c + " "+ new String(perm));
		} else {
			for (int i = 0; i < str.length(); i++) {
				perm[pos] = str.charAt(i);
				permutation(perm, pos + 1, str);
			}
		}
	}

}
