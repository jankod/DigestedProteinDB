package hr.pbf.digestdb.test;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mapdb.SortedTableMap;

import hr.pbf.digestdb.uniprot.UniprotLevelDbFinder;
import hr.pbf.digestdb.uniprot.UniprotLevelDbFinder.IndexResult;

public class TestIndex {

	public static void main(String[] args) {
		int start = 0;
		int fromPos = 0;
		int endPos = 463;
		if (start >= fromPos && start < endPos) {
			System.out.println("true");
		} else {
			System.out.println("false");
		}

	}

	@Test
	void testIndexPosotion() throws Exception {

		UniprotLevelDbFinder.IndexResult index = new IndexResult();
		index.map = new TreeMap<>();
		float f1 = 11.11f;
		float f2 = 22.22f;
		float f3 = 33.33f;

		index.map.put(f1, 10);
		index.map.put(f2, 10);
		index.map.put(f3, 10);

		{
			Pair<Float, Long> res = index.getStartMass(12);
			assertNotNull(res);
			assertEquals(res.getKey(), Float.valueOf(f2));
			assertEquals(res.getValue().intValue(), 2);
		}
		{
			Pair<Float, Long> res = index.getStartMass(29);
			assertNotNull(res);
			assertEquals(res.getKey().floatValue(), f3);
			assertEquals(res.getValue().intValue(), 9);
		}
		{
			Pair<Float, Long> res = index.getStartMass(1);
			assertNotNull(res);
			assertEquals(res.getKey().floatValue(), f1);
			assertEquals(res.getValue().intValue(), 1);
		}

		{
			Pair<Float, Long> res = index.getStartMass(31);
			assertNull(res);
		}
		{
			long countTotalPeptides = index.countTotalPeptides();
			assertEquals(30, countTotalPeptides);
			
		}

	}
}
