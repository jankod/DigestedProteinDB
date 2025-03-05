package hr.pbf.digestdb.db;

import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.MyUtil;
import hr.pbf.digestdb.util.ValidatateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Utility class for creating a RocksDB from a CSV file containing mass and sequence accessions.
 * CSV file must be sorted by mass is like this:
 * <pre>
 * 503.234,SGAGAAA:2-SAAGGAA:1
 * 505.2132,AGGASSG:3
 * 513.2547,AGAAPAG:4
 * 516.2405,GGGGGGR:8;7;6;5
 * 516.2656,AAGGGGK:11;10;9
 * ...
 * </pre>
 */
@Slf4j
@Data
public class MassRocksDbCreator {

    private final String fromCsvPath;
    private final String toDbPath;
    private int bufferSizeForReadCsv = 8192 * 128; // 128 KB

    public MassRocksDbCreator(String fromCsvPath, String toDbPath) {
        this.fromCsvPath = fromCsvPath;
        this.toDbPath = toDbPath;
        ValidatateUtil.fileMustExist(fromCsvPath);
        ValidatateUtil.fileMustNotExist(toDbPath);
    }

    public static class DbInfo {
        public int countMasses;
    }

    public DbInfo startCreate() throws RocksDBException {

        int countMasses = 0;

        try (RocksDB db = MyUtil.openWriteDB(toDbPath)) {

            try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(Path.of(fromCsvPath)), bufferSizeForReadCsv)) {

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
        DbInfo dbInfo = new DbInfo();
        dbInfo.countMasses = countMasses;
        return dbInfo;
        //log.info("DB rockDB size " + MyUtil.getDirSize(toDbPath));
    }

}
