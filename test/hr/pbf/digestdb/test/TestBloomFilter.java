package hr.pbf.digestdb.test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class TestBloomFilter {

	public static void main(String[] args) {

		// 60_577_058_951
		BloomFilter<String> b = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.US_ASCII), 8_300_000_00L);
		b.put("PERO");
		b.put("JANKO");

		System.out.println(b.mightContain("PERO"));
		System.out.println(b.mightContain("JANKO"));
		
	}
}
