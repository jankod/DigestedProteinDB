package hr.pbf.digestdb.uniprot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.BiteUtil;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;

public class A5_UniprotCSVtoFormat3 {
	private static final Logger log = LoggerFactory.getLogger(A5_UniprotCSVtoFormat3.class);

	static String pathCsv = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_test.csv";
	static String pathDb = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_text_uncompress.db";
	static String pathIndex = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_test.index";

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		searchDB();
		// createDB();
		// createDBNew();
		// searchMapdb();
	}

	private static void createDBNew() throws UnsupportedEncodingException, IOException {
		int numEntry = 13_353_898;
		UniprotFormat3Creator creator = new UniprotFormat3Creator(pathDb, pathIndex, numEntry);
		try (BufferedReader reader = BioUtil.newFileReader(pathCsv, "ASCII")) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				
				// 500.18015       AGGGCH  A0A1F7PX67:1802111,A0A1F7NB42:1802102
				String[] split = StringUtils.split(line, "\t");
				float mass = Float.parseFloat(split[0]);
				String peptide = split[1];
				String accTax = split[2];
				String[] splitAccTax = StringUtils.split(accTax, ":");
				ArrayList<AccTax> accTaxList = new ArrayList<>(splitAccTax.length);
				for (int i = 0; i < splitAccTax.length; i++) {
					accTaxList.add(new AccTax(splitAccTax[0], Integer.parseInt(splitAccTax[1])));
				}

				creator.putNext(mass, peptide, accTaxList);
			}
			creator.finish();
		}
	}

	//

	private static void searchDB() throws IOException {
		// TreeMap<Float, Long> index = loadIndex();
		// log.debug("min {} max {}", index.firstEntry().getValue(),
		// index.lastEntry().getValue());
		UniprotFormat3 f = new UniprotFormat3(pathDb, pathIndex);
		Map<Float, UniprotModel.PeptideAccTax> result = f.search(1500f, 1500.3f);

	}

	private static TreeMap<Float, Long> loadIndex() throws IOException {
		FileInputStream in = new FileInputStream(new File(pathIndex));
		TreeMap<Float, Long> res = SerializationUtils.deserialize(in);
		in.close();
		return res;
	}

	public static void createDB() throws UnsupportedEncodingException, FileNotFoundException, IOException {

		// Volume volume = MappedFileVol.FACTORY.makeVolume((pathDb + ".mapdb"), false);
		//
		// // open consumer which will feed map with content
		// SortedTableMap.Sink<Float, byte[]> sink = SortedTableMap.create(
		//
		// volume, Serializer.FLOAT, // key serializer
		// Serializer.BYTE_ARRAY // value serializer
		// ).pageSize(64 * 1024 * 16) // set Page Size to 64KB
		// .nodeSize(8) // set Node Size to 8 entries
		// .createFromSink();

		TreeMap<Float, Long> index = new TreeMap<>();
		long lastIndexPos = 0;
		FastBufferedOutputStream db = new FastBufferedOutputStream(new FileOutputStream(pathDb));
		try (BufferedReader reader = BioUtil.newFileReader(pathCsv, "ASCII")) {
			String line = null;

			float lastMass = -1;
			while ((line = reader.readLine()) != null) {
				String[] split = StringUtils.split(line, "\t");
				float mass = Float.parseFloat(split[0]);
				String peptide = split[1];
				String accTax = split[2];

				if (lastMass == -1 || lastMass == mass) {
					buksa(mass, peptide, accTax);

				} else {
					byte[] compress = compress();
					log.debug("put " + lastMass + " " + compress.length);
					// sink.put(lastMass, compress);

					index.put(lastMass, lastIndexPos);
					db.write(BiteUtil.toByte(countUniquePeptide));
					db.write(compress);
					lastIndexPos += compress.length + 4;
					b.setLength(0);
				}
				lastMass = mass;

			}
			// finally open created map
			// SortedTableMap<Float, byte[]> map = sink.create();
			// log.debug("Mapdb "+ map.size());
			// volume.close();

			FileOutputStream outputStreamSer = new FileOutputStream(pathIndex);
			log.debug("index size: " + index.size());
			SerializationUtils.serialize(index, outputStreamSer);
			outputStreamSer.close();
			db.close();
		}
	}

	private static String uncompress(byte[] compressed) throws UnsupportedEncodingException, IOException {
		byte[] uncompress = Snappy.uncompress(compressed);
		return new String(uncompress);
	}

	private static byte[] compress() throws UnsupportedEncodingException, IOException {
		return Snappy.compress(b.toString().getBytes("ASCII"));
		// return b.toString().getBytes("ASCII");
	}

	private static StringBuilder b = new StringBuilder(40000);

	// static FastByteArrayOutputStream cacheByte = new FastByteArrayOutputStream();
	private static int countUniquePeptide = 0;

	private static void buksa(float mass, String peptide, String accTax) {
		countUniquePeptide++;
		b.append(peptide).append("\t").append(accTax).append("\n");
	}

	// private static void searchMapdb() throws UnsupportedEncodingException,
	// IOException {
	// // Existing SortedTableMap can be reopened.
	// // In that case only Serializers needs to be set,
	// // other params are stored in file
	// Volume volume = MappedFileVol.FACTORY.
	//
	// makeVolume(pathDb + ".mapdb", true);
	// // read-only=true
	// SortedTableMap<Float, byte[]> map = SortedTableMap.open(volume,
	// Serializer.FLOAT, Serializer.BYTE_ARRAY);
	//
	// System.out.println("Size: " + map.size());
	// String res = uncompress(map.get(Float.valueOf(500)));
	//
	// System.out.println("500 " + res);
	//
	// map.close();
	//
	// }
}
