package hr.pbf.digestdb;

import hr.pbf.digestdb.workflow.MainCsvMassGrouper;
import hr.pbf.digestdb.workflow.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.util.List;

@Slf4j
public class App {
    enum Mode {
        UNIPROT_TO_CSV_1,
        CSV_TO_DUCKDB_2,
        DUCKDB_TO_SORTED_CSV_3,
        SORTED_CSV_TO_ROCKS_DB_4,
        SORTED_CSV_TO_DACK_DB_5,
        CREATE_ACCESSIONS_DB_CSV_TO_ROCKS_DB_6,
        SEARCH_DB_ROCKS_BY_MASS_4, CSV_GROUP, GROUP_WITH_ACC_ID, SEARCH_ACCESSION_DB_7
    }

    enum Location {
        LOCAL,
        REMOTE
    }

    public static void main(String[] args) throws Throwable {

        Mode mode = Mode.GROUP_WITH_ACC_ID;
        Location location;
        // if mac then is local, otherwise remote
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            location = Location.LOCAL;
        } else {
            location = Location.REMOTE;
        }
        int minPeptideLength = 7;
        int maxPeptideLength = 30;


        String REMOTE_DIR = "/disk3/janko/digested_db/generated_bacteria";
        String LOCAL_DIR = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_human";

        String DIR_GENERATED = location == Location.LOCAL ? LOCAL_DIR : REMOTE_DIR;

        FileUtils.forceMkdir(new File(DIR_GENERATED));


        log.info("START MODE={} DIR= {} " ,mode, DIR_GENERATED);
        StopWatch watch = StopWatch.createStarted();
        if (mode == Mode.UNIPROT_TO_CSV_1) {
            MainUniprotToPeptideCsv app = new MainUniprotToPeptideCsv();
            app.minPeptideLength = minPeptideLength;
            app.maxPeptideLength = maxPeptideLength;

            if (location == Location.LOCAL) {
                app.fromSwisprotPath = DIR_GENERATED + "/../csv/uniprot_sprot.xml.gz";

            } else {
                //app.fromSwisprotPath = DIR_GENERATED + "/../uniprot_trembl.xml.gz";
                app.fromSwisprotPath = DIR_GENERATED + "/../uniprot_trembl_bacteria.xml.gz";
            }
            app.toCsvPath = DIR_GENERATED + "/peptide_mass.csv";

            app.start();
        } else if (mode == Mode.CSV_TO_DUCKDB_2) {
            MainCsvToDackdb app = new MainCsvToDackdb();
            app.maxPeptideLength = maxPeptideLength;
            app.fromCsvPath = DIR_GENERATED + "/peptide_mass.csv";
            app.toDbUrl = "jdbc:duckdb:" + DIR_GENERATED + "/dack_mass.db";
            app.start();
        } else if (mode == Mode.DUCKDB_TO_SORTED_CSV_3) {
            MainExportSortedDackDb app = new MainExportSortedDackDb();
            app.fromDbUrl = "jdbc:duckdb:" + DIR_GENERATED + "/dack_mass.db";
            app.toCsvPath = DIR_GENERATED + "/peptide_mass_sorted.csv";
            app.start();
        } else if (mode == Mode.SORTED_CSV_TO_ROCKS_DB_4) {
            MainMassRocksDb app = new MainMassRocksDb();
            app.fromCsvPath = DIR_GENERATED + "/peptide_mass_sorted_console.csv";
            app.toDbPath = DIR_GENERATED + "/rocks_mass.db";
            app.dbAccessionPath = DIR_GENERATED + "/rocks_accessions.db";
            app.startInsertToRocksDb();
        } else if (mode == Mode.SEARCH_DB_ROCKS_BY_MASS_4) {
            MainMassRocksDb app = new MainMassRocksDb();
            app.toDbPath = DIR_GENERATED + "/rocks_mass.db";
            app.dbAccessionPath = DIR_GENERATED + "/rocks_accessions.db";

            List<MainMassRocksDb.SearchResult> searchResults = app.searchByMass(400, 1.3);
            log.debug("Search results: {}", searchResults.size());


        } else if (mode == Mode.SORTED_CSV_TO_DACK_DB_5) {
            MainSortedCsvToDackDb app = new MainSortedCsvToDackDb();
            app.fromCsvPath = DIR_GENERATED + "/peptide_mass_sorted.csv";
            app.toDbPath = DIR_GENERATED + "/dack_mass.db";
            app.startInsertToDackDb();

        } else if (mode == Mode.CREATE_ACCESSIONS_DB_CSV_TO_ROCKS_DB_6) {
            MainAccessionDb app = new MainAccessionDb();
            app.setFromCsvPath(DIR_GENERATED + "/peptide_mass_sorted_console.csv");
            app.setToRocksDbPath(DIR_GENERATED + "/rocks_accessions.db");
            app.startCreateDB();
        } else if (mode == Mode.SEARCH_ACCESSION_DB_7) {
            MainAccessionDb app = new MainAccessionDb();
            app.setToRocksDbPath(DIR_GENERATED + "/rocks_accessions.db");
            String accc = app.searchAccessionDb(1L);
            log.info("ID: {}", accc);
        } else if (mode == Mode.CSV_GROUP) {
            MainCsvMassGrouper app = new MainCsvMassGrouper();
            app.inputCsv = DIR_GENERATED + "/peptide_mass_sorted_console.csv";
            app.outputCsv = DIR_GENERATED + "/peptide_mass_sorted_console_grouped.csv";
            app.start();
        } else if (mode == Mode.GROUP_WITH_ACC_ID) {
            MainCsvMassGrouperWithAccIds app = new MainCsvMassGrouperWithAccIds();
            //int bufferSize = 16 * 1024 * 1024; // 16MB buffer
            app.setInputCsv(DIR_GENERATED + "/peptide_mass_sorted_console.csv");
            app.setOutputGroupedCsv(DIR_GENERATED + "/grouped_with_ids.csv");
            app.setOutputAccessionMapCsv(DIR_GENERATED + "/accession_map.csv");
            app.start();

        }

        watch.stop();
        log.info("Finished time: {}", DurationFormatUtils.formatDurationHMS(watch.getTime()));
        log.info("Params \nmode: {}, \nlocation: {}, \nminPeptideLength: {}, \nmaxPeptideLength: {}, \nDIR_GENERATED {}", mode, location, minPeptideLength, maxPeptideLength, DIR_GENERATED);
    }


}
