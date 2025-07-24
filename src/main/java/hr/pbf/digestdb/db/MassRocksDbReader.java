package hr.pbf.digestdb.db;

import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.MyUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.rocksdb.ReadOptions;
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

    public void open() throws RocksDBException {
        this.db = MyUtil.openReadDB(dbPath);
    }

    public void close() {
        if (db != null) {
            db.close();
        }
    }

    public MassPageResult searchByMass(double mass1, double mass2, int page, int pageSize) {

        List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> results = new ArrayList<>();
        int mass1Int = MyUtil.toInt(mass1); //(int) Math.round(mass1 * 10_000);
        int count = 0;
        int start = (page - 1) * pageSize;
        int end = page * pageSize;
        int totalCount = 0;

        try (ReadOptions readOptions = new ReadOptions()) {
            RocksIterator it = db.newIterator(readOptions);
            it.seek(MyUtil.intToByteArray(mass1Int));

            while (it.isValid()) {
                int massInt = MyUtil.byteArrayToInt(it.key());
                double keyMass = massInt / 10_000.0;

                if (keyMass > mass2) {
                    break;
                }
                totalCount++;

                if (count >= start && count < end) {
                    Set<BinaryPeptideDbUtil.PeptideAccids> peptideAccs = BinaryPeptideDbUtil.readGroupedRow(it.value());

                    results.add(new AbstractMap.SimpleEntry<>(keyMass, peptideAccs));
                }
                count++;
                it.next();
            }
        }

        return new MassPageResult(totalCount, results);
    }

    public List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> searchByMass(double mass1, double mass2) {
        List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> results = new ArrayList<>();
        int mass1Int = (int) Math.round(mass1 * 10_000);

        RocksIterator it = db.newIterator();
        it.seek(MyUtil.intToByteArray(mass1Int));

        while (it.isValid()) {
            int massInt = MyUtil.byteArrayToInt(it.key());
            double keyMass = massInt / 10_000.0;

            if (keyMass > mass2) {
                break;
            }

            Set<BinaryPeptideDbUtil.PeptideAccids> peptideAccs = BinaryPeptideDbUtil.readGroupedRow(it.value());
            results.add(new AbstractMap.SimpleEntry<>(keyMass, peptideAccs));
            it.next();
        }

        return results;
    }

    @Data
    @RequiredArgsConstructor
    public static class MassPageResult {
        private final int totalCount;
        private final List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> results;

    }

    @Data
    @RequiredArgsConstructor
    public static class MassPageResultTax {
        private final int totalCount;
        private final List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> results;
    }

//    public static class PeptideAccTax {
//        String seq;
//        int[] acc;
//        int taxId;
//
//    }
//
//    public static class AccTaxs {
//        String seq;
//        List<PeptideAccTax> accs = new ArrayList<>();
//
//        public void addAcc(PeptideAccTax acc) {
//            this.accs.add(acc);
//        }
//    }



}
