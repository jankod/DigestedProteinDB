package hr.pbf.digestdb.demo;

import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;

import java.io.*;
import java.util.*;

@Data
@Slf4j
public class MapdbPeptideDatabase {

    private String dbPath = "";
    private String groupedCsvPath = "";

    public static void main(String[] args) throws Exception {
        MapdbPeptideDatabase db = new MapdbPeptideDatabase();


        db.setGroupedCsvPath("../misc/generated_bacteria_uniprot/grouped_with_ids.csv");
        db.setDbPath(".../misc/generated_bacteria_uniprot/mapdb_sstable.db");

        //  FileUtils.delete(new File(db.dbPath));
         db.startBuild();
      //  db.startSearchMain(args);
    }

    public void startSearchMain(String[] args) {
        // read from console two number and perfom search
        MapdbPeptideDatabase db = new MapdbPeptideDatabase();
        SortedTableMap<Integer, byte[]> maps = db.openForSearch();
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
            List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> result = db.search(mass1, mass2, maps);
            log.info("Search time milisec " + watch.getTime());
            log.info("Results time: " + result.size());
            for (Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>> entry : result) {
                Set<BinaryPeptideDbUtil.PeptideAcc> value = entry.getValue();
                System.out.println(entry.getKey() + ": " + value);
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

        buildDb();

    }

    public SortedTableMap<Integer, byte[]> openForSearch() {
        Volume volume = MappedFileVol.FACTORY.makeVolume(dbPath, true);

        return SortedTableMap.open(
              volume,
              Serializer.INTEGER,
              Serializer.BYTE_ARRAY
        );
    }

    public List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> search(double mass1, double mass2, SortedTableMap<Integer, byte[]> map) {

        return searchRange(map, mass1, mass2);

    }

    public List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> searchRange(SortedTableMap<Integer, byte[]> map, double mass1, double mass2) {
//        long mass1long = Double.doubleToLongBits(mass1);
//        long mass2long = Double.doubleToLongBits(mass2);
//        int mass1int = (int) (mass1 * 10000);
//        int mass2int = (int) (mass2 * 10000);
        int mass1Int = (int) Math.round(mass1 * 10000);
        int mass2Int = (int) Math.round(mass2 * 10000);

        SortedMap<Integer, byte[]> submap = map.subMap(mass1Int, true, mass2Int, true);
        List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> results = new ArrayList<>();
        for (Map.Entry<Integer, byte[]> entry : submap.entrySet()) {
            //  int massInt = (int)Math.round(mass * 10000);
            double keyMass = entry.getKey() / 10000.0;
            Set<BinaryPeptideDbUtil.PeptideAcc> peptideAccs = BinaryPeptideDbUtil.readGroupedRow(entry.getValue());
            results.add(new AbstractMap.SimpleEntry<>(keyMass, peptideAccs));
        }
        return results;
    }

    public void buildDb() throws IOException {

        log.debug("Building DB from: " + groupedCsvPath + " to: " + dbPath);
        Volume volume = MappedFileVol.FACTORY.makeVolume(dbPath, false);

        SortedTableMap.Sink<Integer, byte[]> sink =
              SortedTableMap.create(
                          volume,
                          Serializer.INTEGER,
                          Serializer.BYTE_ARRAY
                    )
                   // .pageSize(1024 * 1024 * 4) // Povećajte veličinu stranice
                   // .nodeSize(64) // Povećajte veličinu čvora
                    .createFromSink();


        BufferedReader reader = new BufferedReader(new FileReader(groupedCsvPath));
        String line;

        while ((line = reader.readLine()) != null) {
            // 503.234,SGAGAAA:15-SAAGGAA:14-TGAAAGG:16
            String[] parts = line.split(",", 2);
            if (parts.length < 2) throw new IllegalArgumentException("Invalid input CSV format " + line);

            double mass = Double.parseDouble(parts[0]);
            //mass = BioUtil.roundToFour(mass);
//            long massLong = Double.doubleToLongBits(mass);
            int massInt = (int) Math.round(mass * 10000);
            String seqAccs = parts[1];
            byte[] seqAccsBytes = BinaryPeptideDbUtil.writeGroupedRow(seqAccs);
            sink.put(massInt, seqAccsBytes);
        }
        SortedTableMap<Integer, byte[]> map = sink.create();
        log.info("Created map with size: " + map.size());
        map.close();


    }
}
