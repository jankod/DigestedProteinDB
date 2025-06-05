package hr.pbf.digestdb.util;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MyUtilTest {

	@Test
	void doubleToByteArray() {
		double value = 123.456;
		byte[] byteArray = MyUtil.doubleToByteArray(value);
		assertEquals(value, MyUtil.byteArrayToDouble(byteArray));

	}

	@Test
	void roundTo4() {
		assertEquals(123.4567, MyUtil.roundTo4(123.45674));
		assertEquals(123.4568, MyUtil.roundTo4(123.45676));
	}

	@Test
	void roundTo5() {
		assertEquals(123.45678, MyUtil.roundTo5(123.456784));
		assertEquals(123.45679, MyUtil.roundTo5(123.456786));
	}

	@Test
	void discretizedTo4() {
		assertEquals("123.4567", MyUtil.discretizedTo4(123.45674));
		assertEquals("123.4568", MyUtil.discretizedTo4(123.45676));
	}

	@Test
	void floatToByteArray() {
		float value = 123.456f;
		byte[] byteArray = MyUtil.floatToByteArray(value);
		assertEquals(value, MyUtil.byteArrayToFloat(byteArray));
	}

	@Test
	void byteArrayToFloat() {
		byte[] byteArray = MyUtil.floatToByteArray(123.456f);
		assertEquals(123.456f, MyUtil.byteArrayToFloat(byteArray));
	}

	@Test
	void byteArrayToDouble() {
		byte[] byteArray = MyUtil.doubleToByteArray(123.456);
		assertEquals(123.456, MyUtil.byteArrayToDouble(byteArray));
	}

	@Test
	void accessionLong36() {
		List<String> accessions = List.of("A0A1B4LE64", "A0AAP0BM96", "D3EBS4", "A0A3D1LDG2", "V5AZM9", "a0a2c5y3c5");
		for(String accession : accessions) {
			long longAccession = MyUtil.toAccessionLong36(accession);
			String convertedAccession = MyUtil.fromAccessionLong36(longAccession);
			assertEquals(accession.toUpperCase(), convertedAccession);
		}
	}

	@Test
	void openWriteDB() throws RocksDBException, IOException {
		File tempDir = Files.createTempDirectory("rocksdb").toFile();
		RocksDB db = MyUtil.openWriteDB(tempDir.getAbsolutePath());
		assertNotNull(db);
		db.close();
	}

	@Test
	void intToByteArray() {
		int value = 123456;
		byte[] byteArray = MyUtil.intToByteArray(value);
		assertEquals(value, MyUtil.byteArrayToInt(byteArray));
	}

	@Test
	void byteArrayToInt() {
		byte[] byteArray = MyUtil.intToByteArray(123456);
		assertEquals(123456, MyUtil.byteArrayToInt(byteArray));
	}

	@Test
	void intListToByteList() {
		List<Integer> intList = List.of(1, 2, 3);
		List<byte[]> byteList = MyUtil.intListToByteList(intList);
		assertEquals(intList.size(), byteList.size());
		for(int i = 0; i < intList.size(); i++) {
			assertEquals(intList.get(i), MyUtil.byteArrayToInt(byteList.get(i)));
		}
	}

	@Test
	void stopAndShowTime() {
		String result = MyUtil.stopAndShowTime(StopWatch.createStarted(), "Test message");
		assertTrue(result.contains("Test message"));
	}

	@Test
	void getFileSize() {
		File tempFile = new File("tempFile.txt");
		try {
			Files.writeString(tempFile.toPath(), "test content");
			assertEquals("12 bytes", MyUtil.getFileSize(tempFile.getAbsolutePath()));
		} catch(IOException e) {
			fail("IOException occurred during test");
		} finally {
			tempFile.delete();
		}
	}

	@Test
	void getDirSize() {
		File tempDir = new File("tempDir");
		tempDir.mkdir();
		try {
			File tempFile = new File(tempDir, "tempFile.txt");
			Files.writeString(tempFile.toPath(), "test content");
			assertEquals("12 bytes", MyUtil.getDirSize(tempDir.getAbsolutePath()));
		} catch(IOException e) {
			fail("IOException occurred during test");
		} finally {
			tempDir.delete();
		}
	}
}
