package hr.pbf.digestdb.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccTaxDBTest {


  //  @Test
    void testAccTaxDBCreation() {

        String xmlPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/db_all_swisprot/src/uniprot_sprot.xml";
        String dbPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/db_all_swisprot/src/accession_tax.db";

        AccTaxDB accTaxDB = new AccTaxDB();
        accTaxDB.createDb(xmlPath);
        System.out.println("Created accession to taxonomy ID map with size: " + accTaxDB.size());

        accTaxDB.writeToDisk(dbPath);

        accTaxDB.readFromDisk(dbPath);

        System.out.println("Read accession to taxonomy ID map from disk with size: " + accTaxDB.size());

        assertTrue(true); // Replace with actual assertions
    }

}
