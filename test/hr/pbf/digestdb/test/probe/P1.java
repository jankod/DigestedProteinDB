package hr.pbf.digestdb.test.probe;

import java.io.FileNotFoundException;
import java.io.IOException;

import hr.pbf.digestdb.util.CallbackMass;
import hr.pbf.digestdb.util.MassCSV;

public class P1 {

	static int c = 0;

	public static void main(String[] args) throws FileNotFoundException, IOException {

		MassCSV csv = new MassCSV(
				"C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\nr_mass_sorted_200_000.csv");
		csv.startIterate(new CallbackMass() {
			@Override
			public void row(double mass, String accVersion, String peptide) {
				// System.out.println(mass + " "+ accVersion + " "+ peptide);
				c++;
			}
		});
		System.out.println(c);

	}
}
