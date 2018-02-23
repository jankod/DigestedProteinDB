package hr.pbf.digestdb.app;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.SerializationUtils;
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
import hr.pbf.digestdb.util.MassRangeMap;

/**
 * Nakon sto se napravi veliki CSV sa podacima, masa=>peptid=>id ovaj kod
 * razbija na manje CSV filove po 0.3Da.
 * 
 * @author tag
 *
 */
public class App_3_CreateMenyFilesFromCSV implements IApp {
	private static final Logger log = LoggerFactory.getLogger(App_3_CreateMenyFilesFromCSV.class);

	String inputCsvPath = null; // "C:\\Eclipse\\OxygenWorkspace\\CreateNR\\misc\\sample_data\\850_000_nr_mass.csv";
	String folderResultPath = null; // "C:\\Eclipse\\OxygenWorkspace\\CreateNR\\misc\\sample_data\\small_store";

	private final static float DELTA = 0.3f;
	final static int fromMass = 500;
	final static int toMass = 6000;
	static final MassRangeMap massPartsMap = new MassRangeMap(DELTA, fromMass, toMass);

	public App_3_CreateMenyFilesFromCSV() {
		if (SystemUtils.IS_OS_WINDOWS) {
			inputCsvPath = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\850_000_nr_mass.csv";
			folderResultPath = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\small_store";
		} else {
			// LINUX
			inputCsvPath = "/home/mysql_data/mysql/nr_mass.csv";
			//inputCsvPath = "/home/tag/nr_db/ncbi/nr_mass_test.csv"; // TEST
			folderResultPath = "/home/tag/nr_db/mass_small_store";
		}
	}

	public static void main(String[] args) throws IOException {
	}

	@Override
	public void populateOption(Options o) {

	}

	static NumberFormat nf = NumberFormat.getInstance();

	public final static String MASS_PARTS_NAME = "mass_parts.txt";

	@Override
	public void start(CommandLine appCli) {
		log.debug("Params: Mass from: {}, to: {}", fromMass, toMass);
		log.debug("Params: result folder: {}", folderResultPath);
		log.debug("Params: read csv from {}", inputCsvPath);
		StopWatch s = new StopWatch();
		s.start();

		new File(folderResultPath).mkdirs();

		BufferedReader in = null;
		try {

			int threads = 52;
			ExecutorService ex = Executors.newFixedThreadPool(threads);
			Semaphore semaphore = new Semaphore(threads);

			in = BioUtil.newFileReader(inputCsvPath, null, 8192 * 34);
			LineIterator it = IOUtils.lineIterator(in);
			long count = 0;
			while (it.hasNext()) {
				count++;
				if (count % 70_000_000 == 0) {
					// break;
					System.out.println(
							"Count " + nf.format(count) + " " + DurationFormatUtils.formatDurationHMS(s.getTime()));
				}
				String line = (String) it.next();

				semaphore.acquire();
				ex.submit(new Runnable() {

					@Override
					public void run() {
						try {
							handleRow(line);
							semaphore.release();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});

			}
			ex.shutdown();
			ex.awaitTermination(7, TimeUnit.MINUTES);

			System.out.println("Count sequence total " + NumberFormat.getInstance().format(count));
			System.out.println("Duration: " + DurationFormatUtils.formatDurationHMS(s.getTime()));

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
			closeAllStreams();
		}
		File fileMassParts = new File(folderResultPath + "/" + MASS_PARTS_NAME);
		try (FileOutputStream outFF = new FileOutputStream(fileMassParts)) {
			SerializationUtils.serialize(massPartsMap, outFF);
			log.debug("file mass parts {}", fileMassParts);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleRow(String line) throws IOException {

		String[] split = StringUtils.split(line, '\t');
		// 1. mass
		float mass = (float) Double.parseDouble(split[0].trim());
		// 2. peptide
		String peptide = split[1].trim();
		// 3. accession_my_id
		String accessionID = split[2].trim();

		if (mass < fromMass || mass > toMass) {
			return;
		}
		// DataOutputStream out = rangeMap.get(mass);
		DataOutputStream out = getDataOutputStream(mass);
		writeRow(mass, peptide, accessionID, out);
	}

	private HashMap<String, DataOutputStream> massStreamMapp = new HashMap<>();

	private DataOutputStream getDataOutputStream(float mass) throws FileNotFoundException {
		String fileName = massPartsMap.getFileName(mass);
		synchronized (massStreamMapp) {
			if (massStreamMapp.containsKey(fileName)) {
				return massStreamMapp.get(fileName);
			}
		}

		DataOutputStream out = BioUtil.newDataOutputStream(folderResultPath + "/" + fileName + ".db", 8192 * 36); // 0.3
																													// MB
		synchronized (massStreamMapp) {
			massStreamMapp.put(fileName, out);
		}
		return out;

	}

	private void closeAllStreams() {
		Collection<DataOutputStream> values = massStreamMapp.values();
		for (DataOutputStream dataOutputStream : values) {
			try {
				dataOutputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			IOUtils.closeQuietly(dataOutputStream);
		}

	}

	public Row readRow(DataInputStream in) throws IOException {
		Row r = new Row();
		//r.mass = in.readDouble();
		r.accessionID = in.readUTF();
		r.peptide = in.readUTF();
		return r;
	}

	public static class Row {
		//public double mass;
		public String peptide;
		public String accessionID;
	}

	public void writeRow(double mass, String peptide, String accessionID, DataOutputStream out) throws IOException {

		synchronized (out) {
			// out.writeDouble(mass); NE VISE
			out.writeUTF(accessionID);
			out.writeUTF(peptide);
		}
	}

	public void deleteAllZeroFiles() {
		// NE RADI, DataOutputStream uvjek zapise par bytova...
		File[] dirs = new File(folderResultPath).listFiles();
		for (File file : dirs) {
			if (file.length() == 0) {
				System.out.println("Brisem : " + file.getName());
			} else {
				System.out.println("NE brisem " + file.getName());
			}
		}
	}

}
