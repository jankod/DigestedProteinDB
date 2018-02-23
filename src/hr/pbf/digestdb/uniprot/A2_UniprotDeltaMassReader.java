package hr.pbf.digestdb.uniprot;

import static java.util.stream.Collectors.groupingBy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.collections.impl.set.mutable.primitive.FloatHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.iq80.leveldb.*;
//import static org.fusesource.leveldbjni.JniDBFactory.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;

import hr.pbf.digestdb.uniprot.UniprotModel.KryoFloatHolder;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.BiteUtil;

public class A2_UniprotDeltaMassReader {
	private static final Logger log = LoggerFactory.getLogger(A2_UniprotDeltaMassReader.class);
	private static String pathLevelDb = "";
	private static LevelDbUniprot levelDb;

	public static void main(String[] args) throws IOException {

		// read(615.2F, 615.3f);

		try {
			String dir = "/home/users/tag/uniprot/delta-db";
			String dirKryo = "/home/users/tag/uniprot/delta-kryo";
			pathLevelDb = "/home/users/tag/uniprot/delta-delveldb";
			if (SystemUtils.IS_OS_WINDOWS) {
				dir = "F:\\Downloads\\uniprot\\uniprot_sprot.dat_delta-db1_100000";
				dirKryo = "C:\\tmp\\delta-kryo";
				pathLevelDb = "C:/tmp/delta_leveldb";
			}
			File fLevel = new File(pathLevelDb);
			FileUtils.deleteDirectory(fLevel);
			fLevel.mkdirs();
			
			levelDb = new LevelDbUniprot(pathLevelDb);

			log.debug(dir);
			log.debug(dirKryo);
			log.debug(pathLevelDb);
			toSmallDb(dir, dirKryo);
	
			DB db = levelDb.getDb();
//			db.compactRange(BiteUtil.floatToByteArray(0), BiteUtil.floatToByteArray(6000));
			db.resumeCompactions();
//			String stats = db.getProperty("leveldb.stats");
//			System.out.println(stats);
			long s = FileUtils.sizeOfDirectory(fLevel);
			System.out.println("Level db size: "+ FileUtils.byteCountToDisplaySize(s));
		} finally {
			if (levelDb != null)
				levelDb.close();
		}
		
	
		// FileUtils.sizeOfDirectory(new File(dirKryo));
	}

	private static void toSmallDb(String dirPath, String dirPathKryo) throws IOException {
		File dir = new File(dirPath);
		if (!dir.isDirectory()) {
			throw new RuntimeException("Not a dir: " + dirPath);
		}

		log.debug("start delete " + dirPathKryo);
		File dirKryo = new File(dirPathKryo);
		FileUtils.deleteDirectory(dirKryo);
		dirKryo.mkdirs();
		log.debug("start after delete dir");

		long c = 0;
		File[] listFiles = dir.listFiles();
		DB db = levelDb.getDb();
		WriteBatch batch = db.createWriteBatch();
		boolean batchWriten = false;
		for (File f : listFiles) {
			// log.debug("Parse " + f);
			ArrayList<PeptideAccTax> data = parseDataFile(f);
			Map<Float, List<PeptideAccTax>> res = data.stream().collect(groupingBy(o -> {
				float num = (float) BioUtil.calculateMassWidthH2O(o.getPeptide());
				num = BioUtil.roundToDecimals(num, A1_UniprotTextParser.ROUND_FLOAT_MASS); // round 2 same as tjhe
				return num;
			}));

			Set<Float> keySet = res.keySet();
			ArrayList<Float> sortedMass = new ArrayList<>(keySet);
			sortedMass.sort(Float::compareTo);
			for (Float mass : sortedMass) {
				// if (mass > 1011.4 && mass < 1011.6 ) {
				// log.debug("masa je ova");
				// }

				List<PeptideAccTax> d = res.get(mass);

				d.sort(A2_UniprotDeltaMassReader::compareByPeptideAndAcc);
				// toKryo(mass, d, dirPathKryo);
				toLevelDb(mass, d, batch);
				c++;
				
				if (c % 50 == 0) {
					db.write(batch);
					batch.close();
					batch = db.createWriteBatch();
					batchWriten = true;
				} else {
					batchWriten = false;
				}
				
			}
		}
		if (!batchWriten) {
			db.write(batch);
		}

		batch.close();
		
//		levelDb.getDb().write(batch);
//		batch.close();
		log.debug("Floata zapisao "+ c);
		
	}

	private static void toLevelDb(Float mass, List<PeptideAccTax> d, WriteBatch batch) throws IOException {
		byte[] peptidesBytes = levelDb.peptidesToBytes(d);
		byte[] floatBytes = levelDb.floatToBytes(mass);
		DB db = levelDb.getDb();
//		WriteBatch batch = db.createWriteBatch();
		batch.put(floatBytes, peptidesBytes);
		db.write(batch);
//		batch.close();
//		db.put(floatBytes, peptidesBytes);
		

	}

	private static int compareByPeptideAndAcc(PeptideAccTax o1, PeptideAccTax o2) {
		return Comparator.comparing(PeptideAccTax::getPeptide).thenComparing(PeptideAccTax::getAcc).compare(o1, o2);
	}

	public static File toKryo(float mass, List<PeptideAccTax> d, String dir) throws IOException {

		OutputStream outputStream;
		File file = new File(dir, mass + ".db");
		if (file.exists()) {
			throw new RuntimeException("Exist " + mass);
		}
		// log.debug("Radim "+ mass);
		outputStream = new DeflaterOutputStream(new FileOutputStream(file));
		// Output output = new Output(outputStream);
		FastOutput output = new FastOutput(outputStream);
		Kryo kryo = new Kryo();
		kryo.writeObject(output, new KryoFloatHolder(d));
		output.close();
		outputStream.close();
		return file;
	}

	public static List<PeptideAccTax> fromKryo(File f) throws IOException {
		// mass = BioUtil.roundToDecimals(mass, A1_UniprotTextParser.ROUND_FLOAT_MASS);
		// File f = new File(dir, mass + ".db");
		InputStream in = new InflaterInputStream(new FileInputStream(f), new Inflater(), 512 * 16);
		// Input input = new Input(in);
		FastInput input = new FastInput(in);
		Kryo k = new Kryo();
		KryoFloatHolder res = k.readObject(input, KryoFloatHolder.class);
		in.close();
		input.close();
		return res.getData();

	}

	private static ArrayList<PeptideAccTax> parseDataFile(File f) throws IOException {
		DataInputStream in = BioUtil.newDataInputStream(f.getAbsolutePath());
		ArrayList<PeptideAccTax> result;
		try {
			result = new ArrayList<>();
			while (in.available() != 0) {
				String peptide = in.readUTF();
				int tax = in.readInt();
				String acc = in.readUTF();
				PeptideAccTax p = new PeptideAccTax(peptide, acc, tax);
				result.add(p);
			}
		} finally {
			IOUtils.closeQuietly(in);
		}
		return result;

	}

}
