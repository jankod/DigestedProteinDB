package hr.pbf.digestdb.uniprot.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.uniprot.Format3Index;
import hr.pbf.digestdb.uniprot.UniprotCSVformat;
import hr.pbf.digestdb.uniprot.UniprotFormat3;
import hr.pbf.digestdb.uniprot.UniprotFormat3Creator;
import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideMassAccTaxList;

public class TestUniprotFormat3 {

	static String pathDb = "c:/tmp/uniprot_format3.db";
	static String pathIndex = "c:/tmp/uniprot_format3.index";
	private static final Logger log = LoggerFactory.getLogger(TestUniprotFormat3.class);

	@BeforeAll
	static void before() {
		// String res =
		// TestUniprotFormat3.class.getResource("test_uniprot.csv").getFile();

		try {
			// log.debug(res);
			UniprotFormat3Creator c = new UniprotFormat3Creator(pathDb, pathIndex, 2);

			{
				PeptideMassAccTaxList row = UniprotCSVformat.parseCSVLine(
						"500.18015	GGCHQ	A0A2D1GQ99:2048005,A0A218M489:2006922,A0A151AVI8:1122241,F2VWY8:1002724");
				c.putNext(row.getMass(), row.getPeptide(), row.getAccTaxs());
			}
			{
				PeptideMassAccTaxList row = UniprotCSVformat.parseCSVLine("500.20532	ANCPP	A0A0C9TC68:990650");
				c.putNext(row.getMass(), row.getPeptide(), row.getAccTaxs());
			}
			c.finish();

			// log.debug("creatred DB");
		} catch (IOException e) {
			log.error("", e);
			assertTrue(false, "Exception " + e.getMessage());
		}
	}

	@AfterAll
	static void afterAll() {
		// new File(pathDb).delete();
		// new File(pathIndex).delete();
	}

	private List<AccTax> newList(AccTax... accTax) {
		ArrayList<AccTax> result = new ArrayList<>(accTax.length);
		for (AccTax a : accTax) {
			result.add(a);
		}
		return result;
	}

	@Test
	void parseCSVLine1() throws Exception {
		PeptideMassAccTaxList result = UniprotCSVformat.parseCSVLine("500.20532	ANCPP	A0A0C9TC68:990650");
		assertEquals(500.20532F, (float) result.getMass());
		assertEquals("ANCPP", result.getPeptide());
		List<AccTax> accTaxs = result.getAccTaxs();
		assertEquals(1, accTaxs.size());
		assertEquals("A0A0C9TC68", accTaxs.get(0).getAcc());
		assertEquals(990650, accTaxs.get(0).getTax());

	}

	@Test
	void parseCSVLine2() throws Exception {
		PeptideMassAccTaxList result = UniprotCSVformat.parseCSVLine(
				"500.20532	NPACP	D8RWN9:88036,D8S665:88036,A0A0J9CW18:13690,K9DG33:883163,A0A291N0X2:13690,A0A085K075:13690,A0A084E946:13690,A0A177JPN3:13690");
		assertEquals(500.20532F, (float) result.getMass());
		assertEquals("NPACP", result.getPeptide());
		List<AccTax> accTaxs = result.getAccTaxs();
		assertEquals(8, accTaxs.size());
		assertEquals("D8RWN9", accTaxs.get(0).getAcc());
		assertEquals(88036, accTaxs.get(0).getTax());

		assertEquals("D8S665", accTaxs.get(1).getAcc());
		assertEquals(88036, accTaxs.get(1).getTax());

	}

	@Test
	void testIndex() throws Exception {
		// byte[] bytes = UniprotUtil.toByteArrayFast(new File(pathIndex));

		Format3Index index = new Format3Index(4);
		index.newEntry(0, 500.1f, 0);
		index.newEntry(1, 500.2f, 5002);
		index.newEntry(2, 500.3f, 5003);
		index.newEntry(3, 600.4f, 6004);
		index.setLastPostitionFileLength(7000);

		
		{
			long[] pos = index.get(500.15F, 500.4F);
			assertArrayEquals(new long[] {5002, 6004},  pos);
		}
		
		
		{
			assertNull(index.get(300, 400));
			assertNull(index.get((float) 300.2332, 500));
		}
		{
			assertNull(index.get(22300, 422200));
			assertNull(index.get((float) 600.5, 400));
		}
		
		
		assertEquals(4, index.size());

		assertNull(index.get(300F));
		assertNull(index.get(500F));
		assertNull(index.get(500.4F));
		assertNull(index.get(600F));

		{
			long[] pos = index.get(500.1f);
			assertArrayEquals(new long[] {0, 5002},  pos);
		}
		
		{
			long[] pos = index.get(500.2f);
			assertArrayEquals(new long[] {5002, 5003},  pos);
		}
		{
			long[] pos = index.get(600.4f);
			assertArrayEquals(new long[] {6004, 7000},  pos);
		}
		{
			long[] pos = index.get(8000.2323f);
			assertNull(pos);
		}
		
	}

	@Test()
	void testRead() throws Exception {
		UniprotFormat3 format3 = new UniprotFormat3(pathDb, pathIndex);

	}

	private ArrayList<AccTax> newAccTax(int i) {
		ArrayList<AccTax> res = new ArrayList<>();
		res.add(new AccTax("AO34B45", 3535 + i));
		res.add(new AccTax("CO84E45", 567535 + i));
		return res;
	}

	@Test
	void testSearch() throws Exception {

		UniprotFormat3 format3 = new UniprotFormat3(pathDb, pathIndex);
	}

}
