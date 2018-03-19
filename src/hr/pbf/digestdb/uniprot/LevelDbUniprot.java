package hr.pbf.digestdb.uniprot;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.util.BiteUtil;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class LevelDbUniprot {
	private static final Logger log = LoggerFactory.getLogger(LevelDbUniprot.class);
	private DB db;

	public LevelDbUniprot(String pathLevelDb) throws IOException {

		File f = new File(pathLevelDb);
		
//		log.debug("Radim bazu na "+ f.getAbsolutePath());
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
				float f1 = BiteUtil.toFloat(key1);
				float f2 = BiteUtil.toFloat(key2);
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

		
		db = Iq80DBFactory.factory.open(f, options);

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


	public byte[] floatToBytes(float f) {
		return BiteUtil.toBytes(f);
	}

	public List<PeptideAccTax> bytesToPeptides(byte[] b) throws IOException {
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
