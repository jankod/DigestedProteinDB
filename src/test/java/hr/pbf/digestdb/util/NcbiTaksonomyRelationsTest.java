package hr.pbf.digestdb.util;

import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class NcbiTaksonomyRelationsTest {

    private static NcbiTaksonomyRelations taxonomy;
    private static String pathToCsv;

    @BeforeAll
    public static void setUp() throws NcbiTaxonomyException {
        ClassLoader classLoader = NcbiTaksonomyRelationsTest.class.getClassLoader();
        URL resource = classLoader.getResource("nodes_test.dmp.csv");
        assertNotNull(resource, "Test file not found!");
        pathToCsv = new File(resource.getFile()).getAbsolutePath();
        taxonomy = NcbiTaksonomyRelations.loadTaxonomyNodes(pathToCsv);
        assertNotNull(taxonomy.getChildParrentsMap());
        assertFalse(taxonomy.getChildParrentsMap().isEmpty(), "Taxonomy should not be empty");
    }


    @Test
    void testGetDivisionForTaxId() {
        System.out.println(taxonomy.getTaxIdToDivisionIdMap());
        assertEquals(0, taxonomy.getDivisionForTaxId(2));

        assertEquals(0, taxonomy.getDivisionForTaxId(7));

        assertEquals(-1, taxonomy.getDivisionForTaxId(999999));
    }

    @Test
    void testIsTaxIdInDivision() {
        // Provjera točne pripadnosti
        assertTrue(taxonomy.isTaxIdInDivision(7, 0));

        // Provjera netočne pripadnosti
        assertFalse(taxonomy.isTaxIdInDivision(1, 0));
        assertFalse(taxonomy.isTaxIdInDivision(7, 1));

        // Provjera za nepostojeći tax_id
        assertFalse(taxonomy.isTaxIdInDivision(999999, 0));
    }


    @Test
    void testSheep() {
        // sheep 9940
        // virus 11588
        boolean ancestor = taxonomy.isAncestor(9940, 11588);
        assertFalse(ancestor, "9940 should not be ancestor of 11588");
    }

    @Test
    public void testLoadTaxonomyNodes() {
        assertNotNull(taxonomy, "Taxonomy should be initialized");
    }

    @Test
    public void testDirectParentRelations() {
        // Test known parent-child relationships from the test file
        assertTrue(taxonomy.isDirectParent(23, 22), "22's parent should be 23");

        // Test false relationships
        assertFalse(taxonomy.isDirectParent(1, 2), "1 is not parent of 2");
        assertFalse(taxonomy.isDirectParent(6, 7), "6 is not parent of 7 (it's the other way around)");
    }

    @Test
    public void testAncestorRelations() {
        // Test ancestor relationships that should be true
        assertTrue(taxonomy.isAncestor(7, 6), "6 should be ancestor of 7 (direct parent)");
        assertTrue(taxonomy.isAncestor(7, 335928), "335928 should be ancestor of 7 (grandparent)");


        // Test ancestor relationships that should be false
        assertFalse(taxonomy.isAncestor(335928, 7), "7 is not ancestor of 335928");
    }

    @Test
    public void testAncestorWithMaxDepth() {
        // Test with depth limit that should succeed
        assertTrue(taxonomy.isAncestor(7, 6, 1), "6 should be ancestor of 7 with depth 1");
        assertTrue(taxonomy.isAncestor(7, 335928, 2), "335928 should be ancestor of 7 with depth 2");

        // Test with depth limit that should fail
        assertFalse(taxonomy.isAncestor(7, 335928, 1), "335928 should not be found as ancestor within depth 1");
        assertFalse(taxonomy.isAncestor(7, 1, 2), "1 should not be found as ancestor within depth 2");
    }

    @Test
    public void testSelfRelationship() {
        // Test that root is its own parent
        assertFalse(taxonomy.isDirectParent(1, 1), "Root (1) should not be its own parent");
    }

    @Test
    public void testSiblingRelationships() {
        // Testing nodes that have the same parent
        int parent22 = 20;
        assertTrue(taxonomy.isDirectParent(21, parent22), "21's parent should be " + parent22);

        // Siblings should not be ancestors of each other
        assertFalse(taxonomy.isAncestor(23, 24), "23 should not be ancestor of 24 (siblings)");
        assertFalse(taxonomy.isAncestor(24, 25), "24 should not be ancestor of 25 (siblings)");
        assertFalse(taxonomy.isAncestor(25, 23), "25 should not be ancestor of 23 (siblings)");
    }


    @Test
    public void testSpecificRelationships() {
        // Test specific relationships from nodes_test.dmp
        assertTrue(taxonomy.isDirectParent(9, 32199), "9's parent should be 32199");
        assertTrue(taxonomy.isDirectParent(11, 1707), "11's parent should be 1707");
        assertTrue(taxonomy.isDirectParent(14, 13), "14's parent should be 13");
        assertTrue(taxonomy.isDirectParent(17, 16), "17's parent should be 16");
        assertTrue(taxonomy.isDirectParent(19, 2812025), "19's parent should be 2812025");
        assertTrue(taxonomy.isDirectParent(21, 20), "21's parent should be 20");

        // Test some multi-level ancestry
        assertTrue(taxonomy.isAncestor(14, 13), "13 should be ancestor of 14");
        assertTrue(taxonomy.isAncestor(17, 16), "16 should be ancestor of 17");
    }


}
