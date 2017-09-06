package hr.pbf.digestdb;

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

import hr.pbf.digestdb.util.AccessionMemoryDB;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.FastaSeq;
import hr.pbf.digestdb.util.MySQLdb;

public class PrepareNr_APP {

	public static final String ARG_APP_NAME = "nr";

	public static void printArgs() {
		System.out.println();
		System.out.println(PrepareNr_APP.class.getName());
		System.out.println("" + ARG_APP_NAME + " path");
		System.out.println();
	}

	static MySQLdb mysql;

	private static BufferedWriter seqeunceWriter;
	private static BufferedWriter proteinWriter;
	private static int threads = 12;
	private static long useSeqvences =  Long.MAX_VALUE;
	private static BufferedWriter massWriter;
	final static Semaphore semaphore = new Semaphore(threads);
	private static AccessionMemoryDB accessionDB;

	static boolean useMemoryDB = false;
	static long globalSeqID = 0; // Ima ih 127.050.501

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		BioUtil.printMemoryUsage("Prije baze: ");

		if (useMemoryDB) {
			accessionDB = AccessionMemoryDB.tryDeserializeSelf();
			if (accessionDB == null) {
				accessionDB = new AccessionMemoryDB("./accesion_num_exported.csv");
			}
		}
		BioUtil.printMemoryUsage("Poslje baze");
		BioUtil.printTimeDurration(stopWatch);

		mysql = new MySQLdb();
		if (!useMemoryDB)
			mysql.initDatabase(GlobalMainOld.DB_USER, GlobalMainOld.DB_PASSWORD, GlobalMainOld.DB_URL);
		String fastaPath = "C:\\Eclipse\\OxygenWorkspace\\CreateNR\\misc\\sample_data\\4000nr.txt";

		ExecutorService th = Executors.newFixedThreadPool(threads);

		if (args.length > 1) {
			fastaPath = args[1];
			System.out.println("Read: " + fastaPath);
		}

		String pathSequence = fastaPath + "_sequence.csv";
		System.out.println("Write to " + pathSequence);
		seqeunceWriter = BioUtil.newFileWiter(pathSequence, null);
		proteinWriter = BioUtil.newFileWiter(fastaPath + "_protein.csv", null);
		massWriter = BioUtil.newFileWiter(fastaPath + "_mass.csv", null);
		BioUtil.readLargeFasta(fastaPath, new BioUtil.Callback() {

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
		System.out.println("Finish all, seq ID: " + globalSeqID);
		System.out.println("Trajanje: " + DurationFormatUtils.formatDurationHMS(stopWatch.getTime()));
		System.out.println("SEQ num: " + globalSeqID);
	}

	protected static void insertDBprotein(long accessionNum, String protName, long seqID) throws IOException {
		String str = accessionNum + "\t" + seqID + "\t" + protName + "\n";
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

			if (useMemoryDB) {
				Long res = accessionDB.getAccessionNum(accession);
				if (res == null) {
					return -1;
				}
				return res;

			}

			Connection conn = mysql.getConnection();

			String sql = "SELECT accession_num FROM accession_taxid_gi WHERE accession = '"
					+ StringUtils.trimToEmpty(accession) + "'";
			Statement s = conn.createStatement();
			ResultSet res = s.executeQuery(sql);
			long accession_num = -1;
			if (res.next()) {
				accession_num = res.getLong("accession_num");
			} else {
				// System.err.println("Nisam nasao accession num: " + sql);
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
				// System.err.println("Nema "+ header);
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
			long accessionNum = getAccesionNumDB(accession);
			if (accessionNum == -1) {
				// Ako nema accessiona, znaci da ovaj je
				// ftp://ftp.ncbi.nih.gov/pub/taxonomy/accession2taxid/README

				// prot.accession2taxid.gz
				// TaxID mapping for live protein sequence records.
				// The second set of files contains accession to taxid mappings for dead
				// (suppressed or withdrawn) sequence records:
				continue;
				// System.out.println("Not find accession in DB. " + header);
			}

			usedHeader = true;
			String protName = extractProteinNameFromHeader(header);

			// System.out.println(accession + "\n" + protName + "\n" + header);

			insertDBprotein(accessionNum, protName, seqID);

			insertDBmasses(seq, accessionNum);
		}
		if (!usedHeader) {
			System.err.println("Necu unjeti: " + StringUtils.abbreviate(seq.header, 18));
		} else {
			insertDBSeq(seq.seq, seqID);
		}
	}

	// private static DecimalFormat df = new DecimalFormat("#.######");

	final private static void insertDBmasses(FastaSeq seq, long accessionNum) throws IOException {
		final Map<String, Double> massesDigest = BioUtil.getMassesDigest(seq.seq);
		Set<String> keySet = massesDigest.keySet();
		for (String peptide : keySet) {
			Double mass = massesDigest.get(peptide);
			String str = BioUtil.roundToDecimals(mass, 7) + "\t" + peptide + "\t" + accessionNum + "\n";
			massWriter.write(str);
		}
	}
}