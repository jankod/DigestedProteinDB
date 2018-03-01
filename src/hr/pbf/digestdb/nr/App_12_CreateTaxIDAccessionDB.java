package hr.pbf.digestdb.nr;

import static hr.pbf.digestdb.util.BiteUtil.toByte;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
//import static org.fusesource.leveldbjni.JniDBFactory.*;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.TimeScheduler;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;

/**
 * Radi accession->taxid DB. Uzima
 * ftp://ftp.ncbi.nih.gov/pub/taxonomy/accession2taxid/ dead and live accession.
 * 
 * @author tag
 *
 */
public class App_12_CreateTaxIDAccessionDB {
	private static final Logger log = LoggerFactory.getLogger(App_12_CreateTaxIDAccessionDB.class);

	private static long count = 0;

	public static void main(String[] args) throws IOException {

		 leveldb();
		//mvstore();

	}

	private static void mvstore() {

	}

	private static void leveldb() throws IOException {
		Options options = new Options();
		options.createIfMissing(true);

		options.cacheSize(100 * 1048576 * 10); // 100MB cache
		options.compressionType(CompressionType.SNAPPY);
		//options.verifyChecksums(false);
		//options.paranoidChecks(true);

		String newDbPath = "/home/users/tag/nr_db/leveldb_accession2taxid_dead";
		String accessionTaxIdPath = "/home/users/tag/nr_db/dead_prot.accession2taxid";
		System.out.println("db: " + newDbPath);
		DB db = factory.open(new File(newDbPath), options);

		TimeScheduler.runEvery10Minutes(new Runnable() {

			@Override
			public void run() {
				log.debug("Došao do " + NumberFormat.getIntegerInstance().format(count));
				log.debug("Status " + db.getProperty("leveldb.stats"));
			}
		});

		MutableString line = new MutableString();
		try (FastBufferedReader reader = new FastBufferedReader(
				new FileReader(accessionTaxIdPath))) {
			WriteBatch batch = db.createWriteBatch();

			boolean batchWriten = false;
			reader.readLine(line); // header
			while ((reader.readLine(line)) != null) {
				String[] split = StringUtils.split(line.toString(), '\t');
				// accession \t accession.version \t taxid \t gi

				String acc = split[0].trim();
				int taxId = Integer.parseInt(split[2].trim());

				batch.put(toByte(acc), toByte(taxId));
				if (count % 400_000 == 0) {
					db.write(batch);
					batch.close();
					batch = db.createWriteBatch();
					batchWriten = true;
				} else {
					batchWriten = false;
				}

				count++;
			}
			if (!batchWriten) {
				db.write(batch);
				batch.close();
			}

			batch.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		db.close();

	}
}
