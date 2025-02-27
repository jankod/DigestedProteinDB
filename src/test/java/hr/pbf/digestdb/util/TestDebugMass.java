package hr.pbf.digestdb.util;

import hr.pbf.digestdb.workflow.MassRocksDb;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestDebugMass {
    public static void main(String[] args) throws RocksDBException {
        MassRocksDb db = new MassRocksDb();

        db.setToDbPath("/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/rocksdb_mass.db");
        RocksDB rocksDB = db.openReadDB();
        List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> entries = db.searchByMass(rocksDB, 1830.9312, 1830.9312);
        for (Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>> entry : entries) {
            System.out.println(entry.getKey());
            for (BinaryPeptideDbUtil.PeptideAcc peptideAcc : entry.getValue()) {
                System.out.println(peptideAcc.getSeq());
            }
        }

        rocksDB.close();
    }
}
