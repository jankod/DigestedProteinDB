package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.exception.UnknownAminoacidException;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.CustomAccessionDb;
import hr.pbf.digestdb.util.MyUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class MainMassRocksDb {

    public String fromCsvPath = "";
    public String toDbPath = "";
   // public String accDbPath = "";

//    public String dbAccessionPath = "";


    public static void main(String[] args) throws RocksDBException {
        MainMassRocksDb db = new MainMassRocksDb();
        // db.fromCsvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_human/grouped_with_ids.csv";
        // db.toDbPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_human/rocksdb_mass.db";

        db.fromCsvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/grouped_with_ids.csv";
        db.toDbPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/rocksdb_mass.db";
     //   db.setAccDbPath("/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/rocksdb_accession.db");
        //  db.startCreateToRocksDb();
        db.searchByMassInConsole();
    }

    public void searchByMassInConsole() throws RocksDBException {
        System.out.println("Enter two numbers separated by space. Enter 'stop' to stop");
        Console console = System.console();
        RocksDB massDb = openReadDB();

        CustomAccessionDb accDb = new CustomAccessionDb();
        accDb.setToDbPath("/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/custom_accession.db");
        accDb.loadDb();
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
            List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>>
                  result = searchByMass(massDb, mass1, mass2);
            watch.stop();


            StopWatch watchAcc = StopWatch.createStarted();
            watchAcc.stop();
            long millisAcc = TimeUnit.NANOSECONDS.toMillis(watchAcc.getNanoTime());
            log.info("Search acc time " + DurationFormatUtils.formatDuration(millisAcc, "HH:mm:ss,SSS"));

            long millisMass = TimeUnit.NANOSECONDS.toMillis(watch.getNanoTime());
            log.info("Search mass time milisec " + DurationFormatUtils.formatDuration(millisMass, "HH:mm:ss,SSS"));
            log.info("Results found: " + result.size());


            for (Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>> entry : result) {
                Set<BinaryPeptideDbUtil.PeptideAcc> value = entry.getValue();
                Double mass = entry.getKey();

                StringBuilder sb = new StringBuilder();
                value.forEach(acc -> {
                    int[] accessions = acc.getAccessions();
                    String seq = acc.getSeq();
                    sb.append(seq).append(": ");
                    for (int accNum : accessions) {
                        String accStr = accDb.getAcc(accNum);
                        sb.append(accStr).append(" ");
                    }
                });
                log.info(mass + ": " + sb);
            }

        }
        massDb.close();

    }


    public List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> searchByMass(RocksDB db, double mass1,
                                                                                     double mass2) throws RocksDBException, UnknownAminoacidException {

        List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> results = new ArrayList<>();

        int mass1Int = (int) Math.round(mass1 * 10000);
        // int mass2Int = (int) Math.round(mass2 * 10000);

        RocksIterator it = db.newIterator();
        it.seek(MyUtil.intToByteArray(mass1Int));
        while (it.isValid()) {
            int massInt = MyUtil.byteArrayToInt(it.key());
            double keyMass = massInt / 10000.0;
            Set<BinaryPeptideDbUtil.PeptideAcc> peptideAccs = BinaryPeptideDbUtil.readGroupedRow(it.value());
            results.add(new AbstractMap.SimpleEntry<>(keyMass, peptideAccs));

            if (keyMass > mass2) {
                break;
            }
            it.next();
        }

        return results;
    }

    public void startCreateToRocksDb() throws RocksDBException {
        if (new File(toDbPath).exists()) {
            log.error("DB already exists: {}", toDbPath);
            return;
        }
        if (!new File(fromCsvPath).exists()) {
            log.error("File not exists: {}", fromCsvPath);
            return;
        }

        try (RocksDB db = MyUtil.openWriteDB(toDbPath)) {
            try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(Path.of(fromCsvPath)), 8 * 1024 * 16)) {

                String line;

                while ((line = reader.readLine()) != null) {
                    // 503.234,SGAGAAA:15-SAAGGAA:14-TGAAAGG:16
                    String[] parts = line.split(",", 2);

                    if (parts.length < 2) throw new IllegalArgumentException("Invalid input CSV format " + line);

                    double mass = Double.parseDouble(parts[0]);
                    int massInt = (int) Math.round(mass * 10_000);
                    String seqAccs = parts[1];

                    byte[] seqAccsBytes = BinaryPeptideDbUtil.writeGroupedRow(seqAccs);
                    byte[] massIntBytes = MyUtil.intToByteArray(massInt);
                    db.put(massIntBytes, seqAccsBytes);
                }
            }
        } catch (IOException | RocksDBException e) {
            log.error("Error: ", e);
            throw new RuntimeException(e);
        }


    }


    public RocksDB openReadDB() throws RocksDBException {
        return MyUtil.openReadDB(toDbPath);
    }
}
