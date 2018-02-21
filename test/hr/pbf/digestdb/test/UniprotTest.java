package hr.pbf.digestdb.test;

import static hr.pbf.digestdb.uniprot.UniprotParseUtil.parseFirstAccession;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import hr.pbf.digestdb.uniprot.UniprotDeltaMassReader;
import hr.pbf.digestdb.uniprot.UniprotModel;
import hr.pbf.digestdb.uniprot.UniprotParseUtil;

class UniprotTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@Test
	void testKryo() {
		
		ArrayList<UniprotModel.PeptideAccTax> list1 = new ArrayList<>();
		
		//UniprotDeltaMassReader.toKryo(mass, d);

	}

	@Test
	void testParseFirstAccession() {
		assertEquals("Q92892",
				parseFirstAccession("AC   Q92892; Q92893; Q92894; Q92895; Q93053; Q96KU9; Q96KV0; Q96KV1;"));

		assertEquals("Q6GZW3", parseFirstAccession("AC   Q6GZW3;"));
	}

	/**
	 * Parsira nesto ovako: OX NCBI_TaxID=30343; Trembl ima ovo: NCBI_TaxID=418404
	 * {ECO:0000313|EMBL:AHZ18584.1}; Moze biti i ovako: "NCBI_TaxID=3617
	 * {ECO:0000305};
	 */
	@Test
	void testParseTaxonomy() {

		{
			// swisprot
			String t1 = "OX   NCBI_TaxID=654924;";
			int res1 = UniprotParseUtil.parseTaxLine(t1);
			assertEquals(654924, res1);
		}
		{
			// uniprot
			String t2 = "OX   NCBI_TaxID=418404 {ECO:0000313|EMBL:ARD89848.1};";
			int res2 = UniprotParseUtil.parseTaxLine(t2);
			assertEquals(418404, res2);
		}
	}

}
