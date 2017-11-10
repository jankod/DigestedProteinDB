package hr.pbf.digestdb.app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import hr.pbf.digestdb.AppConstants;
import hr.pbf.digestdb.util.AccessionMemoryDB;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.FastaSeq;
import hr.pbf.digestdb.util.MySQLdb;

public class App_1_PrepareNr {

	public static final String ARG_APP_NAME = "nr";

	public static void printArgs() {
		System.out.println();
		System.out.println(App_1_PrepareNr.class.getName());
		System.out.println("" + ARG_APP_NAME + " path");
		System.out.println();
	}

	static MySQLdb mysql;

	private static BufferedWriter seqeunceWriter;
	private static BufferedWriter proteinWriter;
	private static int threads = 12;
	private static long useSeqvences = Long.MAX_VALUE;
	private static BufferedWriter massWriter;
	final static Semaphore semaphore = new Semaphore(threads);
	// private static AccessionMemoryDB accessionDB;

	static boolean useMemoryDB = false;
	static long globalSeqID = 0; // Ima ih 127.050.501
	// static String fastaNrPath =
	// "C:\\Eclipse\\OxygenWorkspace\\CreateNR\\misc\\sample_data\\4000nr.txt";
	
	static String fastaNrPath = "/home/tag/nr_db/ncbi";
	static String outSequenceCsvPath = fastaNrPath + "_sequence.csv";
	static String outProteinCsvPath = fastaNrPath + "_protein.csv";
	static String outMassCsvPath = fastaNrPath + "_mass.csv";

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		BioUtil.printMemoryUsage("Prije baze: ");

		mysql = new MySQLdb();
		mysql.initDatabase(AppConstants.DB_USER, AppConstants.DB_PASSWORD, AppConstants.DB_URL);

		ExecutorService th = Executors.newFixedThreadPool(threads);

		if (args.length > 1) {
			fastaNrPath = args[1];
			System.out.println("Read: " + fastaNrPath);
		}

		System.out.println("Write to " + outSequenceCsvPath);
		seqeunceWriter = BioUtil.newFileWiter(outSequenceCsvPath, null);

		proteinWriter = BioUtil.newFileWiter(outProteinCsvPath, null);

		massWriter = BioUtil.newFileWiter(outMassCsvPath, null);
		BioUtil.readLargeFasta(fastaNrPath, new BioUtil.Callback() {

			@Override
			public void evoTiFasta(FastaSeq seq) throws IOException {
				final String[] headers = BioUtil.ctrlAPattern.split(seq.header.replace('>', ' ').trim());
				// System.out.println("headers: " + headers.length);
				// System.out.println("Seq: " + seq.seq.length());

				globalSeqID++;

				try {
					semaphore.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				final long mySeqID = globalSeqID;
				th.execute(new Runnable() {

					@Override
					public void run() {

						try {
							processOneSeq(stopWatch, seq, headers, mySeqID);
						} catch (Throwable e) {
							throw new RuntimeException(e);
						} finally {
							semaphore.release();
						}
					}
				});

			}
		}, useSeqvences);

		th.shutdown();
		th.awaitTermination(7, TimeUnit.MINUTES);

		IOUtils.closeQuietly(proteinWriter);
		IOUtils.closeQuietly(seqeunceWriter);
		IOUtils.closeQuietly(massWriter);
		mysql.closeDatabaase();

		BioUtil.printMemoryUsage("Poslje baze");

		System.out.println("Finish all, seq ID: " + globalSeqID);
		BioUtil.printTimeDurration(stopWatch);
		System.out.println("SEQ num: " + globalSeqID);
	}

	protected static void insertDBprotein(String accession, String protName, long seqID) throws IOException {
		String str = accession + "\t" + seqID + "\t" + protName + "\n";
		proteinWriter.write(str);
	}

	protected static void insertDBSeq(String seq, long seqID) {
		try {
			StringBuilder seqCache = new StringBuilder(seq.length() + 26);
			seqCache.setLength(0);
			// String sql = "INSERT INTO `sequence` (`seq`) VALUES (''); ";
			seqCache.append(seqID);
			seqCache.append('\t');
			seqCache.append(seq);
			seqCache.append('\n');
			seqeunceWriter.write(seqCache.toString());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/*
	 * MORA SE MAKNUTI VERSION iz accessiona jer inace ne radi. Vraca -1 ako ne
	 * nadje.
	 */
	protected static long getAccesionNumDB(String accession) {
		try {

			accession = BioUtil.removeVersionFromAccession(accession);

			Connection conn = mysql.getConnection();

			String sql = "SELECT accession_num FROM accession_taxid_gi WHERE accession = '"
					+ StringUtils.trimToEmpty(accession) + "'";
			Statement s = conn.createStatement();
			ResultSet res = s.executeQuery(sql);
			long accession_num = -1;
			if (res.next()) {
				accession_num = res.getLong("accession_num");
				System.err.println("Nisam nasao accession num: " + sql);
			}
			s.close();
			conn.close();
			return accession_num;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	protected static String extractProteinNameFromHeader(String header) {
		try {
			int spacePos = header.indexOf(' ');
			int parentheasPos = header.indexOf('[');
			if (parentheasPos < 0) {
				parentheasPos = header.length();
			}

			return header.substring(spacePos + 1, parentheasPos);
		} catch (StringIndexOutOfBoundsException e) {
			System.err.println("Error line: " + header);
			e.printStackTrace();
			return "prot name:???";

		}
	}

	protected static String extractAccessionFromHeader(String header) {
		try {
			int spacePos = header.indexOf(' ');
			return header.substring(0, spacePos);
		} catch (StringIndexOutOfBoundsException e) {
			System.err.println("Error line: " + header);
			e.printStackTrace();
			return "acc:???";

		}
	}

	private static void processOneSeq(StopWatch stopWatch, FastaSeq seq, final String[] headers, long seqID)
			throws IOException {
		if (seqID % 100000 == 0) {
			System.out.println("SEQ ID " + seqID);
			BioUtil.printTimeDurration(stopWatch);
		}

		boolean usedHeader = false;
		for (String header : headers) {
			String accession = extractAccessionFromHeader(header);
			String protName = extractProteinNameFromHeader(header);

			insertDBprotein(accession, protName, seqID);

			insertDBmasses(seq, accession);
		}
		if (!usedHeader) {
			System.err.println("Necu unjeti: " + StringUtils.abbreviate(seq.header, 18));
		} else {
			insertDBSeq(seq.seq, seqID);
		}
	}

	final private static void insertDBmasses(FastaSeq seq, String accession) throws IOException {
		final Map<String, Double> massesDigest = BioUtil.getMassesDigest(seq.seq);
		Set<String> keySet = massesDigest.keySet();
		for (String peptide : keySet) {
			Double mass = massesDigest.get(peptide);
			String str = BioUtil.roundToDecimals(mass, 7) + "\t" + peptide + "\t" + accession + "\n";
			massWriter.write(str);
		}
	}
}
