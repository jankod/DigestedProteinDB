package hr.pbf.digestdb.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class AccessionDbTest {

    private File accessionDbTest;
    private String dbPath;

    @Test
    public void testCsvCreateDelete() throws IOException {

        // create CSV in temp dir
        String csv = """
              0,B4IPW1
              1,B8IPW1
              2,A7IJ80
              3,A8INZ3
              4,A1WLI5
              5,Q1IQ54
              6,Q3AW77
              7,Q48E63
              8,Q87WP1
              9,A5IBB3""";
        accessionDbTest = File.createTempFile("accession_db_test", ".csv");
        FileUtils.writeStringToFile(accessionDbTest, csv, "UTF-8");
        dbPath = accessionDbTest.getAbsolutePath() + ".db";

        // create DB
        AccessionDbCreator creator = new AccessionDbCreator(accessionDbTest.getAbsolutePath(), dbPath);
        creator.startCreate();
        // read DB
        AccessionDbReader reader = new AccessionDbReader(dbPath);
        assertEquals("B4IPW1", reader.getAccession(0));
        assertEquals("B8IPW1", reader.getAccession(1));
        assertEquals("A7IJ80", reader.getAccession(2));
        assertEquals("A8INZ3", reader.getAccession(3));
        assertEquals("A1WLI5", reader.getAccession(4));
        assertEquals("Q1IQ54", reader.getAccession(5));
        assertEquals("Q3AW77", reader.getAccession(6));
        assertEquals("Q48E63", reader.getAccession(7));
        assertEquals("Q87WP1", reader.getAccession(8));
        assertEquals("A5IBB3", reader.getAccession(9));
        assertEquals(10, reader.getAccessionCount());



    }

    @AfterEach
    public void tearDown() {
        log.debug("Deleting files.");
        if (accessionDbTest != null && accessionDbTest.exists()) {
            FileUtils.deleteQuietly(accessionDbTest);
        }


        if (dbPath != null) {
            File dbFile = new File(dbPath);
            if (dbFile.exists()) {
                FileUtils.deleteQuietly(dbFile);
            }
        }
    }
}
