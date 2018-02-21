package hr.pbf.digestdb.uniprot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastOutput;
import com.google.common.collect.TreeRangeMap;

import hr.pbf.digestdb.uniprot.UniprotModel.CallbackUniprotReader;
import hr.pbf.digestdb.uniprot.UniprotModel.EntryUniprot;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MassRangeMap;
import hr.pbf.digestdb.util.TimeScheduler;

/**
 * Doc text formata: https://web.expasy.org/docs/userman.html
 *
 */
public class A1_UniprotTextParser {
	private static final Logger log = LoggerFactory.getLogger(A1_UniprotTextParser.class);

	private static long c = 0;

	private static String mainfolder = "F:\\Downloads\\uniprot";
	static String deltaDbPath;
	// private static String taxidLevelDBpath = mainfolder + "\\taxid_level.db";
	// private static String accLevelDBpath = mainfolder + "\\acc_level.db";
	static BufferedWriter outProt;

	public static void main(String[] args) throws IOException {
		long maxRows = 1000;
		if (SystemUtils.IS_OS_LINUX) {
			mainfolder = "/home/users/tag/uniprot";
			maxRows = Long.MAX_VALUE;
		}
		deltaDbPath = mainfolder + File.separator + "delta-db";

		File d = new File(deltaDbPath);
		FileUtils.deleteDirectory(d);
		d.mkdirs();
		TimeScheduler.runEvery10Minutes(new Runnable() {

			@Override
			public void run() {
				log.debug("Working progress: " + NumberFormat.getIntegerInstance().format(c));
			}
		});
		String outProtPath = mainfolder + File.separator + "result_prot_names.csv";
		// log.debug(outProtPath);
		outProt = BioUtil.newFileWiter(outProtPath, "ASCII");

		String db = "uniprot_trembl.dat";
		// db = "uniprot_sprot.dat";
		log.debug("db " + db);
		readUniprotTextLarge(mainfolder + File.separator + db, e -> {

			try {

				writeMass(e);
				writeProteNames(e);
				c++;
			} catch (Throwable e1) {
				e1.printStackTrace();
			}

		}, maxRows);

		IOUtils.closeQuietly(outProt);
		for (DataOutputStream out : massStreamMap.values()) {
			IOUtils.closeQuietly(out);
		}
		TimeScheduler.stopAll();
	}

	private static void writeProteNames(EntryUniprot e) throws IOException {
		outProt.write(e.getAccession() + "\t" + e.getProtName() + "\n");
	}

	/**
	 * taxid, tax_desc -> CSV
	 * 
	 * @param e
	 */
	private static void writeTax_Desc(EntryUniprot e) {
		// outProt.write(e.getTax() +"\t"+e.);
	}

	private final static double DELTA = 0.3;
	final static int fromMass = 500;
	final static int toMass = 6000;
	static final MassRangeMap massPartsMap = new MassRangeMap(DELTA, fromMass, toMass);
	private static HashMap<String, DataOutputStream> massStreamMap = new HashMap<>();

	private static void writeMass(EntryUniprot e) throws IOException {
		Map<String, Double> massesMapp = BioUtil.getMassesDigest(e.getSeq().toString());
		reduceMasses(massesMapp, fromMass, toMass);

		Set<Entry<String, Double>> entrySet = massesMapp.entrySet();
		for (Entry<String, Double> peptideMass : entrySet) {
			Double mass = peptideMass.getValue();
			String fileName = massPartsMap.getFileName(mass);
			DataOutputStream out;
			if (massStreamMap.containsKey(fileName)) {
				out = massStreamMap.get(fileName);
			} else {
				out = BioUtil.newDataOutputStream(deltaDbPath + File.separator + fileName + ".db", 8192);
				massStreamMap.put(fileName, out);
			}

			// out.writeDouble(mass);
			String peptide = peptideMass.getKey();
			out.writeUTF(peptide); 
			out.writeInt(e.getTax());
			out.writeUTF(e.getAccession());

		}

	}

	private static void reduceMasses(Map<String, Double> masses, int fromMass, int toMass) {
		Iterator<String> it = masses.keySet().iterator();
		while (it.hasNext()) {
			Double m = masses.get(it.next());
			if (fromMass >= m || m >= toMass)
				it.remove();

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
