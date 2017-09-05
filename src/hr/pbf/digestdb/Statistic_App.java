package hr.pbf.digestdb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.Frequency;
import org.eclipse.collections.impl.bag.mutable.HashBag;

import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.PermutationProteinTriplets;

public class Statistic_App {

	public static final String ARG_APP_NAME = "stat";

	public static void printArgs() {
		System.out.println();
		System.out.println(Statistic_App.class.getName());
		System.out.println("Stat");
		System.out.println("Count unique mass inn CSV");
		System.out.println();
	}

	static NumberFormat nf = NumberFormat.getInstance();

	public static void main(String[] args) throws FileNotFoundException, IOException {
		FileInputStream input;

		input = new FileInputStream(new File("/home/mysql_data/mysql/nr_mass.csv"));
		// input = new FileInputStream(
		// new
		// File("C:\\Eclipse\\OxygenWorkspace\\CreateNR\\misc\\sample_data\\850_000_nr_mass.csv"));

		int pletsSize = 3;
		boolean stopAtNumber = true;
		long stopAt = Long.MAX_VALUE; //1_000_000 * 20;
		int startFromGB = 0;
		System.out.println(
				"Uzeti samo peptida: " + nf.format(stopAt) + ", startao od: " + startFromGB + "GB, plet size: " + pletsSize);

		if (startFromGB != 0) {
			input.skip(FileUtils.ONE_GB * startFromGB); // od 40GB startaj
		}

		LineIterator it = IOUtils.lineIterator(new BufferedInputStream(input, 8192 * 12), "ASCII");

		if (startFromGB != 0) {
			it.next(); // da se poravna na novu liniju
		}

		long count = 0;
		HashSet<String> distinct = new HashSet<>();

		StopWatch s = new StopWatch();
		s.start();
		// Frequency f = new Frequency();

		CountTripletsFrequency frequency = new CountTripletsFrequency(pletsSize);
		try {
			while (it.hasNext()) {
				count++;
				String string = (String) it.next();
				String[] split = StringUtils.split(string, '\t');
				// double mass = Double.parseDouble(split[0].trim());
				// f.addValue(mass);
				String peptide = split[1].trim();
				// 3. accession_my_id

				frequency.add(peptide);

				// distinct.add(peptide);

				// BioUtil.calculateMassWidthH2O(peptide);

				if (count % 1_000_000 == 0) {
					// break;
					System.out.println(
							"Count " + nf.format(count) + " " + DurationFormatUtils.formatDurationHMS(s.getTime()));
				}

				if (stopAtNumber && count == stopAt) {
					break;
				}
				// System.out.println(peptide);
			}

		} finally {
			IOUtils.closeQuietly(input);
		}
		System.out.println("Unique peptida ima: " + distinct.size());
		System.out.println("Ukupno peptida: " + count);

		System.out.println(DurationFormatUtils.formatDurationHMS(s.getTime()));

		frequency.printStatistic();
	}

}

class CountTripletsFrequency {

	// private HashSet<String> triplets;
	private PermutationProteinTriplets permutationUtil;
	private HashMap<String, Integer> plets;
	private int sizePlest;

	public CountTripletsFrequency(int sizePlest) {
		this.sizePlest = sizePlest;
		permutationUtil = new PermutationProteinTriplets(sizePlest);
		ArrayList<String> pletsSet = permutationUtil.getAllProteinPermutation();
		plets = new HashMap<>(pletsSet.size());
		for (String tri : pletsSet) {
			plets.put(tri, 0);
		}
		System.out.println("Napravio " + sizePlest + "pletsa: " + plets.size());

	}

	public void add(String peptide) {
		ArrayList<String> t = PermutationProteinTriplets.getAllplets(peptide, sizePlest);
		for (String myTri : t) {
			// Integer res = triplets.get(myTri);
			plets.merge(myTri, 1, Integer::sum);
			// if (res == null) {
			// res = 1;
			// triplets.put(myTri, res);
			// } else {
			// res++;
			// }
		}
	}

	public void printStatistic() {

		// HashBag<String> n = new HashBag<>(22);
		// n.add

		HashMap<String, Integer> map = plets;
		Set<Entry<String, Integer>> set = map.entrySet();
		List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				// return (o1.getValue()).compareTo(o2.getValue());// Ascending order
				return (o2.getValue()).compareTo(o1.getValue());// Descending order
			}
		});
		for (Map.Entry<String, Integer> entry : list) {
			if (entry.getValue() == 0) {
				continue;
			}
			System.out.println(entry.getKey() + " ==== " + entry.getValue());
		}
	}
}
