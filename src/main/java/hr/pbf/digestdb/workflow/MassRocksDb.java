package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.exception.UnknownAminoacidException;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.MyUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Data
@Slf4j
public class MassRocksDb {
    public static final String ROCKSDB_MASS_DB_FILE_NAME = "rocksdb_mass.db";

    public String fromCsvPath = "";
    public String toDbPath = "";

    public List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> searchByMass(RocksDB db, double mass1,
                                                                                     double mass2) throws RocksDBException, UnknownAminoacidException {

        List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> results = new ArrayList<>();

        int mass1Int = (int) Math.round(mass1 * 10_000);

        RocksIterator it = db.newIterator();
        it.seek(MyUtil.intToByteArray(mass1Int));
        while (it.isValid()) {
            int massInt = MyUtil.byteArrayToInt(it.key());
            double keyMass = massInt / 10_000.0;
            Set<BinaryPeptideDbUtil.PeptideAcc> peptideAccs = BinaryPeptideDbUtil.readGroupedRow(it.value());
            results.add(new AbstractMap.SimpleEntry<>(keyMass, peptideAccs));

            if (keyMass > mass2) {
                break;
            }
            it.next();
        }

        return results;
    }

    public void start() throws RocksDBException {
        if (new File(toDbPath).exists()) {
            log.error("DB already exists: {}", toDbPath);
            throw new IllegalArgumentException("DB already exists: " + toDbPath);
        }
        if (!new File(fromCsvPath).exists()) {
            log.error("File not exists: {}", fromCsvPath);
            throw new IllegalArgumentException("File not exists: " + fromCsvPath);
        }
        int countMasses = 0;
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
                    countMasses++;
                }
            }
        } catch (IOException | RocksDBException e) {
            log.error("Error: ", e);
            throw new RuntimeException(e);
        }
        log.info("Done. Count diferent masses as key in rocksdb: " + countMasses);
        // get dir length
        log.info("DB rockDB size "+ MyUtil.getDirSize(toDbPath));

    }


    public RocksDB openReadDB() throws RocksDBException {
        return MyUtil.openReadDB(toDbPath);
    }
}
