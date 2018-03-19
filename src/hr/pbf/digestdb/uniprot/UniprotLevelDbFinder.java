package hr.pbf.digestdb.uniprot;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.SerializationUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;

import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.LevelDButil;

public class UniprotLevelDbFinder implements Closeable {

	private DB db;
	// private TreeMap<Float, Integer> indexMap;
	private static final Logger log = LoggerFactory.getLogger(UniprotLevelDbFinder.class);
	private SortedTableMap<Float, Integer> mapIndex;

	public static void main(String[] args) throws IOException {
		try (UniprotLevelDbFinder f = new UniprotLevelDbFinder("F:\\tmp\\trembl.leveldb",
				"C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl.leveldb.index.compact")) {

			float fromMass = 3731.7937F;
			float toMass = (float) (fromMass + 0.73421);
			SearchResult result = f.searchIndex(fromMass, toMass);
			log.debug("total mass {}, peptides: {}", result.totalMass, result.totalPeptides);
			DBIterator it = f.db.iterator();

			it.seek(BiteUtil.toBytes(fromMass));
			int countMass = 0;
			int countPeptides = 0;
			while (it.hasNext()) {
				Map.Entry<byte[], byte[]> entry = (Map.Entry<byte[], byte[]>) it.next();
				float mass = BiteUtil.toFloat(entry.getKey());
				if (mass > toMass) {
					break;
				}
				countMass++;
				TreeMap<String, List<AccTax>> v = UniprotFormat3.uncompressPeptidesJava(entry.getValue());
				countPeptides += v.size();
			}
			log.debug("total mass {}, peptides: {}", countMass, countPeptides);
			it.close();
		}

	}

	public UniprotLevelDbFinder(String levelDbPath, String indexSSTablePath) throws IOException {
		DBComparator comparator = LevelDButil.getJaComparator();
		Options opt = LevelDButil.getStandardOptions();
		opt.comparator(comparator);
		db = LevelDButil.open(levelDbPath, opt);

		Volume volume = MappedFileVol.FACTORY.makeVolume(indexSSTablePath, true);
		mapIndex = SortedTableMap.open(volume, Serializer.FLOAT, Serializer.INTEGER);

	}

	@Override
	public void close() throws IOException {
		if (db != null) {
			db.close();
		}
		if (mapIndex != null) {
			mapIndex.close();
		}
		log.debug("CLOSE regular database");
	}

	public SearchResult searchIndex(double from, double to) {

		ConcurrentNavigableMap<Float, Integer> subMap = mapIndex.subMap((float)from, true, (float)to, true);

//		SortedMap<Float, Integer> subMap = indexMap.subMap((float) from, true, (float) to, true);
		SearchResult result = new SearchResult();
		result.totalMass = subMap.size();
		subMap.forEach(new BiConsumer<Float, Integer>() {

			@Override
			public void accept(Float t, Integer u) {
				result.totalPeptides += u;
			}

		});
		return result;
	}

	public static class SearchResult {
		public int totalMass = 0;
		public int totalPeptides = 0;

	}
}
