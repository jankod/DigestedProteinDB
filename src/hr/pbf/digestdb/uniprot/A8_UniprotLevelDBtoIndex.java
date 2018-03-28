package hr.pbf.digestdb.uniprot;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.LevelDButil;
import hr.pbf.digestdb.util.UniprotConfig;
import hr.pbf.digestdb.util.UniprotConfig.Name;

public class A8_UniprotLevelDBtoIndex {
private static final Logger log = LoggerFactory.getLogger(A8_UniprotLevelDBtoIndex.class);

	public static void main(String[] args) throws Throwable {
		makeIndexDefaultPath();
	}
	
	
	public static void makeIndexFromCsv() {
		
	}
	
	public static void makeIndexDefaultPath() throws IOException {
		String levelDbPath = UniprotConfig.get(Name.PATH_TREMB_LEVELDB);
		String indexCSV = levelDbPath + ".index.csv";
		log.debug(levelDbPath);
		log.debug(indexCSV);
		makeIndex(levelDbPath, indexCSV);		
	}

	public static void makeIndex(String pathLevelDb, String pathIndexCsv) throws IOException {
		//String levelDbPath = UniprotConfig.get(Name.PATH_TREMB_LEVELDB);
		//String indexCSV = levelDbPath + ".index.csv";
		BufferedWriter out = BioUtil.newFileWiter(pathIndexCsv, StandardCharsets.US_ASCII.name());

		DBComparator comparator = LevelDButil.getFloatKeyComparator();
		Options opt = LevelDButil.getStandardOptions();
		opt.comparator(comparator);
		DB db = LevelDButil.open(pathLevelDb, opt);
		DBIterator it = db.iterator();
		it.seekToFirst();
		int c = 0;
		out.write("mass\t"+"unique_peptides\t"+"peptides\n");
		while (it.hasNext()) {
			Entry<byte[], byte[]> next = it.next();
			float mass = BiteUtil.toFloat(next.getKey());
			int howUniquePeptides = 0;
			long howPeptides = 0;

			TreeMap<String, List<AccTax>> v = UniprotFormat3.uncompressPeptidesJava(next.getValue());
			Set<Entry<String, List<AccTax>>> entrySet = v.entrySet();

			 howUniquePeptides += entrySet.size();
			for (Entry<String, List<AccTax>> entry : entrySet) {
				howPeptides += entry.getValue().size();
			}
			if(howPeptides > Integer.MAX_VALUE) {
				log.debug("vece je "+ mass);
			}
			out.write(mass + "\t" + howUniquePeptides + "\t"+ howPeptides+"\n");
			if(c++ % 1_000_000 == 0) {
				//break;
				log.debug("Sada "+ c);
			}
		}
		out.close();
		db.close();
		log.debug("Finish index csv na {}", pathIndexCsv);
	}


}
