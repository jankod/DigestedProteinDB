package hr.pbf.digestdb.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;

import hr.pbf.digestdb.cli.IApp;
import hr.pbf.digestdb.util.BioUtil;
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
		// prepareNames();
		prepareNodes();

	}

	private void prepareNodes() {
		int count = 0;

		try {

			BufferedWriter out = BioUtil.newFileWiter("F:\\tmp\\taxdump.tar\\names.csv", "ASCII");

			MutableString line = new MutableString();
			try (FastBufferedReader reader = new FastBufferedReader(
					new FileReader("F:\\tmp\\taxdump.tar\\names.dmp"))) {
				while ((reader.readLine(line)) != null) {
					count++;
					String[] split = StringUtils.splitByWholeSeparator(line.toString(), "\t|\t");
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
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
							useRow(taxID, split[1], out);
						}
					} else if (split.length == 3) {
						if (split[2].contains("scientific name")) {
							useRow(taxID, split[1], out);
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

	private void useRow(int taxID, String sName, BufferedWriter out) throws IOException {
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
