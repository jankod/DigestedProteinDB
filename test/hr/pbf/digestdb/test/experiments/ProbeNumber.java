package hr.pbf.digestdb.test.experiments;

import java.io.IOException;

import org.mapdb.DataInput2;
import org.mapdb.DataInput2.ByteBuffer;
import org.mapdb.DataOutput2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProbeNumber {
	private static final Logger log = LoggerFactory.getLogger(ProbeNumber.class);

	
	public static void main(String[] args) throws IOException {
		ByteBuffer in = new ByteBuffer(java.nio.ByteBuffer.allocate(5), 0);
		
		DataOutput2 out = new DataOutput2();
//		out.writeChar('A');
		out.packInt('A');
		out.packInt('A');
		out.packInt('A');
		out.packInt('A');
		out.packInt('A');
		out.packInt('A');
		System.out.println(out.copyBytes().length);
	}
	public static void main2(String[] args) {
		double d = 6172.12345678912345d;
		log.debug("dela");
		int p = (int) (d * 100_000);
		System.out.println(p);
		System.out.println((float) p);
		
		
	}

}
