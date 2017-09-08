package hr.pbf.digestdb.compact;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MassRangeMap;

public class MassRangeMapTest {
	private static final Logger log = LoggerFactory.getLogger(MassRangeMapTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetFileName() {
		MassRangeMap map = new MassRangeMap(0.3, 500, 6000);
		String mustBeNull = map.getFileName(400);
		assertNull(mustBeNull);
		String fileName500 = map.getFileName(500);
		log.debug("fileName500 {} ", fileName500);

		String fileName501_1 = map.getFileName(501.1);
		log.debug("fileName501.1 {} ", fileName501_1);

		log.debug(map.getMap().toString());
	}

	@Test
	public void testGetFileName0_2() {
		MassRangeMap map = new MassRangeMap(0.21, 500, 6000);
		String fileName1 = map.getFileName(500.123445);
//		assertEquals("", actual);
		log.debug(fileName1);
		log.debug(map.getMap().toString());
//		log.debug("round {}",BioUtil.roundToDecimals(500.00D, 2));
	}

}
