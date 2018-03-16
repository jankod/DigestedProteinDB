package hr.pbf.digestdb.uniprot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.MyLevelDB;

public class A7_UniprotProtNamesToLevelDB {
	private static final Logger log = LoggerFactory.getLogger(A7_UniprotProtNamesToLevelDB.class);

	public static void main(String[] args) throws IOException {

		String pathCSV = "/home/users/tag/uniprot/uniprot_trembl.dat_prot_names1.csv";
		String pathStore = "/home/users/tag/uniprot/uniprot_trembl.mvstore";

		
		pathStore = "/home/users/tag/uniprot/uniprot_trembl_prot_names.leveldb";
		// saveToMVStore(pathCSV, pathStore);

		saveToLevelDB(pathCSV, pathStore);

	}

	private static void saveToLevelDB(String pathCSV, String pathStore) throws IOException {
		DB db = MyLevelDB.open(pathStore, getLevelDBOptions());

		log.debug("Start, path " + pathStore);
		long c = 0;
		boolean writenBatch = false;
		WriteBatch batch = db.createWriteBatch();
		try (BufferedReader reader = BioUtil.newFileReader(pathCSV, "ASCII")) {
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					String[] split = StringUtils.split(line, "\t");
					String acc = split[0].trim();

					String desc = removeEvidenceAtributes(split[1]);
					
					batch.put(acc.getBytes(StandardCharsets.US_ASCII), desc.getBytes(StandardCharsets.US_ASCII));
					
					if (c % 100 == 0) {
						db.write(batch);
						batch.close();
						writenBatch = true;
						batch = db.createWriteBatch();
					} else {
						writenBatch = false;
					}
					if (c % 100_000 == 0) {
						log.debug("sada sam na: " + c);
					}
					
					c++;
				}
				
				if (!writenBatch) {
					db.write(batch);
				}
				batch.close();
				
			} finally {
				db.close();
			}
			log.debug("Finish: " + c);
		}
	}

	private static void saveToMVStore(String pathCSV, String pathStore)
			throws IOException, UnsupportedEncodingException, FileNotFoundException {
		// open the store (in-memory if fileName is null)
		MVStore s = MVStore.open(pathStore);
		MVMap<String, String> map = s.openMap("proteins");
		log.debug("Start, path " + pathStore);
		long c = 0;
		try (BufferedReader reader = BioUtil.newFileReader(pathCSV, "ASCII")) {
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					// A0A2E7G0N2 Stress response translation initiation inhibitor YciH
					// {ECO:0000313|EMBL:MBN17319.1}
					String[] split = StringUtils.split(line, "\t");
					String acc = split[0].trim();

					String desc = removeEvidenceAtributes(split[1]);
					map.put(acc, desc);

					if (c % 1000 == 0) {
						s.commit();
					}
					if (c++ % 5000 == 0) {
						log.debug("Now is on: " + c);
					}
				}
			} catch (Throwable e) {
				log.error("Error on line: " + line, e);
			}
			s.commit();
			s.close();
			log.debug("Entered rows: " + c);
		}
	}

	/**
	 * Remove string like: https://web.expasy.org/docs/userman.html 2.4. Evidence
	 * attributions . {ECO:0000269|PubMed:10433554}
	 * 
	 * @param line
	 * @return
	 */
	public static String removeEvidenceAtributes(String line) {
		int ecoIndex = line.indexOf("{ECO:");
		if (ecoIndex == -1) {
			log.warn("Not find '{ECO:', line:  '" + line + "'");
			return line;
		}
		String desc = line.substring(0, ecoIndex);
		return desc.trim();
	}

	private static Options getLevelDBOptions() {
		Options options = new Options();
//		options.comparator(new DBComparator() {
//
//			@Override
//			public int compare(byte[] key1, byte[] key2) {
//				// return Ints.compare(BiteUtil.toInt(key1), BiteUtil.toInt(key2));
//				// return Float.compare(BiteUtil.toFloat(key1), BiteUtil.toFloat(key2));
//
//			}
//
//			@Override
//			public String name() {
//				return "ja-comparator";
//			}
//
//			@Override
//			public byte[] findShortestSeparator(byte[] start, byte[] limit) {
//				return start;
//			}
//
//			@Override
//			public byte[] findShortSuccessor(byte[] key) {
//				return key;
//			}
//		});
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
