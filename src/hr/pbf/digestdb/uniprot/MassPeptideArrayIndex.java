package hr.pbf.digestdb.uniprot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.impl.list.mutable.primitive.FloatArrayList;

public class MassPeptideArrayIndex {

	private float[] masses;
	private int[] peptides;

	public MassPeptideArrayIndex(int size) {
		masses = new float[size];
		peptides = new int[size];
	}

	public void put(int index, float mass, int howPeptides) {
		masses[index] = mass;
		peptides[index] = howPeptides;
	}

	public SubMassPeptideIndex search(float from, float to) {
		Arrays.binarySearch(masses, from);
//it.unimi.dsi.fastutil.floats.FloatArrayList l;
//l.subList(from, to)
		

		return null;
	}

	public static class SubMassPeptideIndex {
		private float[] masses;
		private int[] peptides;
	}
}
