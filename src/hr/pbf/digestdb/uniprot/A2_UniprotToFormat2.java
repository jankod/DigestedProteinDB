package hr.pbf.digestdb.uniprot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static void main(String[] args) throws IOException {
		String src = "F:\\Downloads\\uniprot\\uniprot_sprot.dat_delta-db1_100000";
		String dest = "F:\\Downloads\\uniprot\\sprot_final";
		log.debug("Dest "+ dest);
		A2_UniprotToFormat2 c = new A2_UniprotToFormat2(src, dest);
		c.start();
		log.debug("Finish");
	}

	public void start() throws IOException {
		File[] listFiles = fileDbDir.listFiles();
		for (File f : listFiles) {
			// UniprotUtil.dataToPeptides()
			// IOUtils.toByteArray(i	nput)
			byte[] format1bytes = UniprotUtil.toByteArrayFast(f);
			MyDataInputStream in = new MyDataInputStream(new FastByteArrayInputStream(format1bytes));

			ArrayList<PeptideAccTax> pepList = new ArrayList<>();
			while (in.available() != 0) {
				PeptideAccTax pep = UniprotUtil.readOneFormat1(in);
				pepList.add(pep);
			}

			Map<String, List<PeptideAccTax>> group = UniprotUtil.groupByPeptide(pepList);
			byte[] format2bytes = UniprotUtil.peptideToFormat2(group);
			IOUtils.write(format2bytes, new FileOutputStream(
					new File(fileResultsDir, FilenameUtils.getBaseName(f.getName()) + ".format2")));

		}
	}
}
