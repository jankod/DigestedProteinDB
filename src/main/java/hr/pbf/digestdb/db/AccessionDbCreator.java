package hr.pbf.digestdb.db;

import hr.pbf.digestdb.util.MyUtil;
import hr.pbf.digestdb.util.ValidatateUtil;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;

@Slf4j
public class AccessionDbCreator {

	private final String fromCsvPath;

	private final String toDbPath;

	/**
	 * @param fromCsvPath CSV
	 * @param toDbPath    DB path on disk
	 */
	public AccessionDbCreator(String fromCsvPath, String toDbPath) {
		this.fromCsvPath = fromCsvPath;
		this.toDbPath = toDbPath;

		ValidatateUtil.fileMustExist(fromCsvPath);
		ValidatateUtil.fileMustNotExist(toDbPath);
	}

	public void startCreate() throws IOException {
		log.debug("Start creating accession DB from: {} and write DB to: {}", fromCsvPath, toDbPath);
		LongList accList = readCsvToList();
		writeBinaryDb(accList);
	}

	private void writeBinaryDb(LongList accList) throws IOException {
		{
			int bufferSize = 1024 * 1024 * 32;
			try(DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(toDbPath), bufferSize))) {
				out.writeInt(accList.size());
				for(long accLong : accList) {
					out.writeLong(accLong);
				}
			}
		}
	}

	public static long[] readBinaryDb(String dbPath) {
		try(DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(dbPath)))) {
			int size = in.readInt();
			long[] accList = new long[size];
			for(int i = 0; i < size; i++) {
				accList[i] = in.readLong();

			}
			return accList;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public LongList readCsvToList() throws IOException {
		try(BufferedReader reader = IOUtils.toBufferedReader(new FileReader(fromCsvPath))) {
			LongList accList = new LongArrayList();
			String line;
			while((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				int accNum = Integer.parseInt(parts[0]);
				String accessionn = parts[1];
				long accessionLong36 = MyUtil.toAccessionLong36(accessionn);
				accList.add(accNum, accessionLong36);
			}
			return accList;
		}

	}
}
