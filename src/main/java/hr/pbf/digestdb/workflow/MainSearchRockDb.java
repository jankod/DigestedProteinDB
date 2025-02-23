package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.util.MyUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
public class MainSearchRockDb {

    public String toDbPath = "";

    public List<RockDbValue> mainSearch(double startMass, double endMass) throws RocksDBException {

        List<RockDbValue> rows = new ArrayList<>();

        try (RocksDB db = MyUtil.openWriteDB(toDbPath);
             RocksIterator iterator = db.newIterator()) { // Dobijemo iterator

            byte[] startKeyBytes = MyUtil.doubleToByteArray(startMass);
            //byte[] endKeyBytes = RocksDbUtil.doubleToByteArray(endMass);

            iterator.seek(startKeyBytes); // Postavimo iterator na početak raspona

            while (iterator.isValid()) { // Dok iterator pokazuje na valjani unos
                byte[] currentKeyBytes = iterator.key();
                double currentKeyDouble = MyUtil.byteArrayToDouble(currentKeyBytes);

                if (currentKeyDouble > endMass) { // Ako je ključ veći od kraja raspona, prekidamo
                    break;
                }

                byte[] valueBytes = iterator.value();
                if (valueBytes != null) {
                    RockDbValue row = SerializationUtils.deserialize(valueBytes);
                    rows.add(row);
                    // log.debug("Found key: {}, value: {}", currentKeyDouble, row);
                }

                iterator.next(); // Pomaknemo iterator na sljedeći unos
            }


        } catch (RocksDBException e) {
            log.error("RocksDB error: ", e);
            throw e;
        }

        log.debug("Found " + rows.size() + " rows in range [" + startMass + ", " + endMass + "]");

        return rows;

    }

    @Data
    static class RockDbValue {

        private String sequence;
        private Set<String> accessions = new HashSet<>();

    }
}
