package hr.pbf.digestdb.app;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MySQLdb;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;

public class App_10_AddTaxIdToCSV {

	public static void main(String[] args) throws SQLException, UnsupportedEncodingException, FileNotFoundException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		BufferedWriter out = BioUtil.newFileWiter("/home/mysql-ib/nr_mass_taxid_sorted.csv", "ASCII");

		String storeFileDb = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\mvstore.db";
		if (SystemUtils.IS_OS_LINUX) {
			storeFileDb = "/home/users/tag/nr_db/mvstore_accession_taxid.db";
		}
		MySQLdb mysql = new MySQLdb();
		mysql.initDatabase("root", "ja", "jdbc:mysql://localhost:5029/nrprot");
		Connection c = mysql.getConnection();

		MVStore s = new MVStore.Builder().cacheSize(512).fileName(storeFileDb).compress().open();
		MVMap<Object, Object> map = s.openMap("acc_taxid");

		// System.out.println("Size: "+ map.size());
		// System.out.println("Accession: "+ map.get("AAF27197.1"));
		if (map == null) {
			throw new NullPointerException("map");
		}
		c.setReadOnly(true);

		int count = 0;
		MutableString line = new MutableString();
		try (FastBufferedReader reader = new FastBufferedReader(new FileReader("/home/mysql-ib/nr_mass_sorted.csv"))) {
			while ((reader.readLine(line)) != null) {
				String[] split = StringUtils.split(line.toString(), '\t');
				double mass = Double.parseDouble(split[0]);
				String peptide = split[1].trim();
				String accVersion = split[2].trim();
				Integer taxId = (Integer) map.get(accVersion);
				if (taxId == null) {
					System.out.println("Not found acc: " + accVersion);
					continue;
				}
				Statement st = c.createStatement();
				ResultSet r = st.executeQuery("SELECT division_id FROM ncbi_nodes WHERE tax_id = " + taxId);
				if (!r.next()) {
					System.out.println("Not found taxid in mysql " + taxId);
					st.close();
					continue;
				}
				int div = r.getInt(1);

				st.close();
				count++;

				out.write(mass + "," + peptide + "," + accVersion + "," + taxId + "," + div + "\n");

				if (count % 100_000_000 == 0) {
					System.out.println("Sada je na " + count + " " + formatDurationHMS(stopWatch.getTime()));
				}
			}
		} catch (Throwable e) {
			System.err.println("Errr line: " + line.toString());
			e.printStackTrace();
		}

		IOUtils.closeQuietly(out);
		mysql.closeDatabaase();

		// System.out.println("Duration: " +
		// DurationFormatUtils.formatDurationHMS(stopWatch.getTime()));
		// System.out.println("Finish");

	}
}
