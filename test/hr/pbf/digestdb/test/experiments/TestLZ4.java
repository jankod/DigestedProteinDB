package hr.pbf.digestdb.test.experiments;

import java.text.NumberFormat;

import net.jpountz.lz4.LZ4Factory;

public class TestLZ4 {

	public static void main2(String[] args) {
		byte[] bytes = "pedfssdffabc abc  abc sadas dasdas  dasd sadsaas da ABC AACBsdfds fsddasasd adasdasdsad adasds asddas daadsasd asd dssda  as asdasdas dfdsfsdfsd"
				.getBytes();

		LZ4Factory f = LZ4Factory.nativeInstance();
		byte[] result = f.highCompressor().compress(bytes);
		System.out.println("prije  " + bytes.length);
		System.out.println("Poslje " + result.length);

	}

	public static void main(String[] args) {
		NumberFormat f = NumberFormat.getInstance();
		System.out.println(f.format(23423424234234l));
	}
}
