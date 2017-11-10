package hr.pbf.digestdb.app;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.transform.sax.SAXTransformerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.mysql.cj.mysqla.MysqlaUtils;

import hr.pbf.digestdb.AppConstants;
import hr.pbf.digestdb.GlobalMainOld;
import hr.pbf.digestdb.app.App_4_CompressManyFilesSmall.PeptideMassIdRow;
import hr.pbf.digestdb.cli.IApp;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MySQLdb;

public class App_6_InsertToMassTable implements IApp {
	private static final Logger log = LoggerFactory.getLogger(App_6_InsertToMassTable.class);
	private BufferedWriter csvOut;

	@Override
	public void populateOption(Options o) {
	}

	public static void main(String[] args) throws IOException {
		App_6_InsertToMassTable t = new App_6_InsertToMassTable();
		String simpleFile = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\small_store\\1325.3.c";
		simpleFile = "/home/tag/nr_db/mass_small_store/1325.3.c";
		HashMap<String, List<Long>> result = App_4_CompressManyFilesSmall.decompress(simpleFile);
		System.out.println(result);
		System.out.println("Size: " + result.size());
	}

	static int count = 0;

	@Override
	public void start(CommandLine appCli) {
		String folderPath = "/home/tag/nr_db/mass_small_store";
		if (SystemUtils.IS_OS_WINDOWS) {
			folderPath = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\small_store";
		}
		File f = new File(folderPath);
		File[] listFiles = f.listFiles();
		MySQLdb db = new MySQLdb();
		try {
			csvOut = BioUtil.newFileWiter("/home/tag/nr_db/id_mass_accession_num.csv", null);

			db.initDatabase(AppConstants.DB_USER, AppConstants.DB_PASSWORD, AppConstants.DB_URL);
			// }

			StopWatch s = new StopWatch();
			s.start();
			ExecutorService tp = Executors.newFixedThreadPool(40);
			Semaphore semaphore = new Semaphore(40);
			String generatedColumns[] = { "id" };

			for (File file : listFiles) {
				if (Files.getFileExtension(file.getAbsolutePath()).equals("db")) {
					semaphore.acquire();
					count++;
					tp.execute(new Runnable() {

						@Override
						public void run() {
							try {
								handleFile(listFiles, db, generatedColumns, file);
								if (count % 100 == 0) {
									System.out.println("Napravio " + count + " od " + listFiles.length + " filova. "
											+ DurationFormatUtils.formatDurationHMS(s.getTime()));
								}
							} catch (IOException | SQLException e) {
								e.printStackTrace();
							}
							semaphore.release();
						}
					});

				}

			} // end foir
				// st.close();
			tp.shutdown();
			tp.awaitTermination(25, TimeUnit.MINUTES);

			IOUtils.closeQuietly(csvOut);
			System.out.println("Finish " + count + " od " + listFiles.length + " filova. "
					+ DurationFormatUtils.formatDurationHMS(s.getTime()));

		} catch (IOException | SQLException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private final void handleFile(File[] listFiles, MySQLdb db, String[] generatedColumns, File file)
			throws IOException, FileNotFoundException, SQLException {
		// System.out.println("Radim " + file);
		TreeSet<PeptideMassIdRow> r = App_4_CompressManyFilesSmall.readSmallDataFile(file);
		HashMap<String, List<PeptideMassIdRow>> res = App_4_CompressManyFilesSmall.mapByPeptide(r);

		Set<String> peptidesSet = res.keySet();
		Connection conn = db.getConnection();
		PreparedStatement st = conn.prepareStatement("INSERT INTO mass (mass, peptide) VALUES (?, ?)",
				generatedColumns);

		for (String peptide : peptidesSet) {

			List<PeptideMassIdRow> list = res.get(peptide);
			List<Long> accessionNums = new ArrayList<>(list.size());
			for (PeptideMassIdRow peptideMassIdRow : list) {
				accessionNums.add(peptideMassIdRow.id);
			}
			// List<Long> accessionNums = res.get(peptide);
			double mass = BioUtil.calculateMassWidthH2O(peptide);
			st.setDouble(1, mass);
			st.setString(2, peptide);
			st.executeUpdate();

			ResultSet rs = st.getGeneratedKeys();
			if (rs.next()) {
				long id = rs.getLong(1);
				insertPeptideAccessionNums(id, accessionNums, db);
			} else {
				System.out.println("Nisam dobio ID ???");
			}

			// byte[] writeLong = writeLong(accessionNums);
			// log.debug("bytes longova ima: {} length ", writeLong.length);
			// st.setBlob(3, new ByteArrayInputStream(writeLong));
			// st.addBatch();
		}
		st.close();
		// st.executeBatch();

		// if (count > 0) {
		// break;
		// }

		conn.close();
	}

	private void insertPeptideAccessionNums(long id, List<Long> accessionNums, MySQLdb db) {
		for (Long acc : accessionNums) {
			try {
				synchronized (csvOut) {
					csvOut.write(Long.toString(id));
					csvOut.write(',');
					csvOut.write(Long.toString(acc));
					csvOut.write('\n');
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// Connection c;
		// try {
		// c = db.getConnection();
		// c.setAutoCommit(false);
		// PreparedStatement st = c
		// .prepareStatement("INSERT INTO accession_nums (id_mass, accession_num) VALUES
		// (?, ?)");
		//
		// for (Long a : accessionNums) {
		// st.setLong(1, id);
		// st.setLong(2, a);
		// st.addBatch();
		// }
		// st.executeBatch();
		// c.commit();
		//
		// c.close();
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }

	}

	private byte[] writeLong(List<Long> accessionNums) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(accessionNums.size() * 8);
		DataOutputStream da = new DataOutputStream(out);
		// int c = 0;
		for (long v : accessionNums) {
			da.writeLong(v);
			// c++;
		}
		// if (c > 1)
		// log.debug("Zapisao longova {}", c);
		da.flush();
		return out.toByteArray();
	}

}
