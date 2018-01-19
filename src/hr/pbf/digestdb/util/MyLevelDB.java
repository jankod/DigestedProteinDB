package hr.pbf.digestdb.util;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;

public class MyLevelDB {

	private String path;
	private DB db;

	public MyLevelDB(String path) throws IOException {
		this.path = path;

		Options options = new Options();
		options.createIfMissing(true);

		options.cacheSize(100 * 1048576 * 10); // 100MB cache
		options.compressionType(CompressionType.SNAPPY);
		options.verifyChecksums(false);
		options.paranoidChecks(false);
		db = factory.open(new File(path), options);
	}

	public String getStatus() {
		return db.getProperty("leveldb.stats");
	}

	public void close() throws IOException {
		if (db != null) {
			db.close();
		}
	}
	
	
	/**
	 * Mora ga zatvoriti onda!
	 * @return
	 */
	public DBIterator getIterator() {
		return db.iterator();
	}

	/**
	 * Vraca -1 ako ne nadje nista.
	 * 
	 * @param key
	 * @return
	 */
	public int getInt(String key) {
		DBIterator i = db.iterator();
		byte[] bk = BiteUtil.toByte(key);
		i.seek(bk);
		if (i.hasNext()) {
			Entry<byte[], byte[]> res = i.next();
			
			if (res.getKey().equals(bk)) {
				return BiteUtil.toInt(res.getValue());
			}
		}
		try {
			i.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
