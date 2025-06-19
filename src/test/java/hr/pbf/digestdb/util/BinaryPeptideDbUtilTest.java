package hr.pbf.digestdb.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class BinaryPeptideDbUtilTest {

	@Test()
	void testWriteReadInt() {
		for(int i = 0; i < 20_000; i++) {
			//log.debug("Test int: {}", i);
			testWriteVarInt(i);
			if(i > 20) {
				i += 20;
			}
			if(i > 100) {
				i += 100;
			}
		}
	}

	void testWriteVarInt(int testInt) {
		byte[] buffer = new byte[20];
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		//int testInt = 123456;
		BinaryPeptideDbUtil.writeVarInt(byteBuffer, testInt);
		byteBuffer.rewind(); // Reset the position to the beginning of the buffer
		assertEquals(testInt, BinaryPeptideDbUtil.readVarInt(byteBuffer));
		log.debug("Length of {} buffer: {}", testInt, byteBuffer.position());
	}

	@Test
	void testReadVarInt() {
		byte[] buffer = new byte[10];
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		byteBuffer.put((byte) 0x80);
		byteBuffer.put((byte) 0x01);
		byteBuffer.flip();
		assertEquals(128, BinaryPeptideDbUtil.readVarInt(byteBuffer));
	}

	@Test
	void testWriteGroupedRowLarge() throws Exception {
		// read large_groupped_row.txt from src/test/resource/large_groupped_row.txt
		String groupedRow = "SGAGAAA:15-SAAGGAA:14-TGAAAGG:16;12345";
		groupedRow = readFileToString("large_groupped_row.txt");

		log.debug("String length: {}", groupedRow.length());
		byte[] bytes = BinaryPeptideDbUtil.writeGroupedRow(groupedRow);
		log.debug("Bytes length: {}", bytes.length);
		assertNotNull(bytes);
		assertTrue(bytes.length > 0);
		Set<BinaryPeptideDbUtil.PeptideAccids> result = BinaryPeptideDbUtil.readGroupedRow(bytes);
		assertNotNull(result);
		assertFalse(result.isEmpty());

		log.info("Find acc {}", result.size());

	}

	public String readFileToString(String path) throws Exception {
		// Dobij ClassLoader
		ClassLoader classLoader = getClass().getClassLoader();

		// Pronađi fajl u resources folderu
		try(InputStream inputStream = classLoader.getResourceAsStream(path)) {
			if(inputStream == null) {
				throw new IllegalArgumentException("Fajl " + path + " nije pronađen!");
			}

			// Pročitaj fajl u String
			try(BufferedReader reader = new BufferedReader(
					new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				return reader.lines().collect(Collectors.joining("\n"));
			}
		}
	}

	@Test
	void testWriteGroupedRow() {
		String groupedRow = "SGAGAAA:15-SAAGGAA:14-TGAAAGG:16;12345";
		log.debug("String length: {}", groupedRow.length());
		byte[] bytes = BinaryPeptideDbUtil.writeGroupedRow(groupedRow);
		log.debug("Bytes length: {}", bytes.length);
		assertNotNull(bytes);
		assertTrue(bytes.length > 0);
		Set<BinaryPeptideDbUtil.PeptideAccids> result = BinaryPeptideDbUtil.readGroupedRow(bytes);
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(3, result.size());
		result.forEach(c -> {
			switch(c.seq) {
				case "SGAGAAA" -> assertEquals(15, c.accids[0]);
				case "SAAGGAA" -> assertEquals(14, c.accids[0]);
				case "TGAAAGG" -> {
					assertEquals(16, c.accids[0]);
					assertEquals(12345, c.accids[1]);
				}
				default -> fail("Unexpected sequence: " + c.seq);
			}
		});
	}
}
