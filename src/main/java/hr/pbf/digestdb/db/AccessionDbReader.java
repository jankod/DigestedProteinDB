package hr.pbf.digestdb.db;

import hr.pbf.digestdb.util.MyUtil;
import hr.pbf.digestdb.util.ValidatateUtil;

public class AccessionDbReader {

	private final String dbPath;
	// private String[] accList;
	private long[] accList;

	public AccessionDbReader(String dbPath) {
		this.dbPath = dbPath;
		ValidatateUtil.fileMustExist(dbPath);
		loadDb();
	}

	public String getAccession(int index) {
		if(accList == null) {
			throw new RuntimeException("Acc list is not loaded.");
		}
		if(index < 0 || index >= accList.length) {
			throw new RuntimeException("Acc list, index out of bounds: " + index);
		}
		long accLong = accList[index];
		return MyUtil.fromAccessionLong36(accLong);
	}

	private void loadDb() {
		accList = AccessionDbCreator.readBinaryDb(dbPath);

	}

	public int getAccessionCount() {
		return accList.length;
	}
}
