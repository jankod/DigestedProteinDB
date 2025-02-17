package hr.pbf.digestdb.rocksdb;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class MainSortedCsvToDb {

    public String fromCsvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/csv/peptide_mass_sorted.csv";
    public String toDbPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/rocks_db/mass_db_compact";

    public double startMass = 2467.1;
    public double endMass = 2467.4;


    public void mainSearch() throws RocksDBException {


        List<RockDbValue> rows = new ArrayList<>();

        try (RocksDB db = RocksDbUtil.openDB(toDbPath);
             RocksIterator iterator = db.newIterator()) { // Dobijemo iterator

            byte[] startKeyBytes = RocksDbUtil.doubleToByteArray(startMass);
            //byte[] endKeyBytes = RocksDbUtil.doubleToByteArray(endMass);

            iterator.seek(startKeyBytes); // Postavimo iterator na početak raspona

            while (iterator.isValid()) { // Dok iterator pokazuje na valjani unos
                byte[] currentKeyBytes = iterator.key();
                double currentKeyDouble = RocksDbUtil.byteArrayToDouble(currentKeyBytes);

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
        }

        log.debug("Found " + rows.size() + " rows in range [" + startMass + ", " + endMass + "]");
        for (RockDbValue row : rows) {
//            System.out.println(row);
        }

    }

    public void startInsertToDackDb() throws IOException, RocksDBException {


        try (RocksDB db = RocksDbUtil.openDB(toDbPath)) {

            String csvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/csv/peptide_mass_sorted.csv";
            try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(Path.of(csvPath)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("mass")) {
                        continue;
                    }
                    String[] split = line.split(",");

//                    mass,sequence,accession,taxonomy_id
//331.14917543984376,GGGAA,A1ITY1,122587
//331.14917543984376,GGGAA,Q9K153,122586

                    double mass = Double.parseDouble(split[0]);
                    String sequence = split[1];
                    String accession = split[2];
                    int taxonomyId = Integer.parseInt(split[3]);
                    gotRow(mass, sequence, accession, taxonomyId, db);
                }
            }

        }


    }


    private static String lastSequence = "";
    private static final HashSet<String> accessions = new HashSet<>();

    private static void gotRow(Double mass, String sequence, String accession, int taxonomyId, RocksDB db) throws RocksDBException {
        if (sequence.equals(lastSequence)) {
            accessions.add(accession);
        } else {
            if (!lastSequence.isEmpty()) {
                RockDbValue row = new RockDbValue();
                row.sequence = lastSequence;
                row.accessions = accessions;
                byte[] massKey = RocksDbUtil.doubleToByteArray(mass);
                db.put(massKey, SerializationUtils.serialize(row));
            }
            lastSequence = sequence;
            accessions.clear();
            accessions.add(accession);
        }

    }

    public void startInsertToRocksDb() {
        throw new UnsupportedOperationException("Not implemented");
    }


    @Data
    static class RockDbValue implements Externalizable {
        // add  serialVersionUID = 1L
        @Serial
        private static final long serialVersionUID = 1L;

        private String sequence;
        private Set<String> accessions = new HashSet<>();

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(sequence);
            out.writeInt(accessions.size());
            if (!accessions.isEmpty()) {
                for (String acc : accessions) {
                    out.writeUTF(acc);
                }
            }
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException {
            sequence = in.readUTF();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                accessions.add(in.readUTF());
            }
        }
    }
}
