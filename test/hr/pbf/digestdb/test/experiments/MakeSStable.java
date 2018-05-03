package hr.pbf.digestdb.test.experiments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import hr.pbf.digestdb.uniprot.UniprotModel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.SortedTableMap.Sink;
import org.mapdb.serializer.SerializerCompressionWrapper;
import org.mapdb.volume.ByteBufferMemoryVol;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.uniprot.UniprotCSVformat;
import hr.pbf.digestdb.uniprot.UniprotFormat3;
import hr.pbf.digestdb.uniprot.UniprotCSVformat.CallbackReadLine;
import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideMassAccTaxList;

public class MakeSStable {
	private static final Logger log = LoggerFactory.getLogger(MakeSStable.class);

	public static void main(String[] args) throws IOException {

		 createSSTable();

		//readSSTable();
	}

	private static void readSSTable() throws IOException {
		String pathDb = "F:\\ProteinReader\\UniprotDBfiles\\trembl_for_test.csv.sstable";

		// create memory mapped volume
		Volume volume = MappedFileVol.FACTORY.makeVolume(pathDb + ".sstable", true);
		SortedTableMap<Float, byte[]> map = SortedTableMap.open(volume, Serializer.FLOAT,
				new SerializerCompressionWrapper<>(Serializer.BYTE_ARRAY));

		// Entry<Float, byte[]> hi = map.findHigher(500f, true);
		Iterator<Float> it = map.keyIterator();
		int c = 0;
		while (it.hasNext()) {
			Float mass = (Float) it.next();
			if (c++ > 20) {
				break;
			}
			
			log.debug(mass + " " +  UniprotFormat3.uncompressPeptidesJava(map.get(mass)) );
		}
		
		c = 0;
		Iterator<Entry<Float, byte[]>> entryIterator = map.entryIterator();
		while (entryIterator.hasNext()) {
			Map.Entry<java.lang.Float, byte[]> entry = (Map.Entry<java.lang.Float, byte[]>) entryIterator.next();
			if(c++ > 20) {
				break;
			}
			Float mass = entry.getKey();
			byte[] value = entry.getValue();
			TreeMap<String, List<AccTax>> pep = UniprotFormat3.uncompressPeptidesJava(value);
			log.debug(mass + " " + pep);
		}
		map.close();
		
	}

	static float	lastMass			= -1;
	static boolean	writenLastResult	= false;

	private static void createSSTable() throws IOException {

		String pathDb = "F:\\ProteinReader\\UniprotDBfiles\\trembl_for_test.csv.sstable";

		// create memory mapped volume
		//Volume volume = MappedFileVol.FACTORY.makeVolume(pathDb + ".sstable", false);
		Volume volume = ByteBufferMemoryVol.FACTORY.makeVolume(pathDb + ".sstable", false);

		// open consumer which will feed map with content
		SortedTableMap.Sink<Float, byte[]> sink = SortedTableMap
				.create(volume, Serializer.FLOAT, new SerializerCompressionWrapper<>(Serializer.BYTE_ARRAY))
                .pageSize(16*1024 ) // set Page Size to 64KB
                .nodeSize(128)       // set Node Size to 8 entries
				.createFromSink();

		UniprotCSVformat csv = new UniprotCSVformat("F:\\ProteinReader\\UniprotDBfiles\\trembl_for_test.csv");
		csv.readLines(new CallbackReadLine() {

			@Override
			public void readedOne(PeptideMassAccTaxList result) {
				// creator.putNext(result.getMass(), result.getPeptide(), result.getAccTaxs());

				saveToMemory(result);
				if (lastMass == -1 || lastMass == result.getMass()) {
					writenLastResult = false;
				} else {

					write(sink, lastMass);
					writenLastResult = true;
					pepatidesOfOneMass.clear();
				}
				lastMass = result.getMass();

			}

		});

		if (!writenLastResult) {
			write(sink, lastMass);
			// log.debug("Jos write " + result.getMass());
		}

		// finally open created map
		SortedTableMap<Float, byte[]> map = sink.create();
		volume.close();
		log.debug("map " + map.size());
		map.close();
		log.debug("FINISH");
	}

	protected static void write(Sink<Float, byte[]> sink, float lastMass) {
		sink.put(lastMass, format());

	}

	private static TreeMap<String, List<AccTax>> pepatidesOfOneMass = new TreeMap<>();

	protected static void saveToMemory(PeptideMassAccTaxList result) {
		if (pepatidesOfOneMass.containsKey(result.getPeptide())) {
			throw new RuntimeException("Something wrong, contain peptide: " + result.getPeptide());
		}
		// log.debug(result.getMass() + " "+ result.getPeptide() + " "+
		// result.getAccTaxs());
		pepatidesOfOneMass.put(result.getPeptide(), result.getAccTaxs());
	}

	private static byte[] format() {
		try {
			byte[] data = UniprotFormat3.compressPeptidesJava(pepatidesOfOneMass);
			log.debug("Compress "+ pepatidesOfOneMass.size() + " "+ data.length);
			return data;
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}

		// StringBuilder builder = new StringBuilder(peptide);
		// for (UniprotModel.AccTax accTax : accTaxs) {
		// builder.append(",");
		// builder.append(accTax.getAcc());
		// builder.append(":");
		// builder.append(accTax.getTax());
		// }
		// return builder.toString();
	}
}
