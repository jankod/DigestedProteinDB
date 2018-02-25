package hr.pbf.digestdb.uniprot;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.util.BiteUtil;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

import org.apache.commons.io.FileUtils;
import org.iq80.leveldb.*;
//import static org.fusesource.leveldbjni.JniDBFactory.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;

import java.io.*;

public class LevelDbUniprot {
	private static final Logger log = LoggerFactory.getLogger(LevelDbUniprot.class);
	private DB db;

	public LevelDbUniprot(String pathLevelDb) throws IOException {

		File f = new File(pathLevelDb);
		
		log.debug("Radim bazu na "+ f.getAbsolutePath());
		Options options = new Options();
		options.blockSize(8 * 1024);
//		options.verifyChecksums(true);
//		options.paranoidChecks(true);
		options.createIfMissing(true);
		options.compressionType(CompressionType.SNAPPY);
		options.cacheSize(100 * 1048576); // 100 * 1048576= 100MB cache
		options.writeBufferSize(10 * 1048576);

		DBComparator comparator = new DBComparator() {
			public int compare(byte[] key1, byte[] key2) {
				// log.debug("key1 "+ key1.length + " key2 "+ key2.length);
				float f1 = BiteUtil.byteArrayToFloat(key1);
				float f2 = BiteUtil.byteArrayToFloat(key2);
				return Float.compare(f1, f2);
			}

			public String name() {
				return "f";
			}

			public byte[] findShortestSeparator(byte[] start, byte[] limit) {
				return start;
			}

			public byte[] findShortSuccessor(byte[] key) {
				return key;
			}

		};
		options.comparator(comparator);
		options.logger(new org.iq80.leveldb.Logger() {

			@Override
			public void log(String message) {
				System.out.println(message);
			}
		});

		db = factory.open(f, options);

	}

	public void close() {
		if (db != null) {
			try {
				db.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	public DB getDb() {
		return db;
	}

	// public void add(Float mass, List<PeptideAccTax> peptides) throws IOException
	// {
	// byte[] massBytes = BiteUtil.floatToByteArray(mass);
	// byte[] pepBytes = peptidesToBytes(peptides);
	// }

	public byte[] floatToBytes(float f) {
		return BiteUtil.floatToByteArray(f);
	}

	public List<PeptideAccTax> bytesToPeptides(byte[] b) throws IOException {
//		if(true) {
//			Kryo k = new Kryo();
//			@SuppressWarnings("unchecked")
//			List<PeptideAccTax> o = k.readObject(new Input(b), ArrayList.class);
//			return o;
//		}
		DataInputStream in = new DataInputStream(new FastByteArrayInputStream(b));
		int how = in.readInt();
		ArrayList<PeptideAccTax> result = new ArrayList<>(how);
		for (int i = 0; i < how; i++) {
			PeptideAccTax p = new PeptideAccTax();
			p.setPeptide(in.readUTF());
			result.add(i, p);
		}
		for (int i = 0; i < how; i++) {
			result.get(i).setAcc(in.readUTF());
		}
		for (int i = 0; i < how; i++) {
			result.get(i).setTax(in.readInt());
		}
		return result;
	}

	public byte[] peptidesToBytes(List<PeptideAccTax> peptides) throws IOException {
//		if(true) {
//			Kryo k = new Kryo();
////			List<PeptideAccTax> o = k.readObject(new Input(b), List.class);
//			ByteArrayOutputStream outb = new ByteArrayOutputStream();
//			Output output = new Output(outb);
//			k.writeObject(output, peptides);
//			output.flush();
//			output.close();
//			return outb.toByteArray();
//		}
		
		FastByteArrayOutputStream out = new FastByteArrayOutputStream(peptides.size() * 22);
		
		MyDataOutputStream d = new MyDataOutputStream(out);

		d.writeInt(peptides.size());
		for (PeptideAccTax pep : peptides) {
			// b.writeUTF(pep.getPeptide().getBytes(StandardCharsets.US_ASCII));
			d.writeUTF(pep.getPeptide());
			
		}
		for (PeptideAccTax pep : peptides) {
			// b.write(pep.getAcc().getBytes(StandardCharsets.US_ASCII));
			d.writeUTF(pep.getAcc());
		}
		for (PeptideAccTax pep : peptides) {
			// b.write(pep.getTax());
			d.writeInt(pep.getTax());
		}
		return out.array;
	}

	public void printStatus() {
		String s = getDb().getProperty("leveldb.stats");
		System.out.println(s);
	}

}
