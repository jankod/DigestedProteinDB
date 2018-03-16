package hr.pbf.digestdb.nr;

import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;

import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.MyLevelDB;
import hr.pbf.digestdb.util.TimeScheduler;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;

public class App_11_MakeLevelDbStore {
	private static final Logger log = LoggerFactory.getLogger(App_11_MakeLevelDbStore.class);

	static long count = 0;

	public static void main(String[] args) throws IOException {

		TimeScheduler.runEvery10Minutes(new Runnable() {
			@Override
			public void run() {
				log.debug("Došao do " + NumberFormat.getIntegerInstance().format(count));
			}
		});

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
		options.createIfMissing(true);
		options.cacheSize(100 * 1048576 * 10); // 100MB * 10 cache
		options.compressionType(CompressionType.SNAPPY);
		options.verifyChecksums(false);
		options.blockSize(8 * 1024);
		options.paranoidChecks(false);

		String newDbPath = "/home/users/tag/nr_db/leveldb_mass_db";
		System.out.println("db: " + newDbPath);
		DB db = MyLevelDB.open(newDbPath, options);

		MutableString line = new MutableString();
		boolean writen = false;
		double massLast = 0;
		try (FastBufferedReader reader = new FastBufferedReader(new FileReader("/home/mysql-ib/nr_mass_sorted.csv"))) {
			WriteBatch batch = db.createWriteBatch();
			while ((reader.readLine(line)) != null) {
				String[] split = StringUtils.split(line.toString(), '\t');
				double mass = Double.parseDouble(split[0]);
				
				String peptide = split[1].trim();
				String accVersion = split[2].trim();
				
				if(mass != massLast ) {
					writeBufferToDb();
				}
				addToSameBuffer(mass, peptide, accVersion);
				
				massLast = mass;
				
				

				// ByteBuffer.allocate(4).putFloat((float) mass).array()
				batch.put(BiteUtil.toBytes((int) (mass * 100_000)), BiteUtil.toBytes(peptide + "_" + accVersion));
				if (count % 400_000 == 0) {
					db.write(batch);
					batch.close();
					writen = true;
					batch = db.createWriteBatch();
				} else {
					writen = false;
				}

				count++;
			}
			if (!writen) {
				db.write(batch);
				batch.close();
				// batch = db.createWriteBatch();

			}
			batch.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		db.close();
		
	}

	private static void writeBufferToDb() {
		// TODO Auto-generated method stub
		
	}

	private static void addToSameBuffer(double mass, String peptide, String accVersion) {
		// TODO Auto-generated method stub
		
	}

}
