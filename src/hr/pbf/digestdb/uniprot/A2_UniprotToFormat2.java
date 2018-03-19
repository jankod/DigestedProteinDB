package hr.pbf.digestdb.uniprot;

import static hr.pbf.digestdb.uniprot.UniprotUtil.getDirectorySize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.iq80.snappy.SnappyFramedOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;

/**
 * 
 * @author tag
 *
 */
public class A2_UniprotToFormat2 {

	private File fileDbDir;
	private File fileResultsDir;

	public A2_UniprotToFormat2(String pathDbDir, String pathResultsDir) throws IOException {

		fileDbDir = new File(pathDbDir);
		if (!fileDbDir.isDirectory()) {
			throw new IOException("Not a dir: " + pathDbDir);
		}
		fileResultsDir = new File(pathResultsDir);
		if (!fileResultsDir.isDirectory())
			fileResultsDir.mkdirs();

	}

	private static final Logger log = LoggerFactory.getLogger(A2_UniprotToFormat2.class);

	private static int countMax =  Integer.MAX_VALUE;

	public static void main(String[] args) throws IOException, InterruptedException {
		String format1Dir = "F:\\Downloads\\uniprot\\uniprot_sprot.dat_format1";
		String format2Dir = "F:\\Downloads\\uniprot\\format2s-ponovo";

		// format1Dir =
		// "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl\\demo-format1";
		// format2Dir =
		// "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl\\demo-format2";

		if (SystemUtils.IS_OS_LINUX) {
			format1Dir = "/home/users/tag/uniprot/trembl_format1";
			format2Dir = "/home/users/tag/uniprot/trembl_format2s";
		}

		log.debug("Format2s dir: " + format2Dir);
		A2_UniprotToFormat2 c = new A2_UniprotToFormat2(format1Dir, format2Dir);
		c.start();
		log.debug("Finish");
		// 922 -> 1 GB -> 902 MB -> snappy 633 MB
	}

	public void start() throws IOException, InterruptedException {
		File[] listFiles = fileDbDir.listFiles();

		ExecutorService ex = Executors.newFixedThreadPool(8);
		Semaphore s = new Semaphore(8);
		int c = 0;
		for (File f : listFiles) {
			if (c++ == countMax) {
				log.info("break when " + countMax);
				break;
			}
			s.acquire();
			ex.execute(new Runnable() {

				@Override
				public void run() {
					try {
						convert(f);
					} catch (IOException e) {
						log.error("", e);
					} finally {
						s.release();
					}
				}
			});
		}
		log.debug("Finish all, wait...");
		ex.shutdown();
		ex.awaitTermination(10, TimeUnit.MINUTES);
		log.debug("Is terminated: " + ex.isTerminated());

		log.debug("format1 dir: " + getDirectorySize(fileDbDir.getPath()));
		log.debug("format2 dir: " + getDirectorySize(fileResultsDir.getPath()));
	}

	private void convert(File f) throws IOException {
		// log.debug("StartJetty convert " + f.getName());
		byte[] format1bytes = UniprotUtil.toByteArrayFast(f);

		ArrayList<PeptideAccTax> pepList = UniprotUtil.fromFormat1(format1bytes);

		Map<String, List<PeptideAccTax>> group = UniprotUtil.groupByPeptide(pepList);

		byte[] format2bytes = UniprotUtil.toFormat2(group);

		format2bytes = UniprotUtil.compress(format2bytes);
		File fileFormat2 = new File(fileResultsDir, FilenameUtils.getBaseName(f.getName()) + ".f2s");
		Files.write(fileFormat2.toPath(), format2bytes);
	}
}
