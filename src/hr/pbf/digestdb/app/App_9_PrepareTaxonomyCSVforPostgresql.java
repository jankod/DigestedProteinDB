package hr.pbf.digestdb.app;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;

import com.google.common.base.Splitter;

import hr.pbf.digestdb.cli.IApp;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.BiteUtil;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;

public class App_9_PrepareTaxonomyCSVforPostgresql implements IApp {

	public static void main(String[] args) {
		App_9_PrepareTaxonomyCSVforPostgresql p = new App_9_PrepareTaxonomyCSVforPostgresql();
		p.start(null);
	}

	@Override
	public void populateOption(Options o) {
	}

	@Override
	public void start(CommandLine appCli) {
		 prepareNames();
		//prepareNodes();

	}

	private void prepareNodes() {
		int count = 0;

		MutableString line = new MutableString();
		try {

			String dbPath = "/";
			DB db = createLevelDB(dbPath);

			BufferedWriter out = BioUtil.newFileWiter("F:\\tmp\\taxdump.tar\\nodes.csv", "ASCII");

			WriteBatch batch = db.createWriteBatch();
			
			try (FastBufferedReader reader = new FastBufferedReader(
					new FileReader("F:\\tmp\\taxdump.tar\\nodes.dmp"))) {
				while ((reader.readLine(line)) != null) {
					count++;
					String[] split = StringUtils.splitByWholeSeparator(line.toString(), "\t|\t");
					int taxId = Integer.parseInt(split[0]);
					// if(split[1].equals(""))
					int parrentTaxId = Integer.parseInt(split[1]);
					int divisionId = Integer.parseInt(split[4]);
					// System.out.format("taxID=%d parrent=%d div=%s \n", taxId, parrentTaxId,
					// divisionId);

					out.write("\"" + taxId + "\",\"" + parrentTaxId + "\",\"" + divisionId + "\"\n");
					// if(count > 1111) {
					// break;
					// }
					
					//batch.put(BiteUtil.toByte(taxId), value)
				}
				System.out.println("Count " + count);
			}
			out.close();
		} catch (Throwable t) {
			System.out.println("ERROR line: " + count + ":" + line);
			t.printStackTrace();
		}
	}

	private DB createLevelDB(String dbPath) throws IOException {
		org.iq80.leveldb.Options options = new org.iq80.leveldb.Options();
		options.createIfMissing(true);
		options.cacheSize(100 * 1048576 * 6); // 100MB * 6cache

		DB db = factory.open(new File(dbPath), options);
		return db;
	}

	private void prepareNames() {
		/*
		 * 
		 * Taxonomy names file (names.dmp):
		 * 
		 * tax_id -- the id of node associated with this name
		 *
		 * name_txt -- name itself
		 *
		 * unique name -- the unique variant of this name if name not unique
		 *
		 * name class -- (synonym, common name, ...)
		 * 
		 * 
		 */
		int count = 0;

		try {

			String dbPath = "F:/tmp/taxdump.tar/";
			//DB db = createLevelDB(dbPath);
			//WriteBatch batch = db.createWriteBatch();
			BufferedWriter out = BioUtil.newFileWiter("F:\\tmp\\taxdump.tar\\names.csv", "ASCII");

			MutableString line = new MutableString();
			try (FastBufferedReader reader = new FastBufferedReader(
					new FileReader("F:\\tmp\\taxdump.tar\\names.dmp"))) {
				while ((reader.readLine(line)) != null) {
					count++;
					String[] split = StringUtils.splitByWholeSeparator(line.toString(), "\t|\t");

					// System.out.println(split.length + ": " + Arrays.toString(split));
					int taxID = Integer.parseInt(split[0]);

					if (split.length == 4) {
						if (split[3].contains("scientific name")) {
							printRowNames(taxID, split[1], out);
						}
					} else if (split.length == 3) {
						if (split[2].contains("scientific name")) {
							printRowNames(taxID, split[1], out);
						}
					} else {
						System.err.println("ERROR line " + line);
					}

				}
			}
			System.out.println("Lines: " + count);
			System.out.println("Max length sName " + maxSnameLength);
			out.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	private static int maxSnameLength = 0;

	private void printRowNames(int taxID, String sName, BufferedWriter out) throws IOException {
		boolean s = false;
		if (sName.contains("\"")) {
			s = true;
			System.out.println(sName);
		}

		sName = sName.replaceAll("\"", "\"\"");

		if (s) {
			System.out.println(sName);
		}

		// RADI:
		// "F:\\Program Files (x86)\\pgAdmin 4\\v2\\runtime\\psql.exe" --command "
		// "\\copy public.tax_names (taxid, name) FROM
		// 'F:/tmp/taxdump.tar/names.csv' DELIMITER ',' CSV QUOTE '\"' ESCAPE '\"';""

		out.write("\"" + taxID + "\",\"" + sName + "\"\n");
		// maxSnameLength = Math.max(maxSnameLength, sName.length());
		// System.out.println(taxID + " " + sName);
	}

}
