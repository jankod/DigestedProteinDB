package hr.pbf.digestdb.db;

import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.MyUtil;
import hr.pbf.digestdb.util.ValidatateUtil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
		try(DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(dbPath)))) {
			int size = in.readInt();
			this.accList = new long[size];
			//this.accList[0] = "s0"; // 0 index is empty
			for(int i = 0; i < size; i++) {
				int len = BinaryPeptideDbUtil.readVarInt(in);
				byte[] accBytes = new byte[len];
				in.readFully(accBytes);
				String acc = new String(accBytes, StandardCharsets.UTF_8);
				this.accList[i] = MyUtil.toAccessionLong36(acc);
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

	}
}
