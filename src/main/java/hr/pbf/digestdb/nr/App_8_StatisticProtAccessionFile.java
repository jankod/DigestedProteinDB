package hr.pbf.digestdb.nr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.Frequency;

import hr.pbf.digestdb.cli.IApp;
import hr.pbf.digestdb.util.BioUtil;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;

/**
 * Analize prot.accession2taxid
 * 
 * @author tag
 *
 */
public class App_8_StatisticProtAccessionFile implements IApp {

	@Override
	public void populateOption(Options o) {

	}

	static StopWatch s = new StopWatch();

	@Override
	public void start(CommandLine appCli) {
		s.start();
		Frequency frequencyAccessionLength = new Frequency();
		Frequency frequencyAccessionPrefix = new Frequency();
		int countLines = 0;
		// String line = null;
		MutableString line = new MutableString();
		try (FastBufferedReader reader = new FastBufferedReader(
				new FileReader("/home/users/tag/nr_db/prot.accession2taxid"))) {
			// try (BufferedReader reader =
			// BioUtil.newFileReader("/home/mysql-ib/nr_mass_sorted.csv")) {

			reader.readLine(line);
			while ((reader.readLine(line)) != null) {
				countLines++;

				String[] split = BioUtil.fastSplit(line.toString(), '\t');
				String accession = split[1]; // access verzion
				int taxID = Integer.parseInt(split[2]);
				// Arrays.ensureFromTo(arrayLength, from, to);
				frequencyAccessionLength.addValue(accession.length());
				frequencyAccessionPrefix.addValue(BioUtil.extractAccessionPrefix(accession));

				// BioUtil.removeVersionFromAccession(accession)

			}
		} catch (Throwable e) {
			System.err.println("Error on line: " + line);
			System.err.println("Error on line: " + countLines);
			e.printStackTrace();
		}

		s.stop();
		System.out.println(frequencyAccessionLength);
		System.out.println(frequencyAccessionPrefix);
		System.out.println("Count lines " + NumberFormat.getInstance().format(countLines));
		BioUtil.printTimeDurration(s);

	}

}
