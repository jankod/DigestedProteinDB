package hr.pbf.digestdb.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.Test;

import gnu.trove.map.hash.TObjectIntHashMap;
import hr.pbf.digestdb.util.BioUtil;

public class AccessionIntegerTest {

	@Test
	void test1() throws Exception {
		ArrayList<String> prefixList = new ArrayList<>();
		String accession = "XP_00013370388.1";
		int acc = BioUtil.accessionToInt(accession, prefixList);

		String accession2 = BioUtil.intToAccession(acc, prefixList);

		assertEquals(accession, accession2);

		assertTrue(prefixList.size() == 1);
		
	}
	public static void main(String[] args) {
		System.out.println(Integer.MAX_VALUE);
	}

}
