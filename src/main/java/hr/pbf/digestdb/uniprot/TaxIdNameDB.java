package hr.pbf.digestdb.uniprot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;
import com.linkedin.paldb.api.StoreWriter;

import hr.pbf.digestdb.util.BioUtil;

/**
 * Standard name: taxNames.pal.db
 * 
 * @author tag
 *
 */
public class TaxIdNameDB {

	private static final Logger log = LoggerFactory.getLogger(TaxIdNameDB.class);
	private String pathDb;

	public TaxIdNameDB(String pathDb) {
		this.pathDb = pathDb;
	}

	public static void main(String[] args) throws IOException {
		String path = "F:\\tmp\\taxdump.tar\\names.dmp";

		TaxIdNameDB db = TaxIdNameDB.createFromNamesDMP(path, path + ".db");
		String taxName = db.get(21);
		log.debug("21 " + taxName);
		db.close();
		log.debug("Finish");
	}

	private void close() {
		if (reader != null) {
			reader.close();
		}
	}

	StoreReader reader;

	private String get(int taxId) {
		if (reader == null) {
			reader = PalDB.createReader(new File(pathDb));
		}
		return reader.getString(taxId);
	}

	/**
	 * Create db from names.dmp csv of ncbi taxonomy database.
	 * 
	 * @param pathCsv
	 * @param pathDb
	 * @return
	 * @throws IOException
	 */
	private static TaxIdNameDB createFromNamesDMP(String pathCsv, String pathDb) throws IOException {
		BufferedReader reader = BioUtil.newFileReader(pathCsv);
		// DB db = LevelDButil.open(pathLevelDb, LevelDButil.getOptions());
		StoreWriter w = PalDB.createWriter(new File(pathDb));

		String line = null;
		int c = 0;
		while ((line = reader.readLine()) != null) {
			String[] split = StringUtils.splitPreserveAllTokens(line, "|");
			// StringUtils.splitPreserveAllTokens(str)
			int taxId = Integer.parseInt(split[0].trim());
			String name = split[1].trim();
			if ("scientific name".equals(split[3].trim())) {
				c++;
				// System.out.println(taxId + " " + name);
				w.put(taxId, name);
			}
		}
		// db.close();
		w.close();

		return new TaxIdNameDB(pathDb);
	}
}
