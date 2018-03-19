package hr.pbf.digestdb.test.experiments;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.uniprot.MassIndex;

public class H2ArrayIndex {

	static String indexPath = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl.leveldb.index.compact";

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {

		search();
		if (true) {
			return;
		}
		MassIndex i = MassIndex.load(indexPath);

		Volume volume = MappedFileVol.FACTORY.makeVolume(indexPath + ".mapdb", false);

		// open consumer which will feed map with content
		SortedTableMap.Sink<Float, Integer> sink = SortedTableMap.create(volume, Serializer.FLOAT, Serializer.INTEGER)
				.createFromSink();

		// feed content into consumer
		Set<Entry<Float, Integer>> entrySet = i.getMap().entrySet();
		for (Entry<Float, Integer> entry : entrySet) {
			sink.put(entry.getKey(), entry.getValue());
		}
		// sink.put(key, "value"+key);

		// finally open created map
		SortedTableMap<Float, Integer> map = sink.create();
		System.out.println("Napravljeno " + map.size());
		System.out.println("Prije " + i.getMap().size());

		// Class.forName("org.h2.Driver");
		// Connection conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
		// add application code here

		// conn.close();

	}

	private static final Logger log = LoggerFactory.getLogger(H2ArrayIndex.class);

	private static void search() {
		StopWatch s = new StopWatch();
		s.start();
		Volume volume = MappedFileVol.FACTORY.makeVolume(indexPath + ".mapdb", true);
		// read-only=true
		SortedTableMap<Float, Integer> map = SortedTableMap.open(volume, Serializer.FLOAT, Serializer.INTEGER);
		ConcurrentNavigableMap<Float, Integer> subMap = map.subMap(1600f, 1608f);
		Set<Entry<Float, Integer>> entrySet = subMap.entrySet();
		s.stop();
		for (Entry<Float, Integer> entry : entrySet) {
			System.out.println(entry.getKey() + " "+ entry.getValue());
		}
		log.debug(DurationFormatUtils.formatDurationHMS(s.getTime()));
		System.out.println(subMap.size());

		map.close();
	}
}
