package hr.pbf.digestdb.test.experiments;

import java.io.IOException;

import hr.pbf.digestdb.uniprot.A4_UniprotCsvToLevelDB;

public class TestBugLevelDbOnePeptide {

	
	public static void main(String[] args) throws IOException {
		A4_UniprotCsvToLevelDB.createIndexfromLeveldb("F:\\tmp\\trembl._10_000_lines.csv");
	}
}
