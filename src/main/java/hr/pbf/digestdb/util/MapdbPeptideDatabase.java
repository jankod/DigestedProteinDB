package hr.pbf.digestdb.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;

import java.io.*;
import java.util.*;

@Slf4j
public class MapdbPeptideDatabase {

    String DIR = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_human/";
    String dbPath = DIR + "mapdb_sstable.db";
    String groupedCsvPath = DIR + "grouped_with_ids.csv";


    public static void main(String[] args) {
        // read from console two number and perfom search
        MapdbPeptideDatabase db = new MapdbPeptideDatabase();
        SortedTableMap<Long, String> maps = db.openForSearch();
        System.out.println("Enter two numbers separated by space. Enter 'stop' to stop");
        Console console = System.console();
        while (true) {
            String line = console.readLine();
            if ("stop".equals(line)) {
                log.info("Stoping");
                break;
            }
            String[] parts = line.split(" ");
            double mass1 = Double.parseDouble(parts[0]);
            double mass2 = Double.parseDouble(parts[1]);
            StopWatch watch = StopWatch.createStarted();
            List<Map.Entry<Double, String>> result = db.search(mass1, mass2, maps);
            log.info("Search time milisec " + watch.getTime());
            log.info("Results time: " + result.size());
            for (Map.Entry<Double, String> entry : result) {
                  System.out.println( entry.getKey() + ": " + entry.getValue());
//                log.info("mass: " + entry.getKey() + " seqAccs: " + entry.getValue());
            }
        }

        maps.close();


    }

    public void startBuild() throws Exception {
        if (new File(dbPath).exists()) {
            log.info("DB already exists: " + dbPath);
            return;
        }
        if (!new File(groupedCsvPath).exists()) {
            throw new RuntimeException("File not found: " + groupedCsvPath);
        }
        MapdbPeptideDatabase db = new MapdbPeptideDatabase();
        db.buildDb();

    }

    public SortedTableMap<Long, String> openForSearch() {
        Volume volume = MappedFileVol.FACTORY.makeVolume(dbPath, true);

        SortedTableMap<Long, String> map =
              SortedTableMap.open(
                    volume,
                    Serializer.LONG,
                    Serializer.STRING
              );
        return map;
    }

    public List<Map.Entry<Double, String>> search(double mass1, double mass2, SortedTableMap<Long, String> map) {

        return searchRange(map, mass1, mass2);


    }

    public List<Map.Entry<Double, String>> searchRange(SortedTableMap<Long, String> map, double mass1, double mass2) {
        long mass1long = Double.doubleToLongBits(mass1);
        long mass2long = Double.doubleToLongBits(mass2);
        SortedMap<Long, String> submap = map.subMap(mass1long, true, mass2long, true);
        List<Map.Entry<Double, String>> results = new ArrayList<>();
        for (Map.Entry<Long, String> entry : submap.entrySet()) {
            results.add(new AbstractMap.SimpleEntry<>(Double.longBitsToDouble(entry.getKey()), entry.getValue()));
        }
        return results;
    }

    public void buildDb() throws IOException {

        Volume volume = MappedFileVol.FACTORY.makeVolume(dbPath, false);

        SortedTableMap.Sink<Long, String> sink =
              SortedTableMap.create(
                          volume,
                          Serializer.LONG,
                          Serializer.STRING
                    )
                    //.pageSize(1024 * 1024)  // 1MB default 1,048,576 bytes
                    .nodeSize(128)  // 32 default

                    .createFromSink();


        BufferedReader reader = new BufferedReader(new FileReader(groupedCsvPath));
        String line;

        while ((line = reader.readLine()) != null) {
            // 503.234,SGAGAAA:15-SAAGGAA:14-TGAAAGG:16
            String[] parts = line.split(",", 2);
            if (parts.length < 2) throw new IllegalArgumentException("Invalid input CSV format " + line);

            double mass = Double.parseDouble(parts[0]);
            long massLong = Double.doubleToLongBits(mass);
            String seqAccs = parts[1];

            sink.put(massLong, seqAccs);
        }
        SortedTableMap<Long, String> map = sink.create();
        log.info("Created map with size: " + map.size());
        map.close();


    }
}
