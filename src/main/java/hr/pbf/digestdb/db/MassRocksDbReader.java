package hr.pbf.digestdb.db;

import hr.pbf.digestdb.exception.UnknownAminoacidException;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.MyUtil;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.util.*;

public class MassRocksDbReader implements AutoCloseable {
    private final String dbPath;
    private RocksDB db;

    public MassRocksDbReader(String dbPath) throws RocksDBException {
        this.dbPath = dbPath;
        open();
    }

    private void open() throws RocksDBException {
        this.db = MyUtil.openReadDB(dbPath);
    }

    public void close() {
        if (db != null) {
            db.close();
        }
    }

    public List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> searchByMass(double mass1, double mass2) {

        List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> results = new ArrayList<>();

        int mass1Int = (int) Math.round(mass1 * 10_000);

        RocksIterator it = db.newIterator();
        it.seek(MyUtil.intToByteArray(mass1Int));
        while (it.isValid()) {
            int massInt = MyUtil.byteArrayToInt(it.key());
            double keyMass = massInt / 10_000.0;
            Set<BinaryPeptideDbUtil.PeptideAcc> peptideAccs = BinaryPeptideDbUtil.readGroupedRow(it.value());
            if (keyMass > mass2) {
                break;
            }
            results.add(new AbstractMap.SimpleEntry<>(keyMass, peptideAccs));


            it.next();
        }

        return results;
    }
}
