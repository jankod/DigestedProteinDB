package hr.pbf.digestdb.test.probe;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.io.IOException;

import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import hr.pbf.digestdb.util.BiteUtil;

public class AccessionTaxIdDB {

	
	private DB db;

	public AccessionTaxIdDB(String dbPath) throws IOException {
		
		Options options = new Options();
		options.createIfMissing(true);

		options.cacheSize(100 * 1048576 * 10); // 100MB cache
		options.compressionType(CompressionType.SNAPPY);
		
		db = factory.open(new File(dbPath), options);
		
	}
	
	public void close() {
		
	}

	/**
	 * Postavlja na temelju US_ASCII
	 * @param key
	 * @param value
	 */
	public void put(String key, String value) {
		db.put(BiteUtil.toByte(key), BiteUtil.toByte(value));
	}
	
}
