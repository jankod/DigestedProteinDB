package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.MyUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
public class MainAccessionRocksDb {

    private String fromCsvPath = "";
    private String toDbPath = "";

    //private String dbAccessionPath = "";

    public static void main(String[] args) throws RocksDBException {
        MainAccessionRocksDb db = new MainAccessionRocksDb();
        db.fromCsvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/accession_map_sorted.csv";
        db.toDbPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/rocksdb_accession.db";

        //   db.startCreateToRocksDb();


//         502836,B0BB14
        // search
        RocksDB rocksDb = db.openReadDB();
        int searchAccNum = 502836;
        StopWatch watch = StopWatch.createStarted();
        String acc = db.getAccByNum(rocksDb, searchAccNum);
        watch.stop();


        long nanoTime = watch.getNanoTime();
        long milliTime = TimeUnit.NANOSECONDS.toMillis(nanoTime);
        log.info("Search time " + DurationFormatUtils.formatDuration(milliTime, "HH:mm:ss,SSS"));
        log.debug("numm {}: {}", searchAccNum, acc);
        rocksDb.close();


    }

    public Map<Integer, String> searchAccs(RocksDB rocksDb, List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> result) throws RocksDBException {
        List<Integer> accs = new ArrayList<>();
        for (Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>> entry : result) {
            Set<BinaryPeptideDbUtil.PeptideAcc> value = entry.getValue();
            for (BinaryPeptideDbUtil.PeptideAcc acc : value) {
                int[] accessions = acc.getAccessions();
                for (int i : accessions) {
                    accs.add(i);
                }
            }
        }
        return searchMultiAccs(rocksDb, accs);
    }

    public RocksDB openReadDB() throws RocksDBException {
        return MyUtil.openPointReadDB(toDbPath);
    }


    public Map<Integer, String> searchMultiAccs(RocksDB rocksDb, List<Integer> accs) throws RocksDBException {
        List<byte[]> bytes = rocksDb.multiGetAsList(MyUtil.intListToByteList(accs));
        Map<Integer, String> map = new HashMap<>(accs.size());
        for (int i = 0; i < accs.size(); i++) {
            map.put(accs.get(i), new String(bytes.get(i), StandardCharsets.UTF_8));
        }
        return map;
    }

    public static String getAccByNum(RocksDB rocksDb, int searchAccNum) throws RocksDBException {
        byte[] res = rocksDb.get(MyUtil.intToByteArray(searchAccNum));
        return new String(res, StandardCharsets.UTF_8);
    }

    public void startCreateToRocksDb() {
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
                    // 1,B9EXL0
                    int accNum = Integer.parseInt(parts[0]);
                    String acc = parts[1];

                    db.put(MyUtil.intToByteArray(accNum), acc.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException | RocksDBException e) {
            log.error("Error: ", e);
            throw new RuntimeException(e);
        }

        long size = FileUtils.sizeOfDirectory(new File(toDbPath));
        log.info("Size of DB: {} MB", FileUtils.byteCountToDisplaySize(size));
    }
}
