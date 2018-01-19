package hr.pbf.digestdb.test.probe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProbeNumber {
	private static final Logger log = LoggerFactory.getLogger(ProbeNumber.class);

	public static void main(String[] args) {
		double d = 6172.12345678912345d;
		log.debug("dela");
		int p = (int) (d * 100_000);
		System.out.println(p);
		System.out.println((float) p);
	}

}
