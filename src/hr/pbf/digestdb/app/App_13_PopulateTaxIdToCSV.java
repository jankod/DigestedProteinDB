package hr.pbf.digestdb.app;

import java.io.FileNotFoundException;
import java.io.IOException;

import hr.pbf.digestdb.util.CallbackMass;
import hr.pbf.digestdb.util.MassCSV;
import hr.pbf.digestdb.util.MyLevelDB;

public class App_13_PopulateTaxIdToCSV {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		MassCSV csv = new MassCSV("path");
		
		MyLevelDB dbAccession = new MyLevelDB("path");
		
		csv.startIterate(new CallbackMass() {
			@Override
			public void row(Double mass, String accVersion, String peptide) {
				 int taxId = dbAccession.getInt(accVersion);
				
			}
		});
	}
}
