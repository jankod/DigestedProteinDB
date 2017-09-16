package hr.pbf.digestdb.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.transform.sax.SAXTransformerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.mysql.cj.mysqla.MysqlaUtils;

import hr.pbf.digestdb.AppConstants;
import hr.pbf.digestdb.GlobalMainOld;
import hr.pbf.digestdb.cli.IApp;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MySQLdb;

public class App_6_InsertToMassTable implements IApp {
	private static final Logger log = LoggerFactory.getLogger(App_6_InsertToMassTable.class);

	@Override
	public void populateOption(Options o) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(CommandLine appCli) {
		String folderPath = "/home/tag/nr_db/mass_small_store";
		File f = new File(folderPath);
		File[] listFiles = f.listFiles();

		MySQLdb db = new MySQLdb();
		try {
			db.initDatabase(AppConstants.DB_USER, AppConstants.DB_PASSWORD, AppConstants.DB_URL);
			Connection conn = db.getConnection();
			int count = 0;
			StopWatch s = new StopWatch();
			s.start();
			ExecutorService tp = Executors.newCachedThreadPool();
			PreparedStatement st = conn
					.prepareStatement("INSERT INTO mass (mass, peptide, accession_nums) VALUES (?, ?, ?)");
			conn.setAutoCommit(false);

			for (File file : listFiles) {
				if (Files.getFileExtension(file.getAbsolutePath()).equals("c")) {
					HashMap<String, List<Long>> peptides = App_4_CompressManyFilesSmall
							.decompress(file.getAbsolutePath());

					Set<String> peptidesSet = peptides.keySet();
					for (String peptide : peptidesSet) {
						List<Long> accessionNums = peptides.get(peptide);
						if (accessionNums.size() > 1)
							log.debug("accessions ima ih {}", accessionNums.size());
						double mass = BioUtil.calculateMassWidthH2O(peptide);
						st.setDouble(1, mass);
						st.setString(2, peptide);
						byte[] writeLong = writeLong(accessionNums);
						// log.debug("bytes longova ima: {} length ", writeLong.length);
						st.setBlob(3, new ByteArrayInputStream(writeLong));
						st.addBatch();
					}
					st.executeBatch();
					conn.commit();

					if (count > 30) {
						break;
					}
					if (++count % 500 == 0) {
						System.out.println("Napravio " + count + " od " + listFiles.length + " "
								+ DurationFormatUtils.formatDurationHMS(s.getTime()));
					}
				}

			} // end foir
			st.close();
			conn.commit();
			conn.close();
			System.out.println("Finish " + DurationFormatUtils.formatDurationHMS(s.getTime()));
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}

	private byte[] writeLong(List<Long> accessionNums) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(accessionNums.size() * 8);
		DataOutputStream da = new DataOutputStream(out);
		int c = 0;
		for (long v : accessionNums) {
			da.writeLong(v);
			c++;
		}
		if (c > 1)
			log.debug("Zapisao longova {}", c);
		da.flush();
		return out.toByteArray();
	}

}
