package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.exception.UnknownAminoacidException;
import hr.pbf.digestdb.util.AminoAcidCoder;
import hr.pbf.digestdb.util.MyUtil;
import hr.pbf.digestdb.util.PeptideValueSerialization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MainMassRocksDb {

    public String fromCsvPath = "";
    public String toDbPath = "";

    public String dbAccessionPath = "";
    private RocksDB dbAccesion;

    public  static  class SearchResult {
        public double mass;
        public String sequence;
        public Set<String> accessions;

        public SearchResult(double mass, String sequence, Set<String> accessions) {
            this.mass = mass;
            this.sequence = sequence;
            this.accessions = accessions;
        }
    }



    public List<SearchResult> searchByMass(double mass, double tolerance) throws RocksDBException, UnknownAminoacidException {
       // List<Map<String, Set<Long>>> result = new ArrayList<>();
          dbAccesion = MyUtil.openReadDB(dbAccessionPath);
        List<SearchResult> result = new ArrayList<>();
        try (RocksDB db = MyUtil.openReadDB(toDbPath)) {
            RocksIterator it = db.newIterator();
            it.seek(MyUtil.doubleToByteArray(mass));
            while (it.isValid()) {
                double key = MyUtil.byteArrayToDouble(it.key());
                if (Math.abs(key - mass) < tolerance) {
                    Map<String, Set<Long>> peptides = PeptideValueSerialization.fromByteArray(it.value());
                    for (Map.Entry<String, Set<Long>> entry : peptides.entrySet()) {
                        String sequence = entry.getKey();
                        Set<Long> accessions = entry.getValue();
                        Set<String> accessionList = accessions.stream().map(a -> {
                            try {
                                byte[] accessionByte = dbAccesion.get(MyUtil.longToByteArray(a));
                                if(accessionByte == null) {
                                    log.error("Accession not found: {}", a);
                                    return "";
                                }
                                return new String(accessionByte, StandardCharsets.UTF_8);
                            } catch (RocksDBException e) {
                                throw new RuntimeException(e);
                            }
                        }).collect(Collectors.toSet());
                        SearchResult searchResult = new SearchResult(key, sequence, accessionList);
                        result.add(searchResult);
                       log.info("Mass: {} Sequence: {} Accessions: {}", key, sequence, accessions);
                    }
                }
                it.next();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dbAccesion.close();
        return result;
    }

    public void startInsertToRocksDb() throws RocksDBException {
        if (new File(toDbPath).exists()) {
            log.error("DB already exists: {}", toDbPath);
            return;
        }
        if (!new File(dbAccessionPath).exists()) {
            log.error("DB not exists: {}", dbAccessionPath);
            return;
        }
        if (!new File(fromCsvPath).exists()) {
            log.error("File not exists: {}", fromCsvPath);
            return;
        }

        Map<String, Set<Long>> currentPeptides = new HashMap<>();

        double currentMass5 = 0;

        dbAccesion = MyUtil.openReadDB(dbAccessionPath);
        int rowNum = 1;
        try (RocksDB db = MyUtil.openDB(toDbPath)) {
            try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(Path.of(fromCsvPath)))) {
                String line;
                rowNum++;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("mass")) {
                        continue;
                    }
                    String[] splited = StringUtils.split(line, ',');
                    double mass = Double.parseDouble(splited[0]);
                    String sequence = splited[1];
                    if (AminoAcidCoder.isInvalidPeptide(sequence)) {
                        log.error("Invalid peptide: {} row num: {}", sequence, rowNum);
                        continue;
                    }


                    String accession = splited[2];
                    long accessionId = getAccession(accession);


                    //int taxonomyId = Integer.parseInt(splited[3]);

                    // round mass to 5
                    double mass5 = MyUtil.roundToFive(mass);
                    if (mass5 != currentMass5) {
                        insertNewData(currentMass5, currentPeptides, db);
                        currentMass5 = mass5;
                        currentPeptides = new HashMap<>();
                    }
                    if (currentPeptides.containsKey(sequence)) {
                        currentPeptides.get(sequence).add(accessionId);
                    } else {
                        Set<Long> set = new HashSet<>();
                        set.add(accessionId);
                        currentPeptides.put(sequence, set);
                    }
                }
            }
        } catch (IOException | RocksDBException e) {
            log.error("Error: ", e);
        }
        //  TODO save accessions to file

    }

    private long getAccession(String accession) throws RocksDBException {
        byte[] accessionValue = dbAccesion.get(accession.getBytes(StandardCharsets.UTF_8));
        if (accessionValue != null) {
            return MyUtil.byteArrayToLong(accessionValue);
        }
        log.error("Accession not found: {}", accession);
        return 0;
    }

    private void insertNewData(double currentMass, Map<String, Set<Long>> currentPeptides, RocksDB db) throws
          RocksDBException, IOException, UnknownAminoacidException {
        // serialize Map to byte buffer
        byte[] value = PeptideValueSerialization.toByteArray(currentPeptides);

        byte[] key = MyUtil.doubleToByteArray(currentMass);
        db.put(key, value);
    }


    public static void main(String[] args) {
        double p1 = 345.1648349574219;
        double p2 = 345.1648309574219;
        double p3 = 347.144109521875;


        System.out.println(MyUtil.roundToFour(p1));
        System.out.println(MyUtil.roundToFour(p2));
        System.out.println(MyUtil.roundToFour(p3));

    }


}
