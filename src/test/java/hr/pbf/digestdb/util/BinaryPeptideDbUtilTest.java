package hr.pbf.digestdb.util;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BinaryPeptideDbUtilTest {

	@Test
	void testWriteVarInt() {
		byte[] buffer = new byte[10];
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		BinaryPeptideDbUtil.writeVarInt(byteBuffer, 123456);
		assertEquals(3, byteBuffer.position());
		assertEquals(123456, BinaryPeptideDbUtil.readVarInt(byteBuffer));
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
	void testWriteGroupedRow() {
		String groupedRow = "SGAGAAA:15-SAAGGAA:14-TGAAAGG:16;12345";
		byte[] bytes = BinaryPeptideDbUtil.writeGroupedRow(groupedRow);
		assertNotNull(bytes);
		assertTrue(bytes.length > 0);
		Set<BinaryPeptideDbUtil.PeptideAcc> result = BinaryPeptideDbUtil.readGroupedRow(bytes);
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(3, result.size());
		result.forEach(c -> {
			switch(c.seq) {
				case "SGAGAAA" -> assertEquals(15, c.acc[0]);
				case "SAAGGAA" -> assertEquals(14, c.acc[0]);
				case "TGAAAGG" -> {
					assertEquals(16, c.acc[0]);
					assertEquals(12345, c.acc[1]);
				}
				default -> fail("Unexpected sequence: " + c.seq);
			}
		});
	}
}