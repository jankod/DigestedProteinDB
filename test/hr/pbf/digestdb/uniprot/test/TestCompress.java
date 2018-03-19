package hr.pbf.digestdb.uniprot.test;

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
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.h2.tools.CompressTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.test.experiments.CompressedPeptide;
import hr.pbf.digestdb.uniprot.UniprotUtil;

public class TestCompress {
	private static final Logger log = LoggerFactory.getLogger(TestCompress.class);

	public static void main(String[] args) throws FileNotFoundException, IOException {
		StopWatch s = new StopWatch();
		s.start();
		String t = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\615.2.db";
		//byte[] r = IOUtils.toByteArray(new FileInputStream(new File(t))); // 00:00:00.672 266 267 [20:55:34]
		byte[] r = UniprotUtil.toByteArrayFast(new File(t)); // 00:00:00.115 114
		s.stop();
		log.debug(DurationFormatUtils.formatDurationHMS(s.getTime()));
		log.debug(" " + r.length); // 163351767
	}
	
	public static void main2(String[] args) throws CompressorException, IOException {
		
//		String dir = "F:\\Downloads\\uniprot";
		StopWatch s = new StopWatch();
		s.start();
		String pathIn = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\615.2.db";
		String pathOut = pathIn +".zip";
		
		FileOutputStream finalOut = new FileOutputStream(pathOut);

		CompressorOutputStream zipOut = new CompressorStreamFactory()
				.createCompressorOutputStream(CompressorStreamFactory.SNAPPY_RAW, finalOut);
		FileInputStream in = new FileInputStream(new File(pathIn));
		IOUtils.copy(in, zipOut);
		zipOut.flush();
		zipOut.close();
		finalOut.close();
		in.close();
		s.stop();
		log.debug(DurationFormatUtils.formatDurationHMS(s.getTime()));
		log.debug("Finish");
	}
}
