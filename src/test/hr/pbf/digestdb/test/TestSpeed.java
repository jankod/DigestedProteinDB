package hr.pbf.digestdb.test;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import hr.pbf.digestdb.util.BioUtil;

public class TestSpeed {

	
	public static void main(String[] args) {
		StopWatch w = new StopWatch();
		w.start();
		BioUtil.calculateMassWidthH2O("PEPTIDE");
		w.stop();
		System.out.println(DurationFormatUtils.formatDurationHMS(w.getNanoTime()));
	
		
	}
}
