package hr.pbf.digestdb.rocksdb;

import lombok.Data;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

public class RocksDBExample {
    private static final String DB_PATH = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/rocks-db/peptide_db"; // Putanja do RocksDB direktorija
    private RocksDB db;

    public void openDB() throws RocksDBException {
        Options options = new Options().setCreateIfMissing(true);
        try {
            db = RocksDB.open(options, DB_PATH);


        } catch (RocksDBException e) {
            System.err.println("Error opening RocksDB: " + e.getMessage());
            throw e; // Ponovno bacite iznimku da se obradi više razine
        }
    }

    public void closeDB() {
        if (db != null) {
            db.close();
        }
    }

    public static void main(String[] args) throws RocksDBException {
        RocksDBExample e = new RocksDBExample();
        e.openDB();


    }

    private static List<RocksMassData> readSwisprotXml() {
        // read xml file
        // parse xml
        // return list of RocksMassData

        return List.of();
    }

}
@Data
class RocksMassData implements Externalizable {
    String peptide;
    float mass;
    List<String> accessions;
    private List<Integer> taxonomyIds;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }
}
