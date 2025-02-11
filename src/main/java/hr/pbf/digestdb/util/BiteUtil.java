package hr.pbf.digestdb.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.google.common.base.Charsets;

public class BiteUtil {

	public static final byte[] toBytes(int value) {
	//	return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
		return ByteBuffer.allocate(4).putInt(value).array();
	}

	public static int toInt(byte[] bytes) {
//		int result = 0;
//		for (int i = 0; i < 4; i++) {
//			result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
//		}
//		return result;
		return ByteBuffer.wrap(bytes).getInt();
	}

	public static String toStringFromByte(byte[] b) {
		return new String(b, Charsets.US_ASCII);
	}

	public static byte[] toBytes(String s) {
		return s.getBytes(Charsets.US_ASCII);
	}

	public static byte[] toBytes(float f) {
		return ByteBuffer.allocate(4).putFloat(f).array();
	}

	public static float toFloat(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getFloat();
	}
}
