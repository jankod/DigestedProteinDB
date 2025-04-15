package hr.pbf.digestdb.util;

import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class NcbiTaksonomyTereticalTest {

    private static NcbiTaksonomy taxonomy;
    private static String pathToCsv;

    @BeforeAll
    public static void setUp() throws NcbiTaxonomyException {
        ClassLoader classLoader = NcbiTaksonomyTereticalTest.class.getClassLoader();
        URL resource = classLoader.getResource("nodes_test_teoretical.dmp");
        assertNotNull(resource, "Test file not found!");
        pathToCsv = new File(resource.getFile()).getAbsolutePath();
        taxonomy =  NcbiTaksonomy.loadTaxonomy(pathToCsv);
        assertNotNull(taxonomy.getChildParrents());
        assertFalse(taxonomy.getChildParrents().isEmpty(), "Taxonomy should not be empty");
    }

    @Test
    public void testLoadTaxonomy() {
        assertNotNull(taxonomy, "Taxonomy should be initialized");
    }

    @Test
    public void testDirectParentRelations() {
        // Test known parent-child relationships from the test file
        assertTrue(taxonomy.isDirectParent(3, 2), "2's parent should be 3");
        assertTrue(taxonomy.isDirectParent(4, 3), "3's parent should be 4");
        assertTrue(taxonomy.isDirectParent(5, 4), "4's parent should be 5");
        assertTrue(taxonomy.isDirectParent(6, 5), "5's parent should be 6");
        assertTrue(taxonomy.isDirectParent(7, 6), "6's parent should be 7");

        assertFalse(taxonomy.isDirectParent(8, 6), "8's parent should be 6");
        assertFalse(taxonomy.isDirectParent(5, 2));
        assertFalse(taxonomy.isDirectParent(11, 1) );

    }

    @Test
    public void testAncestorRelations() {
        // Test ancestor relationships that should be true
        assertTrue(taxonomy.isAncestor(7, 6), "6 should be ancestor of 7 (direct parent)");
        assertTrue(taxonomy.isAncestor(7, 1), "1 should be ancestor of 7 (grandparent)");


        // Test ancestor relationships that should be false
        assertFalse(taxonomy.isAncestor(5, 7), "7 is not ancestor of 5");
    }

    @Test
    public void testAncestorWithMaxDepth() {
        // Test with depth limit that should succeed
        assertTrue(taxonomy.isAncestor(7, 6, 1), "6 should be ancestor of 7 with depth 1");

        // Test with depth limit that should fail
        assertFalse(taxonomy.isAncestor(7, 1, 2), "1 should not be found as ancestor within depth 2");
    }

    @Test
    public void testSelfRelationship() {
        // Test that root is not exist
        assertFalse(taxonomy.isDirectParent(1, 1), "Root (1) should not exist");
    }

    @Test
    public void testSiblingRelationships() {
        // Testing nodes that have the same parent
        int parentRoot = 1;
        assertTrue(taxonomy.isDirectParent(2, parentRoot), "2's parent should be " + parentRoot);

        // Siblings should not be ancestors of each other
        assertFalse(taxonomy.isAncestor(2, 3), "2 should not be ancestor of 3");
        // Test with a non-existent sibling
        assertFalse(taxonomy.isAncestor(2, 11), "2 should not be ancestor of 11");
        // Test with a non-existent sibling
        assertFalse(taxonomy.isAncestor(11, 2), "11 should not be ancestor of 2");

    }




    @Test
    public void testRootRelationships() {

        // Everything should have root as an ancestor
        for (int taxId = 1; taxId <= 10; taxId++) {
            assertTrue(taxonomy.isAncestor(taxId, 1),
                    "Root (1) should be ancestor of " + taxId);
        }
    }

}