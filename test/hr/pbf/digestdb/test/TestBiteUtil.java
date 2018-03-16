package hr.pbf.digestdb.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import hr.pbf.digestdb.util.BiteUtil;

public class TestBiteUtil {

	@Test
	void testFloat() throws Exception {
		float f = 500.123f;
		byte[] res = BiteUtil.toBytes(f);
		assertEquals(f, BiteUtil.toFloat(res));
	}
	
	@Test
	void testInt() {
		int i = 23243;
		byte[] res = BiteUtil.toBytes(i);
		assertEquals(i, BiteUtil.toInt(res));
	}
}
