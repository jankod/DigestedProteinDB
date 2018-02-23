package hr.pbf.digestdb.uniprot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.zone.ZoneRulesProvider;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCompress {
	private static final Logger log = LoggerFactory.getLogger(TestCompress.class);

	public static void main(String[] args) throws CompressorException, IOException {
		
		String dir = "F:\\Downloads\\uniprot";
		FileOutputStream finalOut = new FileOutputStream(dir+"\\615.2.db.zip");

		CompressorOutputStream zipOut = new CompressorStreamFactory()
				.createCompressorOutputStream(CompressorStreamFactory.BZIP2, finalOut);
		
		FileInputStream in = new FileInputStream(new File(dir, "615.2.db"));
		IOUtils.copy(in, zipOut);
		zipOut.flush();
		zipOut.close();
		finalOut.close();
		in.close();
		
		log.debug("Finish");
	}
}
