package hr.pbf.digestdb.util;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Utility class for managing a database of accession numbers and their associated taxonomy IDs.
 * Default path in dbDir + CreateDatabse.DEFAULT_DB_ACC_TAX
 */
@Slf4j
public class AccTaxDB {

    // Acc => TaxId
    private Long2IntMap db;

    public void createEmptyDb() {
        this.db = new Long2IntOpenHashMap();
        this.db.defaultReturnValue(-1); // Default value for non-existing keys
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

    public void writeToDisk(String path) {
        if (db == null) {
            throw new IllegalStateException("Database is not initialized. Call createDb() first.");
        }
        // write to CSV
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            db.forEach((keyAcc, valueTaxId) -> {
                try {
                    writer.write(keyAcc + "," + valueTaxId);
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to disk: " + path, e);
        }

    }

    public void loadFromDisk(String path) throws IOException {
        db = new Long2IntOpenHashMap();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
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
                    db.put(acc, taxId);
                } catch (NumberFormatException e) {
                    log.warn("Error parsing line: {}", line, e);
                }
            }
        }
        log.info("Loaded {} entries from {}", db.size(), path);
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
