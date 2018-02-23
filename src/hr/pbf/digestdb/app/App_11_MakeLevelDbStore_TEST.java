package hr.pbf.digestdb.app;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
//import static org.fusesource.leveldbjni.JniDBFactory.*;


import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;

import com.google.common.primitives.Ints;

import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.BiteUtil;

public class App_11_MakeLevelDbStore_TEST {

	public static void main(String[] args) throws IOException {
		Options options = new Options();
		options.comparator(new DBComparator() {

			@Override
			public int compare(byte[] key1, byte[] key2) {
				return Ints.compare(BiteUtil.toInt(key1), BiteUtil.toInt(key2));
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
		//options.createIfMissing(true);
		options.cacheSize(100 * 1048576 * 10); // 100MB * 10 cache
		options.compressionType(CompressionType.SNAPPY);
		options.verifyChecksums(false);
		options.blockSize(8 * 1024);
		options.paranoidChecks(false);

		String newDbPath = "/home/users/tag/nr_db/leveldb_mass_db";
		System.out.println("db: " + newDbPath);
		DB db = factory.open(new File(newDbPath), options);

		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		DBIterator it = db.iterator();

		double m1 = 1720.23456;
		double m2 = 1721.233345;
		
		m1 = 1000.23456;
		m2 = 1000.89765;
		
		
		
		byte[] m1Byte = BiteUtil.toByte((int) (m1 * 100_000));
		//it.seek(m1Byte);
		int c = 0;
		while (it.hasNext()) {
			c++;
			Entry<byte[], byte[]> entry = it.next();
			String ss = BiteUtil.toStringFromByte(entry.getValue());
			double mass = (BiteUtil.toInt(entry.getKey()) / 100_000);
			String[] split = StringUtils.split(ss, '_');
			double cMass = BioUtil.calculateMassWidthH2O(split[0]);
			System.out.println(mass + " "+ cMass+ " " + split[0] + " " + split[1]);
			if(mass > m2 * 100_000) {
				break;
			}
			if(c == 1_00) {
				break;
			}
		}
		
		it.close();
		
		stopWatch.stop();
		System.out.println("Count "+ c);
		System.out.println("Finish "+ DurationFormatUtils.formatDurationHMS(stopWatch.getTime()));
		db.close();

	}
}
