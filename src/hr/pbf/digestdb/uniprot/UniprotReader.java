package hr.pbf.digestdb.uniprot;

import static hr.pbf.digestdb.uniprot.A1_UniprotTextParser.ROUND_FLOAT_MASS;
import static hr.pbf.digestdb.util.BioUtil.calculateMassWidthH2O;
import static hr.pbf.digestdb.util.BioUtil.roundToDecimals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.BiteUtil;

public class UniprotReader {
	private static final Logger log = LoggerFactory.getLogger(UniprotReader.class);

	public static void main(String[] args) throws IOException {
		// Test that avare funcionirare
		// readKryo();
		readLevelDB("C:\\tmp\\delta_leveldb");

	}

	private static void readLevelDB(String path) throws IOException {

		LevelDbUniprot level = new LevelDbUniprot(path);
		// level.printStatus();
		DB db = level.getDb();

		long[] size = db.getApproximateSizes(new Range(BiteUtil.floatToByteArray(5), BiteUtil.floatToByteArray(60000)));
		System.out.println("Size approx "+ size[0]);

		DBIterator it = db.iterator();
		it.seekToFirst();
//		it.seek(BiteUtil.floatToByteArray(300f));
		int c = 0;
		while (it.hasNext()) {
			Map.Entry<byte[], byte[]> entry = (Map.Entry<byte[], byte[]>) it.next();
			byte[] v = entry.getValue();
			byte[] k = entry.getKey();

			List<PeptideAccTax> peptides = level.bytesToPeptides(v);
			float mass = BiteUtil.byteArrayToFloat(k);
//			log.debug("mass " + mass);
			for (PeptideAccTax peptideAccTax : peptides) {
//				log.debug(peptideAccTax.toString());
			}
			c++;

		}
		it.close();
		level.close();
		log.debug("Total float: "+ c);
		log.debug("finish");
	}

	private static void readKryo() throws IOException {
		String dir = "C:\\tmp\\delta-kryo";
		File[] listFiles = new File(dir).listFiles();
		for (File file : listFiles) {
			List<PeptideAccTax> res = A2_UniprotDeltaMassReader.fromKryo(file);
			String floatName = FilenameUtils.getBaseName(file.getName());
			float mass = BioUtil.roundToDecimals(Float.parseFloat(floatName), A1_UniprotTextParser.ROUND_FLOAT_MASS);

			// System.out.println("My mas: " + mass);
			for (PeptideAccTax p : res) {
				// System.out.println(p);
				float cm = (float) calculateMassWidthH2O(p.getPeptide());
				cm = roundToDecimals(cm, ROUND_FLOAT_MASS);
				if (mass != cm) {
					System.out.println(" GOT: " + mass + "--" + cm);
				}
			}
		}
	}
}
