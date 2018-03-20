package hr.pbf.digestdb.uniprot;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.jasper.tagplugins.jstl.core.ForEach;
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
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
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

	public SortedTableMap<Float, Integer> getMapIndex() {
		return mapIndex;
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

	public List<UniprotModel.PeptideAccTax> searchMass(double mass) throws IOException {
		DBIterator it = db.iterator();
		it.seek(BiteUtil.toBytes((float) mass));
		if (it.hasNext()) {
			Entry<byte[], byte[]> next = it.next();
			float mass2 = BiteUtil.toFloat(next.getKey());
			if (mass != mass2) {
				log.warn("Not the some mass {}:{}", mass, mass2);
				return Collections.emptyList();
			}
			TreeMap<String, List<AccTax>> v = UniprotFormat3.uncompressPeptidesJava(next.getValue());
			Set<Entry<String, List<AccTax>>> entrySet = v.entrySet();
			
			ArrayList<PeptideAccTax> result = new  ArrayList<>();
			for (Entry<String, List<AccTax>> entry : entrySet) {
				List<AccTax> value = entry.getValue();
				for (AccTax accTax : value) {
					result.add(new PeptideAccTax(entry.getKey(), accTax.getAcc(), accTax.getTax()));
				}
			}
			return result;
		}
		return null;
	}

	public SearchResult searchIndex(double from, double to) {

		ConcurrentNavigableMap<Float, Integer> subMap = mapIndex.subMap((float) from, true, (float) to, true);

		// SortedMap<Float, Integer> subMap = indexMap.subMap((float) from, true,
		// (float) to, true);
		SearchResult result = new SearchResult();
		result.totalMass = subMap.size();
		subMap.forEach(new BiConsumer<Float, Integer>() {

			@Override
			public void accept(Float t, Integer u) {
				result.totalPeptides += u;
			}

		});
		result.subMap = subMap;
		return result;
	}

	public static class SearchResult {
		public int totalMass = 0;
		public int totalPeptides = 0;
		public ConcurrentNavigableMap<Float, Integer> subMap;

		@Override
		public String toString() {
			String res = "<br>Total mass: " + totalMass + "<br>Total peptides: " + totalPeptides;

			for (Entry<Float, Integer> set : subMap.entrySet()) {
				res += "<br><a href='showMass.jsp?mass=" + set.getKey() + "'>" + set.getKey() + "</a> : "
						+ set.getValue();
			}
			return res;
		}

	}
}
