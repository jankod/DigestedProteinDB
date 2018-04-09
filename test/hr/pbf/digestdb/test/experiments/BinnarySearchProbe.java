package hr.pbf.digestdb.test.experiments;

import hr.pbf.digestdb.uniprot.UniprotModel;
import org.eclipse.collections.impl.parallel.ParallelIterate;

import javax.enterprise.inject.Model;
import java.util.Arrays;

public class BinnarySearchProbe {

	private static float[] masses;

	public static void main(String[] args) {
		masses = new float[4];
		masses[0] = 500f;
		masses[1] = 500.1f;
		masses[2] = 500.6f;
		masses[3] = 501.1f;

		search(300);
		search(500);
		search(500.1f);
        search(500.11f);
		search(600);
		search(601);
	}

	private static void search(float i) {
		int res = Arrays.binarySearch(masses, i);
		System.out.println("For "+ i + " result "+ res);


	}
}
