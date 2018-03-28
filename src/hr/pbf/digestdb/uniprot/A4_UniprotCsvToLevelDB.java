package hr.pbf.digestdb.uniprot;

import static hr.pbf.digestdb.util.BiteUtil.toBytes;

//import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.Range;
import org.iq80.leveldb.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideMassAccTaxList;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.LevelDButil;
import hr.pbf.digestdb.util.UniprotConfig;
import hr.pbf.digestdb.util.UniprotConfig.Name;

public class A4_UniprotCsvToLevelDB {
	private static final Logger	log			= LoggerFactory.getLogger(A4_UniprotCsvToLevelDB.class);
	static boolean				writenBatch	= false;
	static long					count		= 0;
	static byte[]				buffer		= new byte[1024 * 128 * 16];
	private static WriteBatch	batch;

	private static TreeMap<String, List<AccTax>> pepatidesOfOneMass = new TreeMap<>();
	// private static String pathCsv;
	// private static String pathDb;

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		String pathCsv = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_test.csv";
		String pathDb = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_text.leveldb";
		if (SystemUtils.IS_OS_LINUX) {
			pathCsv = "/home/users/tag/uniprot/trembl.csv";
			pathDb = "/home/users/tag/uniprot/trembl.leveldb";

		}

		StopWatch s = new StopWatch();
		s.start();
		// createLevelDB(pathCsv, pathDb);
		// searchLevelDB(500.1f, 501f);
		// System.out.println((float) BioUtil.calculateMassWidthH2O("AHGNC"));
		createIndexfromLeveldb(UniprotConfig.get(Name.PATH_TREMB_LEVELDB));

		s.stop();
		log.debug("Traje: "+ DurationFormatUtils.formatDurationHMS(s.getTime()));

	}

	public static void searchMain(String[] args) throws NumberFormatException, IOException {
		String[] split = StringUtils.split(args[0], ":");
		searchLevelDB(Float.parseFloat(split[0]), Float.parseFloat(split[1]));
	}

	static float lastMass = -1;

	/**
	 * Na linuxu napraio 13.353.898 komada
	 * 
	 * @param pathDb
	 * @param pathCsv
	 * 
	 * @throws IOException
	 */
	private static void createLevelDB(String pathCsv, String pathDb) throws IOException {
		// UniprotCSVformat csv = new UniprotCSVformat(pathCsv);

		Options options = getLevelDBOptions();

		DB db = LevelDButil.open(pathDb, options);
		batch = db.createWriteBatch();
		log.debug("start");

		PeptideMassAccTaxList result = null;
		boolean writenLastResult = false;
		try (BufferedReader reader = BioUtil.newFileReader(pathCsv, "ASCII")) {
			String line = null;
			while ((line = reader.readLine()) != null) {

				result = UniprotCSVformat.parseCSVLine(line);

				saveToMemory(result);
				if (lastMass == -1 || lastMass == result.getMass()) {
					writenLastResult = false;
				} else {
					write(db, lastMass);
					writenLastResult = true;
					pepatidesOfOneMass.clear();
				}
				lastMass = result.getMass();
			}
		}
		if (!writenLastResult) {
			write(db, lastMass);
			log.debug("Jos write " + result.getMass());
		}

		if (!writenBatch) {
			db.write(batch);
			batch.close();
		}
		batch.close();

		log.debug("resume compaction");
		db.resumeCompactions();
		log.debug("finish resume compaction");

		db.close();
		log.debug("Finish " + count + " last mass: " + lastMass);

	}

	private static void write(DB db, float mass) throws IOException {

		byte[] massKey = BiteUtil.toBytes(mass);

		byte[] compressPeptidesTaxAccs = UniprotFormat3.compressPeptidesJava(pepatidesOfOneMass);
		// byte[] bytesValue = compress(result);

		batch.put(massKey, compressPeptidesTaxAccs);
		if (count % 50 == 0) {
			db.write(batch);
			batch.close();
			writenBatch = true;
			batch = db.createWriteBatch();
		} else {
			writenBatch = false;
		}

		if (count % 10_000 == 0) {
			log.debug("sada sam na: " + count);
		}
		count++;
	}

	protected static void saveToMemory(PeptideMassAccTaxList result) {
		if (pepatidesOfOneMass.containsKey(result.getPeptide())) {
			throw new RuntimeException("Something wrong, contain peptide: " + result.getPeptide());
		}
		// log.debug(result.getMass() + " "+ result.getPeptide() + " "+
		// result.getAccTaxs());
		pepatidesOfOneMass.put(result.getPeptide(), result.getAccTaxs());
	}

	public static void createIndexfromLeveldb(String path) throws IOException {
		log.debug("Pretrazujem sa: " + path);
		Options options = getLevelDBOptions();
		DB db = LevelDButil.open(path, options);

		DBIterator it = db.iterator();

		// float uniquer, => koliko peptida ima
		TreeMap<Float, Integer> index = new TreeMap<>();
		it.seekToFirst();
		int c = 0;
		while (it.hasNext()) {
			Entry<byte[], byte[]> entry = it.next();
			byte[] k = entry.getKey();
			byte[] v = entry.getValue();
			float mass = BiteUtil.toFloat(k);
			Map<String, List<AccTax>> res = UniprotFormat3.uncompressPeptidesJava(v);
			long indexValue = 0;
			Set<Entry<String, List<AccTax>>> entrySet = res.entrySet();
			for (Entry<String, List<AccTax>> entry2 : entrySet) {
				indexValue += entry2.getValue().size();
			}

			if (indexValue > Integer.MAX_VALUE) {
				log.warn("max value {} for mass {}", indexValue, mass);
			}
			index.put(mass, (int) indexValue);

			// Set<Entry<String, List<AccTax>>> entrySet = res.entrySet();
			// for (Entry<String, List<AccTax>> entry2 : entrySet) {
			// System.out.println(mass + "\t" + entry2.getKey() + "\t" +
			// entry2.getValue().size());
			// }
			if (c++ > 2) {
				it.close();
				db.close();
				return;
			}
		}
		log.debug("Imam index: " + index.size());

		// 13.353.898 +
		// 107600000
		File fileIndex = new File(path + ".index");
		FileOutputStream fout = new FileOutputStream(fileIndex);
		SerializationUtils.serialize(index, fout);
		log.debug("seriajaliziran na " + fileIndex);
		fout.close();
		it.close();
		db.close();
	}

	private static void searchLevelDB(float from, float to) throws IOException {
		String pathDb;
		String pathCsv;
		pathCsv = UniprotConfig.get(Name.PATH_TREMBL_CSV);
		pathDb = UniprotConfig.get(Name.PATH_TREMB_LEVELDB);

		// JniDBFactory.pushMemoryPool(1024 * 512);
		Options options = getLevelDBOptions();
		DB db = LevelDButil.open(pathDb, options);
		StopWatch time = new StopWatch();
		time.start();

		DBIterator it = db.iterator();
		Range range = new Range(toBytes(from), toBytes(to));
		String stats = db.getProperty("leveldb.stats");
		log.debug("Status " + stats);
		long[] approximateSizes = db.getApproximateSizes(range);
		log.debug("Approximate Sizes " + FileUtils.byteCountToDisplaySize(approximateSizes[0]));
		log.debug("Search " + from + " " + to);

		// while (iterator.hasNext()) {
		// }

		it.seek(range.start());
		// maks 619.3581
		// iterator.seekToFirst();

		try {
			int c = 0;
			while (it.hasNext()) {
				Entry<byte[], byte[]> entry = it.next();
				byte[] k = entry.getKey(); // iterator.peekNext().getKey();

				byte[] v = entry.getValue(); // iterator.peekNext().getValue();
				float mass = BiteUtil.toFloat(k);
				if (mass > to) {
					log.debug("Finish on mass to: " + mass);
					break;
				}
				Map<String, List<AccTax>> res = UniprotFormat3.uncompressPeptidesJava(v);

				// log.debug(c + " : " + mass + " " + res.keySet() + " " + res.values());
				// log.debug("================" + mass + "");
				Set<Entry<String, List<AccTax>>> entrySet = res.entrySet();

				for (Entry<String, List<AccTax>> e : entrySet) {
					// log.debug(mass + " " + e.getKey() + " " + e.getValue());
					System.out.println(mass + " " + e.getKey() + " " + e.getValue());
				}
				c++;
			}
			it.close();
			time.stop();
			log.debug("Time search " + DurationFormatUtils.formatDurationHMS(time.getTime()));
			log.debug("Found unique mass: " + c);
		} finally {
			// Make sure you close the iterator to avoid resource leaks.

			db.close();
			// JniDBFactory.popMemoryPool();
		}
	}

	private static Options getLevelDBOptions() {
		Options options = new Options();
		options.comparator(new DBComparator() {

			@Override
			public int compare(byte[] key1, byte[] key2) {
				// return Ints.compare(BiteUtil.toInt(key1), BiteUtil.toInt(key2));
				return Float.compare(BiteUtil.toFloat(key1), BiteUtil.toFloat(key2));
			}

			@Override
			public String name() {
				return "ja-comparator";
			}

			@Override
			public byte[] findShortestSeparator(byte[] start, byte[] limit) {
				return start;
			}

			@Override
			public byte[] findShortSuccessor(byte[] key) {
				return key;
			}
		});
		options.createIfMissing(true);
		options.cacheSize(100 * 1048576 * 10); // 100MB * 10 cache
		options.compressionType(CompressionType.SNAPPY);
		options.verifyChecksums(false);
		options.blockSize(8 * 1024);
		options.paranoidChecks(false);
		// options.logger(new org.iq80.leveldb.Logger() {
		//
		// @Override
		// public void log(String message) {
		// System.out.println("LevelDB: " + message);
		// }
		//
		// });
		return options;
	}

}
