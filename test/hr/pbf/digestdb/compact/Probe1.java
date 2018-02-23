package hr.pbf.digestdb.compact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.util.MassRangeMap;

public class Probe1 {
	private static final Logger log = LoggerFactory.getLogger(Probe1.class);

	public static void main(String[] args) {
		MassRangeMap map = new MassRangeMap(0.3f, 500, 1020);
		String f = map.getFileName(1011.2f);
		log.debug(map.getMap().toString());
		log.debug("f " + f);
	}

}
