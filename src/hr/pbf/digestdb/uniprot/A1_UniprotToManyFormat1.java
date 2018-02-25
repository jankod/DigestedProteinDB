package hr.pbf.digestdb.uniprot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.uniprot.UniprotModel.CallbackUniprotReader;
import hr.pbf.digestdb.uniprot.UniprotModel.EntryUniprot;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MassRangeMap;
import hr.pbf.digestdb.util.TimeScheduler;

/**
 * Parse uniprot dat files and create folder of many "format1" files. Format1 is
 * format which store peptide-acc-tax. sorted by mass. Doc text formata:
 * https://web.expasy.org/docs/userman.html Make .db files
 *
 */
public class A1_UniprotToManyFormat1 {
	private static final Logger log = LoggerFactory.getLogger(A1_UniprotToManyFormat1.class);

	// private final static float DELTA = 0.3f;
	// final static int fromMass = 500;
	// final static int toMass = 6000;
	// static final MassRangeMap massRangeMap = new MassRangeMap(DELTA, fromMass,
	// toMass);
	// private static HashMap<String, DataOutput> massStreamMap = new HashMap<>();

	/**
	 * Round float mass before and after save peptide mass
	 */
	public static final int ROUND_FLOAT_MASS = 2;

	private static long counter = 0;

	private static String pathDirMain;
	public static String pathDirFormat1;
	private static BufferedWriter outProt;

	private static MassRangeFiles massRangeFiles;

	public static void main(String[] args) throws IOException {
		long maxEntry = 100_000; // Long.MAX_VALUE

		String db = "uniprot_sprot.dat";

		pathDirMain = "F:\\Downloads\\uniprot";

		if (SystemUtils.IS_OS_LINUX) {
			pathDirMain = "/home/users/tag/uniprot";
			maxEntry = Long.MAX_VALUE;
			db = "uniprot_trembl.dat";
			// db = "uniprot_sprot.dat";
		}

		pathDirFormat1 = pathDirMain + File.separator + db + "_format1";

		if (SystemUtils.IS_OS_WINDOWS) {
			pathDirFormat1 += "_" + maxEntry;
		}
		String outProtPath = pathDirMain + File.separator + db + "_prot_names.csv";

		massRangeFiles = new MassRangeFiles(500, 6000, 0.3f, "format1", pathDirFormat1);

		File d = new File(pathDirFormat1);
		FileUtils.deleteDirectory(d);
		d.mkdirs();
		TimeScheduler.runEvery10Minutes(new Runnable() {

			@Override
			public void run() {
				log.debug("Working progress: " + NumberFormat.getIntegerInstance().format(counter));
			}
		});

		// log.debug(outProtPath);
		outProt = BioUtil.newFileWiter(outProtPath, "ASCII");

		log.debug("db " + db);

		readUniprotTextLarge(pathDirMain + File.separator + db, e -> {

			try {

				writeMass(e);
				writeProteNames(e);
				counter++;
			} catch (Throwable e1) {
				e1.printStackTrace();
			}

		}, maxEntry);

		IOUtils.closeQuietly(outProt);

		massRangeFiles.closeAll();

		TimeScheduler.stopAll();
	}

	private static void writeProteNames(EntryUniprot e) throws IOException {
		outProt.write(e.getAccession() + "\t" + e.getProtName() + "\n");
	}

	private static void writeMass(EntryUniprot e) throws IOException {
		Map<String, Double> massesMapp = BioUtil.getMassesDigest(e.getSeq().toString());
		UniprotUtil.reduceMasses(massesMapp, massRangeFiles.getFromMass(), massRangeFiles.getToMass());

		Set<Entry<String, Double>> entrySet = massesMapp.entrySet();
		for (Entry<String, Double> peptideMass : entrySet) {
			float mass = peptideMass.getValue().floatValue();
			mass = BioUtil.roundToDecimals(mass, ROUND_FLOAT_MASS);

			DataOutput out = massRangeFiles.getOuput(mass);

			String peptide = peptideMass.getKey();
			UniprotUtil.writeOneFormat1(out, peptide, e.getTax(), e.getAccession());

		}

	}

	public static void readUniprotTextLarge(String path, CallbackUniprotReader callback) {
		readUniprotTextLarge(path, callback, Long.MAX_VALUE);
	}

	public static void readUniprotTextLarge(String path, CallbackUniprotReader callback, long howMany) {
		BufferedReader reader = null;
		try {

			EntryUniprot entry = null;
			long count = 0;
			reader = BioUtil.newFileReader(path, "ASCII");
			String line = null;

			while ((line = reader.readLine()) != null) {

				if (count == howMany) {

					log.info("Finish now on " + NumberFormat.getInstance().format(howMany) + " entries !!!");
					return;
				}

				if (line.startsWith("AC")) {
					addAccessions(line, entry);
					continue;
				}

				if (line.startsWith("OX")) {
					addTaxonomy(line, entry);
					continue;
				}
				if (line.startsWith("  ")) {
					addSequence(line, entry);
					continue;
				}

				if (line.startsWith("DE")) {
					addProtName(line, entry);
				}

				if (line.startsWith("//")) {
					callback.readEntry(entry);
				}
				if (line.startsWith("ID")) {
					entry = new EntryUniprot();
					count++;
				}

			}

			System.out.println("Finish all, readed: " + count);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	final static String prefixRecName = "DE   RecName: Full=";
	final static String prefixSubName = "DE   SubName: Full=";

	private static void addProtName(String line, EntryUniprot entry) {
		if (line.startsWith(prefixRecName)) {
			entry.setProtName(line.substring(prefixRecName.length(), line.length() - 1));
		} else if (line.startsWith(prefixSubName) && entry.getProtName() == null) {
			// maybe note have RecName nego SubName, trebl ima SubName uglavnom...
			entry.setProtName(line.substring(prefixSubName.length(), line.length() - 1));
		}
	}

	/**
	 * Ovako nesto: MAFSAEDVLK EYDRRRRMEA LLLSLYYPND RKLLDYKEWS PPRVQVECPK
	 * APVEWNNPPS
	 * 
	 */
	private static void addSequence(String line, EntryUniprot e) {

		String seq = StringUtils.remove(line, " ");
		e.getSeq().append(seq);
	}

	/**
	 * 
	 */
	public static void addTaxonomy(String line, EntryUniprot e) {

		try {
			int taxIt = UniprotParseUtil.parseTaxLine(line);
			e.setTax(taxIt);
		} catch (Throwable t) {
			log.error("Error on line: " + line, t);
		}

	}

	/**
	 * <code>
	 * AC Q16653; O00713; O00714; O00715; Q13054; Q13055; Q14855; Q92891;
	 * </code> ili <code>AC   P00321;</code> Only first use.
	 * 
	 */
	public static void addAccessions(String line, EntryUniprot e) {
		e.setAccession(UniprotParseUtil.parseFirstAccession(line));

	}

}
