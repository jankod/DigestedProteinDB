package hr.pbf.digestdb.test.experiments;

import java.io.IOException;

import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.LevelDButil;

public class AccessionTaxIdDB {

	
	private DB db;

	public AccessionTaxIdDB(String pathDB) throws IOException {
		
		Options options = new Options();
		options.createIfMissing(true);

		options.cacheSize(100 * 1048576 * 10); // 100MB cache
		options.compressionType(CompressionType.SNAPPY);
		
		db = LevelDButil.open(pathDB, options);
		
	}
	
	public void close() {
		
	}

	/**
	 * Postavlja na temelju US_ASCII
	 * @param key
	 * @param value
	 */
	public void put(String key, String value) {
		db.put(BiteUtil.toBytes(key), BiteUtil.toBytes(value));
	}
	
}
