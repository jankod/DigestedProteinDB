package hr.pbf.digestdb.compact;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.util.MassRangeMap;

public class MassRangeMapTest {
	private static final Logger log = LoggerFactory.getLogger(MassRangeMapTest.class);


	public static void main(String[] args) {
		MassRangeMap map = new MassRangeMap(0.3f, 500, 1020);
		String f = map.getFileName(1011.5f);
		log.debug("f "+ f);
	}
	@Test	
	public void test2() {
		
		
	}
	
	@Test
	public void test1() {
		// 1001.6
		MassRangeMap map = new MassRangeMap(0.3f, 500, 6000);
		assertThrows(NullPointerException.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				map.getFileName(400);
			}
		});

		String mass1001_6 = map.getFileName(1001.6f);
		log.debug("1001.6 "+ mass1001_6);
		
		String fileName500 = map.getFileName(500);
		log.debug("fileName500 {} ", fileName500);

		String fileName501_1 = map.getFileName(501.1f);
		log.debug("fileName501.1 {} ", fileName501_1);

		log.debug(map.getMap().toString());

	}

	@Test
	public void testGetFileName0_2() {
		MassRangeMap map = new MassRangeMap(0.21f, 500, 6000);
		String fileName1 = map.getFileName(500.123445f);
		// assertEquals("", actual);
		log.debug(fileName1);
		log.debug(map.getMap().toString());
		// log.debug("round {}",BioUtil.roundToDecimals(500.00D, 2));
	}

	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
}
