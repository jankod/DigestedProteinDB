package hr.pbf.digestdb.test.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.Arrays;

import com.clearspring.analytics.util.Bits;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeOutput;
import com.esotericsoftware.kryo.util.UnsafeUtil;

public class KryoDemo {

	public static void main22(String[] args) throws IOException {
		Kryo kryo = new Kryo();
		File f = new File("test.txt");
		f.delete();
		// UnsafeOutput out = new UnsafeOutput(new FileOutputStream(f));
		// out.flush();
		// out.close();

		ByteBuffer b = MappedByteBuffer.allocate(2);

		ObjectOutputStream ob = new ObjectOutputStream(new FileOutputStream(f));
		ob.writeBoolean(true);
		// ob.writeBoolean(false);
		// ob.writeBoolean(false);
		// ob.writeBoolean(false);
		// ob.flush();
		// ob.close();

		System.out.println("finish");

		System.out.println(f.length());

	}

	public static void main(String[] args) {

		int in[] = { 5, 2, 9 };
		int out[] = new int[9];

		fastpack5(in, 0, out, 0);
		
		System.out.println(Arrays.toString(out));
		
		fastunpack5(out, 0, in, 0);
		
		System.out.println(Arrays.toString(out));
	}

	protected static void fastunpack5(final int[] in, int inpos, final int[] out, int outpos) {
		out[0 + outpos] = ((in[0 + inpos] >>> 0) & 31);
		out[1 + outpos] = ((in[0 + inpos] >>> 5) & 31);
		out[2 + outpos] = ((in[0 + inpos] >>> 10) & 31);
		out[3 + outpos] = ((in[0 + inpos] >>> 15) & 31);
		out[4 + outpos] = ((in[0 + inpos] >>> 20) & 31);
		out[5 + outpos] = ((in[0 + inpos] >>> 25) & 31);
		out[6 + outpos] = (in[0 + inpos] >>> 30) | ((in[1 + inpos] & 7) << (5 - 3));
		out[7 + outpos] = ((in[1 + inpos] >>> 3) & 31);
		out[8 + outpos] = ((in[1 + inpos] >>> 8) & 31);
		out[9 + outpos] = ((in[1 + inpos] >>> 13) & 31);
		out[10 + outpos] = ((in[1 + inpos] >>> 18) & 31);
		out[11 + outpos] = ((in[1 + inpos] >>> 23) & 31);
		out[12 + outpos] = (in[1 + inpos] >>> 28) | ((in[2 + inpos] & 1) << (5 - 1));
		out[13 + outpos] = ((in[2 + inpos] >>> 1) & 31);
		out[14 + outpos] = ((in[2 + inpos] >>> 6) & 31);
		out[15 + outpos] = ((in[2 + inpos] >>> 11) & 31);
		out[16 + outpos] = ((in[2 + inpos] >>> 16) & 31);
		out[17 + outpos] = ((in[2 + inpos] >>> 21) & 31);
		out[18 + outpos] = ((in[2 + inpos] >>> 26) & 31);
		out[19 + outpos] = (in[2 + inpos] >>> 31) | ((in[3 + inpos] & 15) << (5 - 4));
		out[20 + outpos] = ((in[3 + inpos] >>> 4) & 31);
		out[21 + outpos] = ((in[3 + inpos] >>> 9) & 31);
		out[22 + outpos] = ((in[3 + inpos] >>> 14) & 31);
		out[23 + outpos] = ((in[3 + inpos] >>> 19) & 31);
		out[24 + outpos] = ((in[3 + inpos] >>> 24) & 31);
		out[25 + outpos] = (in[3 + inpos] >>> 29) | ((in[4 + inpos] & 3) << (5 - 2));
		out[26 + outpos] = ((in[4 + inpos] >>> 2) & 31);
		out[27 + outpos] = ((in[4 + inpos] >>> 7) & 31);
		out[28 + outpos] = ((in[4 + inpos] >>> 12) & 31);
		out[29 + outpos] = ((in[4 + inpos] >>> 17) & 31);
		out[30 + outpos] = ((in[4 + inpos] >>> 22) & 31);
		out[31 + outpos] = (in[4 + inpos] >>> 27);
	}

	protected static void fastpack5(final int[] in, int inpos, final int[] out, int outpos) {
		out[0 + outpos] = (in[0 + inpos] & 31) | ((in[1 + inpos] & 31) << 5) | ((in[2 + inpos] & 31) << 10)
				| ((in[3 + inpos] & 31) << 15) | ((in[4 + inpos] & 31) << 20) | ((in[5 + inpos] & 31) << 25)
				| ((in[6 + inpos]) << 30);
		out[1 + outpos] = ((in[6 + inpos] & 31) >>> (5 - 3)) | ((in[7 + inpos] & 31) << 3) | ((in[8 + inpos] & 31) << 8)
				| ((in[9 + inpos] & 31) << 13) | ((in[10 + inpos] & 31) << 18) | ((in[11 + inpos] & 31) << 23)
				| ((in[12 + inpos]) << 28);
		out[2 + outpos] = ((in[12 + inpos] & 31) >>> (5 - 1)) | ((in[13 + inpos] & 31) << 1)
				| ((in[14 + inpos] & 31) << 6) | ((in[15 + inpos] & 31) << 11) | ((in[16 + inpos] & 31) << 16)
				| ((in[17 + inpos] & 31) << 21) | ((in[18 + inpos] & 31) << 26) | ((in[19 + inpos]) << 31);
		out[3 + outpos] = ((in[19 + inpos] & 31) >>> (5 - 4)) | ((in[20 + inpos] & 31) << 4)
				| ((in[21 + inpos] & 31) << 9) | ((in[22 + inpos] & 31) << 14) | ((in[23 + inpos] & 31) << 19)
				| ((in[24 + inpos] & 31) << 24) | ((in[25 + inpos]) << 29);
		out[4 + outpos] = ((in[25 + inpos] & 31) >>> (5 - 2)) | ((in[26 + inpos] & 31) << 2)
				| ((in[27 + inpos] & 31) << 7) | ((in[28 + inpos] & 31) << 12) | ((in[29 + inpos] & 31) << 17)
				| ((in[30 + inpos] & 31) << 22) | ((in[31 + inpos]) << 27);
	}

}
