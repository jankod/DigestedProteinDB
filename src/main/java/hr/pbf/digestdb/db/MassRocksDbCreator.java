package hr.pbf.digestdb.db;

import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.MyUtil;
import hr.pbf.digestdb.util.ValidatateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.File;
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
    private int bufferSizeForReadCsv = 1024 * 1024 * 32; // 32 MB

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
        long lineCount = 0;

        log.debug("Start creating RocksDB from CSV file: {} and write DB to: {}", fromCsvPath, toDbPath);
        try (RocksDB db = MyUtil.openWriteDB(toDbPath)) {

            try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(Path.of(fromCsvPath)), bufferSizeForReadCsv)) {

                String line;

                while ((line = reader.readLine()) != null) {
                    // 503.234,SGAGAAA:15-SAAGGAA:14-TGAAAGG:16
                    String[] parts = line.split(",", 2);

                    if (parts.length < 2)
                        throw new IllegalArgumentException("Invalid input CSV format " + line);

                    double mass = Double.parseDouble(parts[0]);
                    int massInt = (int) Math.round(mass * 10_000);
                    String seqAccs = parts[1];

                    try {
                        byte[] seqAccsBytes = BinaryPeptideDbUtil.writeGroupedRow(seqAccs);
                        byte[] massIntBytes = MyUtil.intToByteArray(massInt);
                        writeRocksDb(db, massIntBytes, seqAccsBytes);
                        countMasses++;
                    } catch (Exception e) {
                        log.error("Error on line: " + lineCount + ": " + StringUtils.truncate(line, 200), e);
                    }
                    lineCount++;
                }
            }
        } catch (IOException | RocksDBException e) {
            log.error("Error: ", e);
            throw new RuntimeException(e);
        }
        DbInfo dbInfo = new DbInfo();
        dbInfo.countMasses = countMasses;
        log.debug("RocksDB created successfully with {} masses.", countMasses);

        long sizeOfDirectory = FileUtils.sizeOfDirectory(new File(toDbPath));
        log.debug("RocksDB directory size: {} bytes", FileUtils.byteCountToDisplaySize(sizeOfDirectory));
        return dbInfo;
    }

    protected void writeRocksDb(RocksDB db, byte[] massIntBytes, byte[] seqAccsBytes) throws RocksDBException {
        db.put(massIntBytes, seqAccsBytes);
    }

}
