package hr.pbf.digestdb.test.probe;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AccessionTaxIdDBTest {

	
	private static AccessionTaxIdDB db;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		db = new AccessionTaxIdDB("c:/tmp/acctax_leveldb.db");
		
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		db.close();
	}

	@Test
	void test1() {
		db.put("key", "value");
		
	}

}
