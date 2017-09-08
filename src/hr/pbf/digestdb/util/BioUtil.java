package hr.pbf.digestdb.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.iq80.snappy.SnappyFramedInputStream;
import org.iq80.snappy.SnappyFramedOutputStream;

public class BioUtil {

	public static final double PROTON_MASS = 1.007825;

	public static final double H2O = 18.0105646D;

	/**
	 * Ovo je stari caff, -2 protona, ili H2 zapravo.
	 */
	public static final double CAFF = 247.944929;

	/**
	 * Masa nova cistog CAF
	 */
	public static final double CAFF_NEW = 249.960579; // CAF + 2*PROTON

	/**
	 * H1 Za tolko je manja od CAFF-a
	 */
	public static final double MIRJANA_DELTA = 79.956817;

	/**
	 * full masa
	 */
	public static final double SPITC = 212.955434;

	/**
	 * Bivsa mirjana. Za tolko je manja od CAFF-a
	 */
	public static final double H1 = MIRJANA_DELTA;

	/**
	 * Veca od cafa za 50.0156500642.
	 */
	public static final double H3 = -50.0156500642;

	/**
	 * Nova prava vrijednost
	 */
	public static final double H3_NEW = 299.9762294;

	public static final double H4 = 325.9918794;

	public static final double Hplus = 1.007825;

	public static void readLargeFasta(String path, Callback callback) throws IOException {
		readLargeFasta(path, callback, Long.MAX_VALUE);
	}

	public final static Pattern ctrlAPattern = Pattern.compile("\\p{Cntrl}");

	/**
	 * Amino kis koje ne zelimo u bazi, ima ih malo.
	 */
	public static final char[] NEVALJALE_AA = new char[] { 'X', 'B', 'Z', 'J', 'U', 'O' };

	public static BufferedReader newFileReader(String path) throws UnsupportedEncodingException, FileNotFoundException {
		return newFileReader(path, null);
	}

	public static BufferedReader newFileReader(String path, String charset)
			throws UnsupportedEncodingException, FileNotFoundException {
		if (charset == null) {
			charset = "ASCII";
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), charset));
		return reader;
	}

	public static BufferedWriter newFileWiter(String path, String charset)
			throws UnsupportedEncodingException, FileNotFoundException {

		if (charset == null) {
			charset = "ASCII";
		}
		int BUFFER = 1024 * 1024 * 12; // 12 MB

		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path)), charset),
				BUFFER);
		return w;
	}

	////////////////////////////////////////////////////////////
	// COMPRESS
	////////////////////////////////////////////////////////////

	public static DataOutputStream newDataOutputStreamCompresed(String path) throws IOException {

		DataOutputStream out = new DataOutputStream(
				new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(path)))));
		return out;
	}

	
	/**
	 * Neznam zasto mora ici preko bytearrayoutputstream-a.
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static DataInputStream newDataInputStreamCompressed(String path) throws IOException {

		GZIPInputStream in = new GZIPInputStream(new FileInputStream(new File(path)));
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		IOUtils.copy(in, bytes);
		DataInputStream out = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));

		return out;
	}

	/////////////////////////////////////////////////////////////////////////////////////////// 77

	public static DataOutputStream newDataOutputStream(String path) throws FileNotFoundException {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(path))));
		return out;
	}

	public static DataInputStream newDataInputStream(String path) throws FileNotFoundException {
		DataInputStream out = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(path))));
		return out;
	}

	public static double roundToDecimals(double num, int dec) {
		return Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
	}

	public static float roundToDecimals(float num, int dec) {
		return (float) (Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec));
	}

	public static Map<String, Double> getMassesDigest(String seq) {
		final List<String> cepanice = tripsyn(seq, 5, 110); // 110 znakova je najvise jer ESI radi do 6000Da, a 6000 /
															// 57 ~ 110 aminoki kis.

		Map<String, Double> cepaniceMasa = new HashMap<String, Double>(cepanice.size());
		for (String cepa : cepanice) {
			// maknuti cepanice sa 'X', 'B', 'Z', 'J', 'U', 'O'
			if (StringUtils.containsAny(cepa, 'X', 'B', 'Z', 'J', 'U', 'O')) {
				continue;
			}

			// if (StringUtils.countMatches(cepa, "X") == 0) {
			// // Maknut cu sve B i Z
			//
			// // PeptideUtils.getCombinationStringWithX_B_Z(c, peptidi);
			// } // inace ignoriraj cepanice sa X-evima

			final double mass = calculateMassWidthH2O(cepa);
			if (mass < 6000) // ESI ide do 6000Da
				cepaniceMasa.put(cepa, mass);
		}
		return cepaniceMasa;
	}

	/**
	 * Postavlja sve seq u velika slova.
	 * 
	 * @param path
	 * @param callback
	 * @param koliko
	 * @throws IOException
	 */
	public static void readLargeFasta(String path, Callback callback, long koliko) throws IOException {
		// FileReader f = new FileReader(new File(path));
		// reader = new BufferedReader(new InputStreamReader(new
		// FileInputStream(csvFile), "Cp1252"));

		BufferedReader reader = null;
		try {

			long count = 1;

			reader = newFileReader(path, "ASCII");
			// BufferedReader reader = new BufferedReader(f);
			String line = null;
			StringBuilder seq = new StringBuilder(12345);
			String header = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(">")) {
					if (seq.length() != 0) {
						// header = ctrlAPattern.matcher(line).replaceAll(">");
						// header = header.replaceAll("\\p{Cntrl}", ">");
						FastaSeq fs = new FastaSeq(header, seq.toString().toUpperCase().trim());
						callback.evoTiFasta(fs);
						if (count == koliko) {
							System.err.println("ZAVRSIO nasilno na " + koliko + " sekvenci !!!");
							return;
						}
						// seq = new StringBuilder();
						seq.setLength(0);
						count++;

					}
					header = line;
				} else {
					seq.append(line);
				}

			}
			if (seq.length() != 0) {
				// header = ctrlAPattern.matcher(header).replaceAll(">");
				// header = header.replaceAll("\\p{Cntrl}", ">");
				FastaSeq fs = new FastaSeq(header, seq.toString().toUpperCase());
				callback.evoTiFasta(fs);
			}

		} finally {
			IOUtils.closeQuietly(reader);
		}

	}

	public static interface Callback {
		public void evoTiFasta(FastaSeq seq) throws IOException;
	}

	public static Set<FastaSeq> readFasta(String path, int num) throws IOException {
		int count = 0;
		FileInputStream in = new FileInputStream(new File(path));
		Set<FastaSeq> res = new HashSet<FastaSeq>();
		try {
			LineIterator lines = IOUtils.lineIterator(in, Charset.defaultCharset().name());
			StringBuilder seq = new StringBuilder();
			String header = null;
			while (lines.hasNext()) {
				// for (String line : lines) {
				String line = lines.nextLine();
				if (line.startsWith(">")) {
					if (count++ > num) {
						break;
					}
					if (seq.length() != 0) {
						FastaSeq fs = new FastaSeq(header, seq.toString());
						res.add(fs);
						seq = new StringBuilder();
					}
					header = line;
				} else {
					seq.append(line);

				}
			}
			if (seq.length() != 0) {
				FastaSeq fs = new FastaSeq(header, seq.toString().toUpperCase());
				res.add(fs);
			}

			return res;

		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private static final Pattern pattternGI = Pattern.compile(">GI\\|([0-9]*)\\|", Pattern.CASE_INSENSITIVE);

	/**
	 * Vraca samo jedan GI, iako mozda ima ih vise
	 * 
	 * @param line
	 * @return
	 */
	public static int findOneGI(String line) {
		Matcher res = pattternGI.matcher(line.toUpperCase());
		int gi = 0;
		while (res.find()) {
			gi = Integer.parseInt(res.group(1));
			return gi;

		}
		return gi;
	}

	public static ArrayList<Integer> findGIasList(String line) {
		ArrayList<Integer> intList = new ArrayList<Integer>();

		Matcher res = pattternGI.matcher(line);

		while (res.find()) {
			intList.add(Integer.parseInt(res.group(1)));
		}
		return intList;

	}

	public static String removeVersionFromAccession(String accession) {
		int i = accession.indexOf('.');
		if (i > 0) {
			return accession.substring(0, i);
		}
		return accession;
	}

	/**
	 * Super brza metoda za cepanje sa 1 miss clevage. Cepa iza R ili K ako poslje
	 * nije P. Moraju biti velika slova!
	 * 
	 * @param prot
	 * @param min
	 * @param max
	 * @return
	 */
	public static List<String> tripsyn(String prot, int min, int max) {
		final int numberOfRandK = StringUtils.countMatches(prot, "R") + StringUtils.countMatches(prot, "K");
		if (numberOfRandK == 0) {
			List<String> result = new ArrayList<String>(1);
			if (prot.length() >= min && prot.length() <= max)
				result.add(prot);
			return result;
		}
		List<String> result = new ArrayList<String>(numberOfRandK * 3 + 2);

		if (prot.length() < min) {
			// result.add(prot);
			return result;
		}

		String lastChunk = null;
		int lastPos = 0;

		for (int i = 0; i < prot.length(); i++) {
			final char charAt = prot.charAt(i);

			if (charAt == 'R' || charAt == 'K') {
				if (prot.length() > i + 1) {
					final char nextChar = prot.charAt(i + 1);
					if (nextChar == 'P') {
						continue;
					}
				}

				final String chunk = prot.substring(lastPos, i + 1);
				if (chunk.length() >= min && chunk.length() <= max) {
					result.add(chunk);
				}
				lastPos = i + 1;
				if (lastChunk != null) {
					int size = lastChunk.length() + chunk.length();
					if (size >= min && size <= max)
						result.add(lastChunk + chunk);
				}

				lastChunk = chunk;
			}

		}

		// dodati jos zadnji chuk
		if (lastPos < prot.length()) {
			final String ostatak = prot.substring(lastPos);
			if (lastChunk != null) {
				String n = lastChunk + ostatak;
				if (n.length() >= min && n.length() <= max) {
					result.add(n);
				}
				// i sami komad zadnji staviti
				if (ostatak.length() >= min && ostatak.length() <= max) {
					result.add(ostatak);
				}

			} else {
				String n = ostatak;
				if (n.length() >= min && n.length() <= max) {
					result.add(n);
				}
			}
		}

		// dodati miss clevage

		return result;
	}

	/**
	 * Ovo je masa koja se nalazi u bazi! Ovu masu uzima kao sto i inace racuna masu
	 * peptida drugi libovi. Novo brzo izracunavanje mase + voda 18.01
	 * 
	 * @param peptide
	 * @return
	 */
	public static final double calculateMassWidthH2O(final String peptide) {
		float h = 0;
		for (int i = 0; i < peptide.length(); i++) {
			h += getMassFromAAfast(peptide.charAt(i));
		}
		return h + H2O;
	}

	public static final double getMassFromAAfast(final char aa) {
		switch (aa) {
		case 'G':
			return 57.021463724D;
		// return 57;
		case 'A':
			return 71.03711379D;
		case 'S':
			return 87.03202841D;
		case 'P':
			return 97.05276385D;
		case 'V':
			return 99.06841392D;
		case 'T':
			return 101.04767847D;
		case 'C':
			return 103.00918448D;
		case 'I':
			return 113.08406398D;
		case 'L':
			return 113.08406398D;
		case 'N':
			return 114.04292745D;
		case 'D':
			return 115.02694303D;
		case 'Q':
			return 128.05857751D;
		case 'K':
			return 128.09496302D;
		case 'E':
			return 129.0425931D;
		case 'M':
			return 131.04048461D;
		case 'H':
			return 137.05891186D;
		case 'F':
			return 147.0684139162D;
		case 'R':
			return 156.10111103D;
		case 'Y':
			return 163.06332854D;
		case 'W':
			return 186.07931295D;
		case 'U':
			return 150.95363559D;
		case 'O':
			return 114.0793129535D;
		case 'J':
			return 113.08406398D;
		default:
			// log.error("Koji je ovo aa: '{}'", aa);
			throw new RuntimeException("Wrong AA " + aa);
			// return '?';
		}
	}

	public static void printMemoryUsage(String msg) {
		int gb = 1024 * 1024 * 1024;
		long mem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / gb;

		System.out.println(msg + ". Memory:  " + mem + " GB");
	}

	public static void printTimeDurration(StopWatch stopWatch) {
		System.out.println("Time duration: " + DurationFormatUtils.formatDurationHMS(stopWatch.getTime()));
	}

}
