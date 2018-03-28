package hr.pbf.digestdb.test.experiments;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;

import com.google.common.primitives.Bytes;

import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.LevelDButil;

public class LevelDbNewAproach {

	public static void main22(String[] args) {
		int i = Float.floatToIntBits(1234.123456789f);
		float f = Float.intBitsToFloat(i);
		System.out.println(i + " " + f);
	}

	public static void main(String[] args) throws IOException {
		Options o = LevelDButil.getStandardOptions();
		o.comparator(new DBComparator() {

			@Override
			public int compare(byte[] o1, byte[] o2) {
				ByteBuffer k1 = ByteBuffer.wrap(o1, 0, 4);
				ByteBuffer k2 = ByteBuffer.wrap(o2, 0, 4);
				float f1 = k1.getFloat();
				float f2 = k2.getFloat();
				System.out.println("Compare " + f1 + " " + f2);
				return Float.compare(f1, f2);
			}

			@Override
			public String name() {
				return "float-int";
			}

			@Override
			public byte[] findShortestSeparator(byte[] start, byte[] limit) {
				System.out.println("findShortestSeparator: " + Arrays.toString(start));
				System.out.println("findShortestSeparator limit: " + Arrays.toString(limit));
				return start;
			}

			@Override
			public byte[] findShortSuccessor(byte[] key) {
				return key;
				// if (key.length == 4) {
				// return key;
				// }
				// if (key.length > 4) {
				// byte[] res = new byte[4];
				// System.arraycopy(key, 0, res, 0, 4);
				// return res;
				// }
				// System.out.println("Kay je ovo " + Arrays.toString(key));
				// return key;
			}
		});
		o.createIfMissing();
		String pathDb = "f:/tmp/lelelDBsample.db";
//		FileUtils.deleteDirectory(new File(pathDb));
		DB db = LevelDButil.open(pathDb, o);
//		 write(db);
		read(db);
		System.out.println("finis");

		db.close();
	}

	private static void read(DB db) throws IOException {
		DBIterator it = db.iterator();
		while (it.hasNext()) {
			Map.Entry<byte[], byte[]> entry = (Map.Entry<byte[], byte[]>) it.next();

			byte[] keyBytes = entry.getKey();
			System.out.println(keyBytes.length);
			float key = ByteBuffer.wrap(keyBytes, 0, 4).getFloat();
			float num = ByteBuffer.wrap(entry.getKey(), 4, 4).getInt();
			System.out.println("key num " + key + " " + num + " value=" + new String(entry.getValue()));
		}
		it.close();
		System.out.println("READ");
	}

	private static void write(DB db) {
		db.put(getTwoBytes(1.1f, 11), "jedan".getBytes());
		db.put(getTwoBytes(2.2f, 22), "dva".getBytes());
		db.put(getTwoBytes(1.2f, 22), "jed dva".getBytes());
		
		System.out.println("write bazu");
	}

	static byte[] getTwoBytes(float key, int num) {
		byte[] a = BiteUtil.toBytes(key);
		byte[] b = BiteUtil.toBytes(num);

		byte[] n = new byte[a.length + b.length];
		System.arraycopy(a, 0, n, 0, a.length);
		System.arraycopy(b, 0, n, a.length, b.length);

		return n;
	}
}
