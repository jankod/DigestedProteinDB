package hr.pbf.digestdb.util;

import java.nio.ByteBuffer;

import com.google.common.base.Charsets;

public class BiteUtil {

	public static final byte[] toByte(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}

	public static int toInt(byte[] bytes) {
		int result = 0;
		for (int i = 0; i < 4; i++) {
			result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
		}
		return result;
	}

	public static String toStringFromByte(byte[] b) {
		return new String(b, Charsets.US_ASCII);
	}

	public static byte[] toByte(String s) {
		return s.getBytes(Charsets.US_ASCII);
	}

	public static byte[] floatToByteArray(float f) {
		return ByteBuffer.allocate(4).putFloat(f).array();
	}

	public static float byteArrayToFloat(byte[] bytes) {
		 return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
		//return ByteBuffer.allocate(4).put(t).getFloat();
	}
}
