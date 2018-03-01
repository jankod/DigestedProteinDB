package hr.pbf.digestdb.uniprot.test;

import org.junit.jupiter.api.Test;

import hr.pbf.digestdb.uniprot.UniprotFormat3;
import hr.pbf.digestdb.uniprot.UniprotFormat3Creator;

public class TestUniprotFormat3 {

	String pathDb = "c:/tmp/uniprot_format3.db";
	String pathIndex = "c:/tmp/uniprot_format3.index";

	@Test()
	void testCreate() throws Exception {
		UniprotFormat3Creator c = new UniprotFormat3Creator(pathDb, pathIndex, 10);

		for (int i = 0; i < 10; i++) {
//			c.putNext(i + 0.3f, "PEPTIDE", new AccTax("AC2333", 3434), new AccTax("BC2333", 343423));
		}
		c.finish();
	}

	@Test
	void testSearch() throws Exception {

		UniprotFormat3 format3 = new UniprotFormat3(pathDb, pathIndex);
	}

}
