package hr.pbf.digestdb.util;

import java.nio.ByteBuffer;

import com.google.common.base.Charsets;

public class BiteUtil {

	public static byte[] toBytes(int value) {
		return ByteBuffer.allocate(4).putInt(value).array();
	}

	public static int toInt(byte[] bytes) {
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
