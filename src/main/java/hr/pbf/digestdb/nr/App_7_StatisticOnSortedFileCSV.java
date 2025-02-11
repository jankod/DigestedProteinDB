package hr.pbf.digestdb.nr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.StopWatch;

import hr.pbf.digestdb.cli.IApp;
import hr.pbf.digestdb.util.BioUtil;

/**
 * Parse sorted CSV file of mass. CSV: mass peptide accession
 * 
 * @author tag
 *
 */
public class App_7_StatisticOnSortedFileCSV implements IApp {

	@Override
	public void populateOption(Options o) {

	}

	static StopWatch s = new StopWatch();

	@Override
	public void start(CommandLine appCli) {
		s.start();
		long countTotalRow = 0;
		long countUniqueMass = 0;

		double lastMass = 0;

		String largestAccession = "";

		int countNotEndAccessionWithOne = 0;
		long countUniquePeptides = 0;

		long countUniquePeptidesLength = 0;
		HashSet<String> uniuePeptide = new HashSet<>();
		int countPRFaccessions = 0;

		NumberFormat f = NumberFormat.getInstance();
		/**
		 * First part of nonnumeric accession prefix
		 */
		HashSet<String> uniqueAccessionPrefix = new HashSet<>();

		ArrayList<String> accessionLargeerThan16 = new ArrayList<>();

		try (BufferedReader reader = BioUtil.newFileReader("/home/mysql-ib/nr_mass_sorted.csv")) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				// String[] split = StringUtils.split(line, "\t");
				String[] split = BioUtil.fastSplit(line, '\t');
				double mass = Double.parseDouble(split[0]);
				String peptide = split[1];
				String accession = split[2];

				countTotalRow++;

				if (lastMass != mass) {
					lastMass = mass;
					countUniqueMass++;

					countUniquePeptides += uniuePeptide.size();

					for (String p : uniuePeptide) {
						countUniquePeptidesLength += p.length();
					}

					uniuePeptide.clear();

				}
				uniuePeptide.add(peptide);

				if (largestAccession.length() < accession.length()) {
					largestAccession = accession;
				}

				if (!accession.endsWith(".1")) {
					countNotEndAccessionWithOne++;
				}

				if (accession.length() > 16) {
					accessionLargeerThan16.add(accession);
				}

				String prefix = BioUtil.extractAccessionPrefix(accession);
				if ("PRF".equalsIgnoreCase(prefix)) {
					countPRFaccessions++;
				}
				uniqueAccessionPrefix.add(prefix);

				if (countTotalRow % (1_000_000 * 50_000) == 0) {
					printAll(countTotalRow, countUniqueMass, largestAccession, countNotEndAccessionWithOne,
							countUniquePeptides, countUniquePeptidesLength, uniqueAccessionPrefix,
							accessionLargeerThan16, countPRFaccessions);
				}

			} // end while

		} catch (IOException e) {
			BioUtil.printTimeDurration(s);
			e.printStackTrace();
		}
		s.stop();
		printAll(countTotalRow, countUniqueMass, largestAccession, countNotEndAccessionWithOne, countUniquePeptides,
				countUniquePeptidesLength, uniqueAccessionPrefix, accessionLargeerThan16, countPRFaccessions);

	}

	static NumberFormat f = NumberFormat.getInstance();

	private void printAll(long countTotalRow, long countUniqueMass, String largestAccession,
			int countNotEndAccessionWithOne, long countUniquePeptides, long countUniquePeptidesLength,
			HashSet<String> uniqueAccessionPrefix, ArrayList<String> accessionLargeerThan16, int countPRFaccessions) {

		BioUtil.printTimeDurration(s);
		System.out.println("Total row " + f.format(countTotalRow));
		System.out.println("Unique mass " + f.format(countUniqueMass));
		System.out.println("Largest accession " + largestAccession);
		System.out.println("Number of accession without end with .1 " + f.format(countNotEndAccessionWithOne));
		System.out.println("Unique peptide " + f.format(countUniquePeptides));
		System.out.println("Unique peptide length " + f.format(countUniquePeptidesLength));
		System.out.println("Accession largest than 16 " + (accessionLargeerThan16));
		System.out.println("Unique accession prefix " + uniqueAccessionPrefix);
		System.out.println("Number of PRF accessions " + f.format(countPRFaccessions));
		System.out.println("==========================================================");
	}


}
