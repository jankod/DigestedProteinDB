package hr.pbf.digestdb.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import hr.pbf.digestdb.MyUtil;

class TestMyUtil {

	@Test
	void test1() {
		assertFalse(MyUtil.argsFirstElementContain("pero", makeArray()));
		assertFalse(MyUtil.argsFirstElementContain("pero", makeArray("ss", "pero")));
		assertTrue(MyUtil.argsFirstElementContain("uniprot", makeArray("uniprot")));
		assertTrue(MyUtil.argsFirstElementContain("uniprot", makeArray("uniprot", "nesto")));

	}

	@Test
	void test2() {
		String[] res = MyUtil.argsMinusFirstElement(makeArray("first"));
		assertArrayEquals(makeArray(), res);
		assertEquals(0, res.length);
		
		assertArrayEquals(makeArray("2"), MyUtil.argsMinusFirstElement(makeArray("1", "2")));
	}

	private <T> T[] makeArray(T... t) {
		return t;
	}

}
