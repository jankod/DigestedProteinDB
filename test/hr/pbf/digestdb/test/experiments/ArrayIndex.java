package hr.pbf.digestdb.test.experiments;

import org.h2.table.IndexColumn;

import gnu.trove.list.array.TFloatArrayList;

public class ArrayIndex {

	
	private float[] masses;
	private int[] peptides;
	private TFloatArrayList massesList;
	
	public ArrayIndex(int size) {
		masses = new float[size];
		massesList = new TFloatArrayList(masses);
		peptides = new int[size];
		
	}
	
	public void put(int index, float mass, int howPeptides) {
		masses[index] = mass;
		peptides[index] = howPeptides;
	}
	
	public static void main(String[] args) {
		ArrayIndex i = new ArrayIndex(10);
		
	}
	
	
	
}
