package hr.pbf.digestdb.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import hr.pbf.digestdb.app.App_7_StatisticOnSortedFileCSV;
import hr.pbf.digestdb.util.BioUtil;

class TestUtilMethods {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@Test
	void test() {
		String testAccession1 = "WP_017559261.1";
		String testAccession2 = "CEG88061.1";
		assertEquals("WP", BioUtil.extractAccessionPrefix(testAccession1));
		assertEquals("CEG", BioUtil.extractAccessionPrefix(testAccession2));

		String prf1 = "prf||0807298A:PDB=3PGM";
		String prf2 = "prf||1513188A:PDB=1BBC,1POD";
		assertEquals("prf", BioUtil.extractAccessionPrefix(prf1));
		assertEquals("prf", BioUtil.extractAccessionPrefix(prf2));

	}

}
