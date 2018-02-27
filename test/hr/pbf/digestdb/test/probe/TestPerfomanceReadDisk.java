package hr.pbf.digestdb.test.probe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import com.google.common.io.Files;
import com.ning.compress.lzf.LZFDecoder;
import com.ning.compress.lzf.LZFEncoder;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.uniprot.UniprotUtil;

public class TestPerfomanceReadDisk {

	private static final Logger log = LoggerFactory.getLogger(TestPerfomanceReadDisk.class);

	public static void main22(String[] args) throws IOException {
		String p = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\601.1.db";
		byte[] b = UniprotUtil.toByteArrayFast(new File(p));
		List<PeptideAccTax> peptides = UniprotUtil.fromFormat1(b);
		for (PeptideAccTax peptideAccTax : peptides) {
			log.debug(peptideAccTax.toString());
		}
		// log.debug(peptides.size() + "");
	}

	public static void main(String[] args) throws Exception {
		String p = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\601.1.db";
		byte[] byteswOrig = UniprotUtil.toByteArrayFast(new File(p)); // 095 107
		{
			StopWatch s = new StopWatch();
			byte[] snappyCompresed = Snappy.compress(byteswOrig);
			s.start();
			byte[] orig = Snappy.uncompress(snappyCompresed);
			s.stop();

			if(orig.length != byteswOrig.length) {
				throw new Exception("Nisu jednaki");
			}
			
			log.debug("snappy length compresses " + snappyCompresed.length);
			log.debug("Snappy decompress: " + DurationFormatUtils.formatDurationHMS(s.getTime()));
		}
		{
			StopWatch s = new StopWatch();
			byte[] compressed = LZFEncoder.encode(byteswOrig);
			s.start();
			byte[] orig = LZFDecoder.decode(compressed);
			s.stop();
			
			if(orig.length != byteswOrig.length) {
				throw new Exception("Nisu jednaki");
			}
			
			log.debug("lzf length compresses " + compressed.length);
			log.debug("LZF: " + DurationFormatUtils.formatDurationHMS(s.getTime()));
		}
		// Files.toByteArray(new File(p));
	}
}
