package hr.pbf.digestdb.uniprot;

import static java.util.stream.Collectors.groupingBy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.IOUtils;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import hr.pbf.digestdb.uniprot.UniprotModel.KryoFloatHolder;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MassRangeMap;

public class UniprotDeltaMassReader {
	private final static double DELTA = 0.3;
	final static int fromMass = 500;
	final static int toMass = 6000;
	static final MassRangeMap massPartsMap = new MassRangeMap(DELTA, fromMass, toMass);
	// private static HashMap<String, DataInputStream> massStreamMap = new
	// HashMap<>();

	private static String mainfolder = "F:\\Downloads\\uniprot";
	static String deltaDbPath = mainfolder + "/delta-db/";
	static String kryoDir = mainfolder + File.separator + "kryo";

	private static final Logger log = LoggerFactory.getLogger(UniprotDeltaMassReader.class);

	public static void main(String[] args) throws IOException {

		read(615.2F, 615.3f);

		Options options = new Options();
		options.createIfMissing(true);
		// DB db = factory.open(new File("example"), options);

		toLevelDb("dir_db");
	}

	private static void toLevelDb(String dirPath) throws IOException {
		File dir = new File(dirPath);
		if (!dir.isDirectory()) {
			throw new RuntimeException("Not a dir: " + dirPath);
		}

		File[] listFiles = dir.listFiles();
		for (File f : listFiles) {
			log.debug("Parse " + f);

			ArrayList<PeptideAccTax> data = parseDataFile(f);
			makeLevelDb(data);
		}
	}

	private static void makeLevelDb(ArrayList<PeptideAccTax> data) throws FileNotFoundException {
		// data.sort(UniprotDeltaMassReader::sortByPeptide);

		Map<Float, List<PeptideAccTax>> res = data.stream().collect(groupingBy(o -> {
			return (float) BioUtil.calculateMassWidthH2O(o.getPeptide());
		}));

		Set<Float> keySet = res.keySet();
		for (Float mass : keySet) {
			List<PeptideAccTax> d = res.get(mass);

			toKryo(mass, d);

		}

		// putToLevelDB()

		// mass float -> peptide -> acc -> taxid
	}

	public static void toKryo(float mass, List<PeptideAccTax> d) throws FileNotFoundException {

		OutputStream outputStream;
		outputStream = new DeflaterOutputStream(new FileOutputStream(new File(kryoDir, mass + ".db")));
		// Output output = new Output(outputStream);
		FastOutput output = new FastOutput(outputStream);
		Kryo kryo = new Kryo();
		kryo.writeObject(output, d);
		output.close();
	}

	public static List<PeptideAccTax> fromKryo(float mass) throws IOException {
		InputStream in = new InflaterInputStream(new FileInputStream(new File(kryoDir, mass + ".db")));
		// Input input = new Input(in);
		FastInput input = new FastInput(in);
		Kryo k = new Kryo();
		KryoFloatHolder res = k.readObject(input, KryoFloatHolder.class);
		in.close();
		return res.getData();

	}

	private static ArrayList<PeptideAccTax> parseDataFile(File f) throws IOException {
		DataInputStream in = BioUtil.newDataInputStream(f.getAbsolutePath());
		ArrayList<PeptideAccTax> result = new ArrayList<>();
		while (in.available() != 0) {
			String peptide = in.readUTF();
			String acc = in.readUTF();
			int tax = in.readInt();
			PeptideAccTax p = new PeptideAccTax(peptide, acc, tax);
			result.add(p);
		}
		return result;

	}

	private static void read(float from, float to) throws IOException {
		String fileName = massPartsMap.getFileName(from);
		DataInputStream in = BioUtil.newDataInputStream(deltaDbPath + fileName + ".db");

		int c = 0;
		while (in.available() != 0) {
			String peptide = in.readUTF();
			int tax = in.readInt();
			String acc = in.readUTF();
			log.debug("{} {} {}", peptide, tax, acc);
			c++;
		}
		log.debug("stavki " + c);
		log.debug(fileName);
		in.close();
	}

}
