package hr.pbf.digestdb.util;

import static hr.pbf.digestdb.util.BiteUtil.toStringFromByte;
//import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
//import static org.fusesource.leveldbjni.JniDBFactory.*;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

public class LevelDButil {


	public static DB open(String pathDb, Options options) throws IOException {
		return Iq80DBFactory.factory.open(new File(pathDb), options);
	}
	
	public static DB open(String pathDb) throws IOException {
		return open(pathDb, getStandardOptions());
	}

	public static String getStatus(DB db) {
		return db.getProperty("leveldb.stats");
	}

	
	
	public static DBComparator getJaComparator() {
		DBComparator dbComparator = new DBComparator() {

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
		};
		return dbComparator;
		
	}
	/**
	 * Vraca -1 ako ne nadje nista.
	 * 
	 * @param key
	 * @return
	 */
//	public int getInt(String key) {
//		DBIterator i = db.iterator();
//		byte[] bk = BiteUtil.toBytes(key);
//		i.seek(bk);
//		if (i.hasNext()) {
//			Entry<byte[], byte[]> res = i.next();
//			System.out.println(toStringFromByte(res.getKey()) + " " + BiteUtil.toInt(res.getValue()));
//
//			if (res.getKey().equals(bk)) {
//				return BiteUtil.toInt(res.getValue());
//			}
//		}
//		try {
//			i.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return -1;
//	}

	public static Options getStandardOptions() {
		Options options = new Options();
		options.createIfMissing(true);
		options.cacheSize(100 * 1048576 * 10); // 100MB cache
		options.compressionType(CompressionType.SNAPPY);
		options.verifyChecksums(false);
		options.paranoidChecks(false);
		return options;
	}
}
