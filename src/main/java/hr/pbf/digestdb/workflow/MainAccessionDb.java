package hr.pbf.digestdb.workflow;

import com.google.common.primitives.Longs;
import hr.pbf.digestdb.util.MyUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * Create RocksDB with accessions.
 * Key: long id of accession
 * Value: String accession
 */
@Slf4j
@Data
public class MainAccessionDb {

    private String fromCsvPath = "";
    private String toRocksDbPath = "";
    long currentAccessionIdCounter = 1;

    public void startCreateDB() {
        currentAccessionIdCounter = 1;

        try (RocksDB db = MyUtil.openWriteDB(toRocksDbPath)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fromCsvPath))) {

                reader.lines().forEach(line -> {
                    try {
                        String[] splited = StringUtils.split(line, ',');
                        String accession = splited[1];
                        byte[] accessionBytes = accession.getBytes(StandardCharsets.UTF_8);
                        if (currentAccessionIdCounter == 1) {
                            // check only first line
                            if (Integer.parseInt(splited[0]) != 1) {
                                throw new RuntimeException("First id must be 1");
                            }
                        }

                        try {
                            byte[] keyAccessionId = Longs.toByteArray(currentAccessionIdCounter);
                            if (db.get(keyAccessionId) == null) {
                                db.put(keyAccessionId, accessionBytes);
                            }
                        } catch (RocksDBException e) {
                            throw new RuntimeException(e);
                        }
                        currentAccessionIdCounter++;
                    } catch (Exception e) {
                        log.error("Error in line: '{}'.", line);
                        throw new RuntimeException(e);
                    }

                    // int taxonomyId = Integer.parseInt(splited[3]);
                });


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        log.info("Done creating RocksDB with accessions. Last id: {}", currentAccessionIdCounter);

    }

    /**
     * Search accession in db. If not found return -1.
     *
     * @param accessionId to search
     * @return id or null{}
     */
    public String searchAccessionDb(Long accessionId) {
        try (RocksDB db = MyUtil.openWriteDB(toRocksDbPath)) {
            byte[] key = MyUtil.longToByteArray(accessionId);
            byte[] value = db.get(key);
            if (value != null) {
                return new String(value, StandardCharsets.UTF_8);
            }
            return null;

        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }
}

