package hr.pbf.digestdb.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class MapdbPeptideDatabase {

    String DIR = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_human/";
    String dbPath = DIR + "mapdb_sstable.db";
    String groupedCsvPath = DIR + "grouped_with_ids.csv";


    public static void main(String[] args) {
        // read from console two number and perfom search
        MapdbPeptideDatabase db = new MapdbPeptideDatabase();
        SortedTableMap<Double, String> maps = db.openForSearch();
        System.out.println("Enter two numbers separated by space. Enter 'stop' to stop");
        Console console = System.console();
        while (true) {
            String line = console.readLine();
            if("stop".equals(line)) {
                log.info("Stoping");
                break;
            }
            String[] parts = line.split(" ");
            double mass1 = Double.parseDouble(parts[0]);
            double mass2 = Double.parseDouble(parts[1]);
            List<Map.Entry<Double, String>> result = db.search(mass1, mass2, maps);
            for (Map.Entry<Double, String> entry : result) {
                System.out.println( entry.getKey() + ": " + entry.getValue());
//                log.info("mass: " + entry.getKey() + " seqAccs: " + entry.getValue());
            }
        }

        maps.close();


    }

    public static void main1(String[] args) throws Exception {

        MapdbPeptideDatabase db = new MapdbPeptideDatabase();
        //List<Map.Entry<Double, String>> result = db.search(1705.1, 1705.8);

//         for (Map.Entry<Double, String> entry : results) {
//            log.info("mass: " + entry.getKey() + " seqAccs: " + entry.getValue());
//        }


//        FileUtils.delete(new java.io.File(db.dbPath));
//        db.buildDb();

    }

    public SortedTableMap<Double, String> openForSearch() {
        Volume volume = MappedFileVol.FACTORY.makeVolume(dbPath, true);

        SortedTableMap<Double, String> map =
              SortedTableMap.open(
                    volume,
                    Serializer.DOUBLE,
                    Serializer.STRING
              );
        return map;
    }

    public List<Map.Entry<Double, String>> search(double mass1, double mass2, SortedTableMap<Double, String> map) {

        // Binary search
        StopWatch watch = StopWatch.createStarted();
        List<Map.Entry<Double, String>> results = searchRange(map, mass1, mass2);
        log.info("Search time milisec " + watch.getTime());
        log.info("Results: " + results.size());

     //   map.close();

        return results;


    }

    public List<Map.Entry<Double, String>> searchRange(SortedTableMap<Double, String> map, double mass1, double mass2) {
        List<Map.Entry<Double, String>> results = new ArrayList<>();
        // Počni s ključem koji je >= mass1
        Map.Entry<Double, String> entry = map.findHigher(mass1, true);
        // Iteriraj dok je ključ manji ili jednak mass2
        while (entry != null && entry.getKey() <= mass2) {
            results.add(entry);
            // Dohvati sljedeći viši ključ (isključivo) od trenutnog
            entry = map.findHigher(entry.getKey(), false);
        }
        return results;
    }

    public void buildDb() throws IOException {

        Volume volume = MappedFileVol.FACTORY.makeVolume(dbPath, false);

        SortedTableMap.Sink<Double, String> sink =
              SortedTableMap.create(
                    volume,
                    Serializer.DOUBLE,
                    Serializer.STRING
              ).createFromSink();


        BufferedReader reader = new BufferedReader(new FileReader(groupedCsvPath));
        String line;

        while ((line = reader.readLine()) != null) {
            // 503.234,SGAGAAA:15-SAAGGAA:14-TGAAAGG:16
            String[] parts = line.split(",", 2);
            if (parts.length < 2) throw new IllegalArgumentException("Invalid input CSV format " + line);

            double mass = Double.parseDouble(parts[0]);
            String seqAccs = parts[1];

            sink.put(mass, seqAccs);
        }
        SortedTableMap<Double, String> map = sink.create();
        log.info("Created map with size: " + map.size());
        map.close();


//            //feed content into consumer
//            int key = 0;
//            key< 100000;key++)
//
//            {
//                sink.put(key, "value" + key);
//            }
//
//            // finally open created map
//            SortedTableMap<Integer, String> map = sink.create();


    }
}
