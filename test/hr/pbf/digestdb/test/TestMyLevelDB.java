package hr.pbf.digestdb.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map.Entry;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.LevelDButil;

class TestMyLevelDB {

	private static DB db;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		db = LevelDButil.open(
				"C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\leveldb_accession2taxid_dead",
				LevelDButil.getStandardOptions());
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println(LevelDButil.getStatus(db));
		db.close();
	}

	@Test
	void test1() {

		DBIterator it = db.iterator();
		System.out.println(BiteUtil.toBytes(0));
		int i = 0;
		while (false || it.hasNext()) {
			if (++i > 100 && i % 10000 == 0) {
				break;
			}
			Entry<byte[], byte[]> next = it.next();
			String acc = BiteUtil.toStringFromByte(next.getKey());
			int t = BiteUtil.toInt(next.getValue());

			System.out.println(acc + " " + t);

		}
		try {
			it.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
