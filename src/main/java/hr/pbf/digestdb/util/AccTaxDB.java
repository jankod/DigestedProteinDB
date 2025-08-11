package hr.pbf.digestdb.util;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import lombok.extern.slf4j.Slf4j;

import javax.xml.stream.XMLStreamException;
import java.io.*;


/**
 * Utility class for managing a database of accession numbers and their associated taxonomy IDs.
 * Default path in dbDir + CreateDatabse.DEFAULT_DB_ACC_TAX
 */
@Slf4j
public class AccTaxDB {

    // Acc => TaxId
    private Long2IntMap db;

    private AccTaxDB() {
    }

    public static AccTaxDB createEmptyDb() {
        AccTaxDB accTaxDB = new AccTaxDB();
        accTaxDB.db = new Long2IntOpenHashMap();
        accTaxDB.db.defaultReturnValue(-1); // Default value for non-existing keys
        return accTaxDB;
    }

    public void addAccessionTaxId(long accession, int taxId) {
        if (db == null) {
            throw new IllegalStateException("Database is not initialized. Call createEmptyDb() first.");
        }
        if (taxId <= 0) {
            log.warn("Invalid taxonomy ID for accession {}: {}", accession, taxId);
            return; // Skip invalid taxonomy IDs
        }
        if (db.containsKey(accession)) {
            log.warn("Duplicate accession found: {}", accession);
            return; // Skip duplicate accessions
        }
        db.put(accession, taxId);
    }


    public void createDb(String xmlPath) throws XMLStreamException, IOException {
        //Long2ObjectMap<byte[]> accessionTaxMap = new Long2ObjectOpenHashMap<>();
        Long2IntMap accessionTaxMap = new Long2IntOpenHashMap();
        UniprotXMLParser parser = new UniprotXMLParser();
        parser.parseProteinsFromXMLstream(xmlPath, new UniprotXMLParser.ProteinHandler() {
            @Override
            public void gotProtein(UniprotXMLParser.ProteinInfo p) throws IOException {
                try {
                    long acc = MyUtil.toAccessionLong36(p.getAccession());

                    int taxId = p.getTaxonomyId();
                    if (taxId <= 0) {
                        log.warn("Invalid taxonomy ID for accession {}: {}", p.getAccession(), taxId);
                        return; // Skip invalid taxonomy IDs
                    }
                    if (accessionTaxMap.containsKey(acc)) {
                        log.warn("Duplicate accession found: {}", p.getAccession());
                        return; // Skip duplicate accessions
                    }
                    accessionTaxMap.put(acc, taxId); // Store taxonomy ID directly as int

                } catch (Exception e) { // Hvatanje općenite iznimke za svaki slučaj
                    throw new RuntimeException("Error processing protein: " + p.getAccession(), e);
                }
            }
        });
        this.db = accessionTaxMap;

    }

    public void writeToDiskCsv(String pathCsv) {
        if (db == null) {
            throw new IllegalStateException("Database is not initialized. Call createDb() first.");
        }
        // write to CSV
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathCsv))) {
            db.forEach((keyAcc, valueTaxId) -> {
                try {
                    writer.write(keyAcc + "," + valueTaxId);
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to disk: " + pathCsv, e);
        }

    }

    /**
     * Read CSV file with accession (string) numbers and taxonomy ID (int) .
     * @param pathCsv
     * @return
     * @throws IOException
     */
    public static AccTaxDB loadFromDiskCsv(String pathCsv) throws IOException {
        AccTaxDB accTaxDB = new AccTaxDB();
        accTaxDB.db = new Long2IntOpenHashMap();
        try (BufferedReader reader = new BufferedReader(new FileReader(pathCsv))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 2) {
                    log.warn("Invalid line format: {}", line);
                    continue; // Skip invalid lines
                }
                try {
                    String accStr = parts[0];
                    long acc = MyUtil.toAccessionLong36(accStr);
                    int taxId = Integer.parseInt(parts[1]);
                    accTaxDB.db.put(acc, taxId);
                } catch (NumberFormatException e) {
                    log.warn("Error parsing line: {}", line, e);
                }
            }
        }
        log.info("Loaded {} entries from {}", accTaxDB.db.size(), pathCsv);
        return accTaxDB;
    }


    /**
     * Read CSV file with accession (long) numbers and taxonomy ID (int) .
     * @param pathCsv
     * @return
     * @throws IOException
     */
    public static AccTaxDB loadFromDisk(String pathCsv) throws IOException {
        AccTaxDB accTaxDB = new AccTaxDB();
        accTaxDB.db = new Long2IntOpenHashMap();
        try (BufferedReader reader = new BufferedReader(new FileReader(pathCsv))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 2) {
                    log.warn("Invalid line format: {}", line);
                    continue; // Skip invalid lines
                }
                try {
                    long acc = Long.parseLong(parts[0]);
                    int taxId = Integer.parseInt(parts[1]);
                    accTaxDB.db.put(acc, taxId);
                } catch (NumberFormatException e) {
                    log.warn("Error parsing line: {}", line, e);
                }
            }
        }
        log.info("Loaded {} entries from {}", accTaxDB.db.size(), pathCsv);
        return accTaxDB;
    }


    public int size() {
        if (db == null) {
            return 0;
        }
        return db.size();
    }

    public int getTaxonomyId(String accText) {
        return db.get(MyUtil.toAccessionLong36(accText));
    }
}
