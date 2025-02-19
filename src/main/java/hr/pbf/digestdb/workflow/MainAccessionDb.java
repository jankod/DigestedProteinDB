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
                    byte[] key = accession.getBytes(StandardCharsets.UTF_8);

                    try {
                        if (db.get(key) == null) {
                            db.put(key, Longs.toByteArray(currentAccessionIdCounter));
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
     * @param accession Accession
     * @return id or -1
     */
    public long searchAccessionDb(String accession) {
        try (RocksDB db = MyUtil.openDB(toRocksDbPath)) {
            byte[] key = accession.getBytes(StandardCharsets.UTF_8);
            byte[] value = db.get(key);
            if (value != null) {
                return Longs.fromByteArray(value);
            }
            return -1;

        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }
}

