package hr.pbf.digestdb.app;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import hr.pbf.digestdb.cli.IApp;
import hr.pbf.digestdb.util.BioUtil;

public class CreateSmallMenyFilesDBapp implements IApp {
	private static final Logger log = LoggerFactory.getLogger(CreateSmallMenyFilesDBapp.class);

	String pathCsv = null; // "C:\\Eclipse\\OxygenWorkspace\\CreateNR\\misc\\sample_data\\850_000_nr_mass.csv";
	String folderPath = null; // "C:\\Eclipse\\OxygenWorkspace\\CreateNR\\misc\\sample_data\\small_store";

	private final double DELTA = 0.3;
	final double fromMass = 500;
	final double toMass = 1000;

	public CreateSmallMenyFilesDBapp() {
		if (SystemUtils.IS_OS_WINDOWS) {
			pathCsv = "C:\\Eclipse\\OxygenWorkspace\\CreateNR\\misc\\sample_data\\850_000_nr_mass.csv";
			folderPath = "C:\\Eclipse\\OxygenWorkspace\\CreateNR\\misc\\sample_data\\small_store";
		} else {
			// LINUX
			pathCsv = "/home/mysql_data/mysql/nr_mass.csv";
			folderPath = "/home/tag/nr_db/mass_small_store";
		}
	}

	public static void main(String[] args) throws IOException {
		CreateSmallMenyFilesDBapp app = new CreateSmallMenyFilesDBapp();
		app.readAllFilea(app.folderPath + "\\5704.5-5704.8.db");
	}

	private void readAllFilea(String path) throws IOException {
		DataInputStream in = BioUtil.newDataInputStream(path);
		while (in.available() > 0) {
			System.out.println(in.readDouble() + "," + in.readLong() + "," + in.readUTF());
		}
	}

	@Override
	public void populateOption(Options o) {

	}

	static NumberFormat nf = NumberFormat.getInstance();

	@Override
	public void start(CommandLine appCli) {
		log.debug("Params: Mass from: {}, to: {}", fromMass, toMass);
		log.debug("Params: dest forlder: {}", folderPath);
		log.debug("Params: read csv from {}", pathCsv);
		StopWatch s = new StopWatch();
		s.start();

		new File(folderPath).mkdirs();

		BufferedReader in = null;
		try {
			populateRangeMap();

			in = BioUtil.newFileReader(pathCsv);
			LineIterator it = IOUtils.lineIterator(in);
			int count = 0;
			while (it.hasNext()) {
				count++;
				String string = (String) it.next();
				String[] split = StringUtils.split(string, '\t');
				double mass = Double.parseDouble(split[0].trim());
				// f.addValue(mass);
				String peptide = split[1].trim();
				// 3. accession_my_id
				long accessionID = Long.parseLong(split[2].trim());

				if (count % 10_000_000 == 0) {
					// break;
					System.out.println(
							"Count " + nf.format(count) + " " + DurationFormatUtils.formatDurationHMS(s.getTime()));
				}
				if (mass < fromMass || mass > toMass) {
					continue;
				}
				DataOutputStream out = rangeMap.get(mass);
				writeRow(mass, peptide, accessionID, out);

			}

			System.out.println("Count sequence ukupno " + count);
			System.out.println("Duration: " + DurationFormatUtils.formatDurationHMS(s.getTime()));

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(in);
			closeAllStreams();
		}
	}

	RangeMap<Double, DataOutputStream> rangeMap = TreeRangeMap.create();

	private void populateRangeMap() throws FileNotFoundException {
		for (double i = fromMass; i < toMass; i = i + DELTA) {
			double from = BioUtil.roundToDecimals(i, 1);
			double to = BioUtil.roundToDecimals(i + DELTA, 1);
			Range<Double> r = Range.closed(from, to);
			rangeMap.put(r, BioUtil
					.newDataOutputStream(folderPath + "/" + r.lowerEndpoint() + "-" + r.upperEndpoint() + ".db"));
		}
	}

	private void closeAllStreams() {

		Collection<DataOutputStream> values = rangeMap.asMapOfRanges().values();
		for (DataOutputStream dataOutputStream : values) {
			IOUtils.closeQuietly(dataOutputStream);
		}
	}

	public Row readRow(DataInputStream in) throws IOException {
		Row r = new Row();
		r.mass = in.readDouble();
		r.accessionID = in.readLong();
		r.peptide = in.readUTF();
		return r;
	}

	public static class Row {
		public double mass;
		public String peptide;
		public long accessionID;
	}

	public void writeRow(double mass, String peptide, long accessionID, DataOutputStream out) throws IOException {

		out.writeDouble(mass);
		out.writeLong(accessionID);
		out.writeUTF(peptide);
	}

}
