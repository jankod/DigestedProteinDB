package hr.pbf.digestdb.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import hr.pbf.digestdb.GlobalMain;
import hr.pbf.digestdb.MyUtil;

class TestMyUtil {

	@Test
	void test1() {
		assertFalse(GlobalMain.argsFirstElementContain("pero", makeArray()));
		assertFalse(GlobalMain.argsFirstElementContain("pero", makeArray("ss", "pero")));
		assertTrue(GlobalMain.argsFirstElementContain("uniprot", makeArray("uniprot")));
		assertTrue(GlobalMain.argsFirstElementContain("uniprot", makeArray("uniprot", "nesto")));

	}

	@Test
	void test2() {
		String[] res = GlobalMain.argsMinusFirstElement(makeArray("first"));
		assertArrayEquals(makeArray(), res);
		assertEquals(0, res.length);
		
		assertArrayEquals(makeArray("2"), GlobalMain.argsMinusFirstElement(makeArray("1", "2")));
	}

	private <T> T[] makeArray(T... t) {
		return t;
	}

}
