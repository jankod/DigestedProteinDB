package hr.pbf.digestdb.util;

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

    private Long2ObjectMap<byte[]> db;

    public List<Integer> getTaxonomyIds(String accession) {
        long acc = MyUtil.toAccessionLong36(accession);
        return getTaxonomyIds(acc);
    }

    public List<Integer> getTaxonomyIds(long accession) {
        if (this.db == null) {
            throw new IllegalStateException("Database is not initialized. Call read() first.");
        }

        byte[] taxonomyIdsBytes = db.get(accession);
        if (taxonomyIdsBytes == null) {
            log.warn("No taxonomy IDs found for accession: {}", accession);
            return Collections.emptyList();
        }

        return IntegerListConverter.toIntegerList(taxonomyIdsBytes);
    }


    public void createDb(String xmlPath) throws XMLStreamException, IOException {
        Long2ObjectMap<byte[]> accessionTaxMap = new Long2ObjectOpenHashMap<>();
        UniprotXMLParser parser = new UniprotXMLParser();
        parser.parseProteinsFromXMLstream(xmlPath, new UniprotXMLParser.ProteinHandler() {
            @Override
            public void gotProtein(UniprotXMLParser.ProteinInfo p) throws IOException {
                try {
                    long acc = MyUtil.toAccessionLong36(p.getAccession());
                    List<Integer> taxonomyIds = p.getTaxonomyIds();
                    byte[] taxonomyIdsBytes = IntegerListConverter.toByteArray(taxonomyIds);
                    if (accessionTaxMap.containsKey(acc)) {
                        throw new IllegalStateException("Duplicate accession: " + p.getAccession());
                    }
                    accessionTaxMap.put(acc, taxonomyIdsBytes);
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
        try (var bos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)))) {
            bos.writeInt(db.size());
            db.forEach((key, value) -> {
                try {
                    bos.writeLong(key);
                    bos.writeInt(value.length);
                    bos.write(value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to disk: " + path, e);
        }

    }

    public synchronized void readFromDiskCsv(String path) {
        // CSV Q6TW45,[10258, 9925, 9606, 9940]
        //A9Z0R2,[10258, 9925, 9606, 9940]
        //A0A0F6N206,[10258, 9925, 9606, 9940]
        //A0A0Y0RJ21,[10258, 9925, 9606, 9940]
        //A0A2S1CI81,[10258, 9925, 9606, 9940]
        Long2ObjectMap<byte[]> accessionTaxMap = new Long2ObjectOpenHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    log.warn("Skipping invalid line: {}", line);
                    continue; // Skip invalid lines
                }
                String accession = parts[0].trim();

                long acc = MyUtil.toAccessionLong36(accession);

                String taxonomyIdsStr = parts[1].trim();
                taxonomyIdsStr = taxonomyIdsStr.replaceAll("\\[", "");
                taxonomyIdsStr = taxonomyIdsStr.replaceAll("\\]", "");
                String[] taxonomyIdsArray = taxonomyIdsStr.split(",");
                List<Integer> taxonomyIds = new ArrayList<>(taxonomyIdsArray.length);
                for (String taxIdStr : taxonomyIdsArray) {
                    try {
                        int taxId = Integer.parseInt(taxIdStr.trim());
                        taxonomyIds.add(taxId);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid taxonomy ID: {}", taxIdStr, e);
                    }
                }
                if (taxonomyIds.isEmpty()) {
                    log.warn("No valid taxonomy IDs found for accession: {}", accession);
                    continue; // Skip if no valid taxonomy IDs
                }
                byte[] taxonomyIdsBytes = IntegerListConverter.toByteArray(taxonomyIds);
                if (accessionTaxMap.containsKey(acc)) {
                    log.warn("Duplicate accession found: {}", accession);
                    continue; // Skip duplicate accessions
                }
                accessionTaxMap.put(acc, taxonomyIdsBytes);

            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from disk: " + path, e);
        }
        this.db = accessionTaxMap;

    }

    public synchronized void readFromDiskByte(String path) {
        Long2ObjectMap<byte[]> accessionTaxMap;
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(path), 8 * 1024 * 16))) {
            accessionTaxMap = new Long2ObjectOpenHashMap<>(dis.readInt()); // Read the size of the map first
            while (true) { // Loop indefinitely, will be broken by EOFException
                long acc;
                int length;
                byte[] taxonomyIdsBytes;

                try {
                    acc = dis.readLong(); // Reads 8 bytes for the long key
                    length = dis.readInt(); // Reads 4 bytes for the int length

                    if (length < 0) {
                        // This case should ideally not happen if data is written correctly,
                        // but good for defensive programming.
                        throw new IOException("Read a negative length for byte array: " + length);
                    }

                    taxonomyIdsBytes = new byte[length];
                    dis.readFully(taxonomyIdsBytes); // Reads exactly 'length' bytes or throws EOFException

                    accessionTaxMap.put(acc, taxonomyIdsBytes);
                } catch (EOFException e) {
                    // This is the expected way to signal the end of the file when using DataInputStream
                    break; // Exit the loop
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from disk: " + path, e);
        }
        this.db = accessionTaxMap;
    }

    public int size() {
        if (db == null) {
            return 0;
        }
        return db.size();
    }
}
