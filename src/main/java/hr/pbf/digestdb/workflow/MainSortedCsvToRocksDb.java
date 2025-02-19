package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.util.MyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class MainSortedCsvToRocksDb {

    public String fromCsvPath = "";
    public String toDbPath = "";

    public String dbAccessionPath = "";


    private RocksDB dbAccesion;

    public void startInsertToRocksDb() throws RocksDBException {
        Map<String, Set<Long>> currentPeptides = new HashMap<>();

        double currentMass5 = 0;

        dbAccesion = MyUtil.openReadDB(dbAccessionPath);

        try (RocksDB db = MyUtil.openDB(toDbPath)) {
            try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(Path.of(fromCsvPath)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("mass")) {
                        continue;
                    }
                    String[] splited = StringUtils.split(line, ',');
                    double mass = Double.parseDouble(splited[0]);
                    String sequence = splited[1];
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
          RocksDBException {
        // serialize Map to byte buffer
        int calculatedSize = calculateByteSize(currentPeptides);
        byte[] value = new byte[calculatedSize]; // ????



        db.put(MyUtil.doubleToByteArray(currentMass), value);
    }

    private int calculateByteSize(Map<String, Set<Long>> currentPeptides) {
        int totalSize = currentPeptides.size();
        for (Map.Entry<String, Set<Long>> entry : currentPeptides.entrySet()) {
            String peptide = entry.getKey();
            Set<Long> ids = entry.getValue();

            totalSize += Integer.BYTES; // For peptide string length
            totalSize += peptide.getBytes().length; // For peptide string bytes
            totalSize += Long.BYTES; // For number of long in list
            totalSize += ids.size() * Long.BYTES; // For integers in list
        }
        return totalSize;
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
