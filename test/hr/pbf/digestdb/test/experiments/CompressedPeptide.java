package hr.pbf.digestdb.test.experiments;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.iq80.snappy.Snappy;

import com.google.common.base.Charsets;
import com.google.common.primitives.Ints;

public class CompressedPeptide {

	public static void main22(String[] args) {
		// A - Z 65:90 , a:z = 97:122
		System.out.println((int) 'a' + ":" + (int) 'z');

		// u 5 byte stane 32 znaka
		System.out.println("___________");
		System.out.println(Character.toString((char) 122));

		// A B C D G H K M S T V W N
		convert("ATC");

	}
	
	public static void main223(String[] args) {
		String orig = "AWPLIRDVQRLIDWDKRI";
		byte[] c = Snappy.compress(orig.getBytes(Charsets.US_ASCII));
		System.out.println(orig.length() + " vs "+ c.length);
		
	}
	
	public static void main(String[] args) {
		// u 5 bit stane 32
		int i = 23335;
		byte[] res = to5byteArray(i);
		System.out.println("Duzina : "+ res.length);
		int ni = from5byteArray(res);
		System.out.println(ni == i);
	}

	private static void convert(String peptide) {
		ByteBuffer buf = ByteBuffer.allocate(peptide.length() * 5);
		for (int i = 0; i < peptide.length(); i++) {
			int num = peptide.charAt(i);
			if (num < 65 || num > 90) {
				throw new RuntimeException("Character: '" + peptide.charAt(i) + "' is num: " + num);
			}
			num = num - 65;

			byte[] res = to5byteArray(num);

			System.out.println(num);
		}
	}

	private final static byte[] to5byteArray(int i) {
		byte[] result = new byte[5];

		result[0] = (byte) (i >> 32);
		result[1] = (byte) (i >> 24);
		result[2] = (byte) (i >> 16);
		result[3] = (byte) (i >> 8);
		result[4] = (byte) (i /* >> 0 */);

		return result;
	}

	public final static int from5byteArray(byte[] b) {

		return (b[0] & 0xFF) << 32 | b[1] << 24 | (b[2] & 0xFF) << 16 | (b[3] & 0xFF) << 8 | (b[4] & 0xFF);
	}

}
