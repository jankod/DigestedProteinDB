package hr.pbf.digestdb.rocksdb;

import hr.pbf.digestdb.util.BioUtil;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.List;

@Slf4j
public class RocksDbMass {
    private final String dbPath;
    private RocksDB db;

    public RocksDbMass(String dbPath) {
        this.dbPath = dbPath;
    }

    public void openDB() throws RocksDBException {
        Options options = new Options().setCreateIfMissing(true);
        try {
            db = RocksDB.open(options, dbPath);
        } catch (RocksDBException e) {
            log.error("Error opening RocksDB: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void closeDB() {
        if (db != null) {
            db.close();
        }
    }

    public void saveToDatabase(ProteinInfo p, int peptideLengthMin, int peptideLengthMax) {
        // add
        List<String> peptides = BioUtil.tripsyn(p.getSequence(), peptideLengthMin, peptideLengthMax);
        for (String peptide : peptides) {
            float mass = (float) BioUtil.calculateMassWidthH2O(peptide);
            // mass to byte[]
            byte[] massAsKey = RocksDbUtil.floatToByteArray(mass);

            // ako vec postoji taj kljuc u bazi, onda dodajemo jos jedan peptide

            // ako ne postoji taj kljuc u bazi, onda dodajemo novi kljuc


            PeptideByMass peptideMass = new PeptideByMass();


            PeptideByMass peptideAsValue = new PeptideByMass();
            peptideAsValue.setPeptide(peptide);

            //  db.put(massAsKey, SerializationUtils.serialize(p));
        }

    }

    public void search(float mass, float tolerance) {

    }
}


