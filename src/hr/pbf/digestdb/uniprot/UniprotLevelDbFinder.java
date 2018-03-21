package hr.pbf.digestdb.uniprot;

import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxNames;
import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.LevelDButil;
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

import com.linkedin.paldb.api.NotFoundException;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentNavigableMap;

public class UniprotLevelDbFinder implements Closeable {

	private DB db;
	// private TreeMap<Float, Integer> indexMap;
	private static final Logger log = LoggerFactory.getLogger(UniprotLevelDbFinder.class);
	private SortedTableMap<Float, Integer> mapIndex;
	// private StoreReader protNameReader;
	// private SortedTableMap<String, String> mapAccProtName;
	private DB dbProtName;

	public static void main(String[] args) throws IOException {
		try (UniprotLevelDbFinder f = new UniprotLevelDbFinder("F:\\tmp\\trembl.leveldb",
				"C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl.leveldb.index.compact")) {

			float fromMass = 3731.7937F;
			float toMass = (float) (fromMass + 0.73421);
			SearchIndexResult result = f.searchIndex(fromMass, toMass);
			// log.debug("total mass {}, peptides: {}", result.totalMasses,
			// result.totalPeptides);
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

		String protNamePalPath = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_prot_names.leveldb";
		// "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_prot_names.leveldb.paldb";

		// open existing memory-mapped file in read-only mode

		// read-only=true

		dbProtName = LevelDButil.open(protNamePalPath, LevelDButil.getStandardOptions());

		// mapAccProtName =
		// SortedTableMap.open(MappedFileVol.FACTORY.makeVolume(protNamePalPath, true),
		// Serializer.STRING, Serializer.STRING);

		//
		// File file = new File(protNamePalPath);
		// if (!file.exists()) {
		// String errMsg = "Not find path " + file;
		// log.error(errMsg);
		// throw new FileNotFoundException(errMsg);
		// }
		// protNameReader = PalDB.createReader(file);
		// protNameReader.getConfiguration().set("compression.enabled", "true");
	}

	public String getProtName(String acc) {
		// return protNameReader.getString(acc);
		// return mapAccProtName.get(acc);
		DBIterator it = dbProtName.iterator();
		it.seek(acc.getBytes(StandardCharsets.US_ASCII));
		if (it.hasNext()) {
			Entry<byte[], byte[]> entry = it.next();
			String accFounded = new String(entry.getKey(), StandardCharsets.US_ASCII);
			if (!accFounded.equals(acc)) {
				log.warn("Not find: {} {}", acc, accFounded);
				return null;
			}
			String protName = new String(entry.getValue(), StandardCharsets.US_ASCII);
			return protName;
		}
		return null;
		// return new String(r);
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

		if (db != null) {
			db.close();
		}
		log.debug("CLOSE regular database");
	}

	public List<UniprotModel.PeptideAccTaxNames> searchMass(double mass) throws IOException {
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

			List<PeptideAccTaxNames> result = new ArrayList<>();
			for (Entry<String, List<AccTax>> entry : entrySet) {
				List<AccTax> value = entry.getValue();
				String peptide = entry.getKey();
				for (AccTax accTax : value) {
					String acc = accTax.getAcc();
					int tax = accTax.getTax();
					PeptideAccTaxNames p = new PeptideAccTaxNames();
					p.setAcc(acc);
					p.setTax(tax);
					p.setPeptide(peptide);
					p.setProtName(getProtName(acc));
					result.add(p);
					// result.add(new PeptideAccTax(peptide, acc, tax));
				}
			}
			return result;
		}
		log.warn("Nothig found for mass: " + mass);
		return null;
	}

	public SearchIndexResult searchIndex(double from, double to) {

		ConcurrentNavigableMap<Float, Integer> subMap = mapIndex.subMap((float) from, true, (float) to, true);

		// SortedMap<Float, Integer> subMap = indexMap.subMap((float) from, true,
		// (float) to, true);
		SearchIndexResult result = new SearchIndexResult();

		result.subMap = subMap;
		return result;
	}

	public static class SearchIndexResult {
		public ConcurrentNavigableMap<Float, Integer> subMap;

		public int countMasses() {
			if (subMap == null) {
				return 0;
			}
			return subMap.size();
		}

		public long countPeptides() {
			long c = 0;
			Set<Entry<Float, Integer>> entrySet = subMap.entrySet();
			for (Entry<Float, Integer> entry : entrySet) {
				c += entry.getValue();
			}
			return c;
		}

		@Override
		public String toString() {
			String res = "<br>Total mass: " + countMasses() + "<br>Total peptides: " + countPeptides();

			for (Entry<Float, Integer> set : subMap.entrySet()) {
				res += "<br><a href='showMass.jsp?mass=" + set.getKey() + "'>" + set.getKey() + "</a> : "
						+ set.getValue();
			}
			return res;
		}

	}
}
