package hr.pbf.digestdb.test;

import static hr.pbf.digestdb.uniprot.UniprotParseUtil.parseFirstAccession;
import static org.apache.commons.lang3.RandomStringUtils.randomAscii;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.text.CharacterPredicate;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;

import hr.pbf.digestdb.uniprot.A2_UniprotDeltaMassReader;
import hr.pbf.digestdb.uniprot.UniprotModel;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.uniprot.UniprotParseUtil;

class UniprotTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	private static final Logger log = LoggerFactory.getLogger(UniprotTest.class);

	@Test
	void testKryo() throws IOException {
		StopWatch s = new StopWatch();

		RandomStringGenerator generatorAZ = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
		RandomStringGenerator generatorAZnum = new RandomStringGenerator.Builder()

				// RandomStringGenerator generator = new RandomStringGenerator.Builder()
				.withinRange('0', 'z').filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
				// .withinRange('0', 'z')
				.build();

		ArrayList<PeptideAccTax> list1 = new ArrayList<>();
		list1.add(new PeptideAccTax("PEPTIDE", "ACSD233", 235872));
		list1.add(new PeptideAccTax("PEPTIDE2", "33CSD233", 2455822));
		for (int i = 0; i < 50_000; i++) {
			list1.add(new PeptideAccTax(generatorAZ.generate(nextInt(6, 50)), generatorAZnum.generate(nextInt(6, 10)),
					RandomUtils.nextInt()));
		}
		float mass = 2344.223F;
		String dir = "c:/tmp/test_kryio";
		new File(dir).mkdirs();
		s.start();
		File resultFile = A2_UniprotDeltaMassReader.toKryo(mass, list1, dir);
		log.debug("save " + s.getTime());
		s = new StopWatch();
		s.start();
		List<PeptideAccTax> listReaded = A2_UniprotDeltaMassReader.fromKryo(resultFile);
		log.debug("read " + s.getTime());

		assertIterableEquals(list1, listReaded);
		assertEquals(list1.size(), listReaded.size());

		for (PeptideAccTax peptideAccTax : listReaded.subList(0, 100)) {
			log.debug(peptideAccTax.toString());
		}

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
