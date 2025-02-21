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

        try (RocksDB db = MyUtil.openDB(toRocksDbPath)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fromCsvPath))) {

                reader.lines().forEach(line -> {
                    String[] splited = StringUtils.split(line, ',');
                    // double mass = Double.parseDouble(splited[0]);
                    //String sequence = splited[1];
                    String accession = splited[2];
                    byte[] value = accession.getBytes(StandardCharsets.UTF_8);

                    try {
                        byte[] keyAccessionId = Longs.toByteArray(currentAccessionIdCounter);
                        if (db.get(keyAccessionId) == null) {
                            db.put(keyAccessionId, value);
                        }
                    } catch (RocksDBException e) {
                        throw new RuntimeException(e);
                    }
                    currentAccessionIdCounter++;

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
        try (RocksDB db = MyUtil.openDB(toRocksDbPath)) {
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

