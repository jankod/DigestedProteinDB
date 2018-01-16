package hr.pbf.digestdb.app;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.TimeScheduler;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;

public class App_11_MakeLevelDbStore {
	private static final Logger log = LoggerFactory.getLogger(App_11_MakeLevelDbStore.class);

	static int count = 0;

	public static void main(String[] args) throws IOException {

		TimeScheduler.runEveryHour(new Runnable() {
			@Override
			public void run() {
				log.debug("Došao do " + NumberFormat.getIntegerInstance().format(count));
			}
		});

		
		
		Options options = new Options();
		options.createIfMissing(true);
		options.cacheSize(100 * 1048576); // 100MB cache
		options.compressionType(CompressionType.SNAPPY);
		options.verifyChecksums(false);
		options.paranoidChecks(false);

		String newDbPath = "/home/users/tag/nr_db/leveldb_mass.db";
		System.out.println("db: " + newDbPath);
		DB db = factory.open(new File(newDbPath), options);

		MutableString line = new MutableString();
		try (FastBufferedReader reader = new FastBufferedReader(new FileReader("/home/mysql-ib/nr_mass_sorted.csv"))) {
			WriteBatch batch = db.createWriteBatch();
			while ((reader.readLine(line)) != null) {
				String[] split = StringUtils.split(line.toString(), '\t');
				double mass = Double.parseDouble(split[0]);
				String peptide = split[1].trim();
				String accVersion = split[2].trim();

				batch.put(ByteBuffer.allocate(4).putFloat((float) mass).array(),
						BiteUtil.toByte(peptide + "_" + accVersion));
				db.write(batch);

				count++;
				if (count % 1000 == 0) {
					log.debug("Sada sam na 1000");
				}
			}
			batch.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}
