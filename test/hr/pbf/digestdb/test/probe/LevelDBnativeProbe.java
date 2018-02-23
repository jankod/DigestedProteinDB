package hr.pbf.digestdb.test.probe;

import org.iq80.leveldb.*;

import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.CallbackMass;
import hr.pbf.digestdb.util.MassCSV;

//import static org.fusesource.leveldbjni.JniDBFactory.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;

import java.io.*;

public class LevelDBnativeProbe {

	public static void main(String[] args) throws IOException {
		Options options = new Options();
		options.createIfMissing(true);

		options.cacheSize(100 * 1048576 * 10); // 100MB cache
		options.compressionType(CompressionType.SNAPPY);
		options.verifyChecksums(false);
		options.paranoidChecks(false);

		DB db = factory.open(new File("c:/tmp/leveldb_jni"), options);
		try {

			MassCSV csv = new MassCSV("C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\nr_mass_sorted_200_000.csv");
			WriteBatch batch = db.createWriteBatch();
			csv.startIterate(new CallbackMass() {
				@Override
				public void row(double mass, String accVersion, String peptide) {
				}
			});
			

		} finally {
			// Make sure you close the db to shutdown the
			// database and avoid resource leaks.
			db.close();
		}
	}
}
