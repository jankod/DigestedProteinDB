package hr.pbf.digestdb;

import hr.pbf.digestdb.workflow.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;

@Slf4j
public class App {
    enum Mode {
        UNIPROT_TO_CSV_1,
        CSV_TO_DUCKDB_2,
        DUCKDB_TO_SORTED_CSV_3,
        SORTED_CSV_TO_ROCKS_DB_4,
        SORTED_CSV_TO_DACK_DB_5,
        CREATE_ACCESSIONS_DB_CSV_TO_ROCKS_DB_6,
        SEARCH_ACCESSION_DB_7
    }

    enum Location {
        LOCAL,
        REMOTE
    }

    public static void main(String[] args) throws Throwable {

        Mode mode = Mode.CREATE_ACCESSIONS_DB_CSV_TO_ROCKS_DB_6;
        Location location = Location.REMOTE;
        int minPeptideLength = 7;
        int maxPeptideLength = 30;


        String REMOTE_DIR = "/disk3/janko/digested_db/generated";
        String LOCAL_DIR = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated";

        String DIR_GENERATED = location == Location.LOCAL ? LOCAL_DIR : REMOTE_DIR;

        FileUtils.forceMkdir(new File(DIR_GENERATED));

        StopWatch watch = StopWatch.createStarted();
        if (mode == Mode.UNIPROT_TO_CSV_1) {
            MainUniprotToPeptideCsv app = new MainUniprotToPeptideCsv();
            app.minPeptideLength = minPeptideLength;
            app.maxPeptideLength = maxPeptideLength;

            if (location == Location.LOCAL) {
                app.fromSwisprotPath = DIR_GENERATED + "/../csv/uniprot_sprot.xml.gz";
            } else {
                app.fromSwisprotPath = DIR_GENERATED + "/../uniprot_trembl.xml.gz";
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
            MainSortedCsvToRocksDb app = new MainSortedCsvToRocksDb();
            app.fromCsvPath = DIR_GENERATED + "/peptide_mass_sorted.csv";
            app.toDbPath = DIR_GENERATED + "/rocks_mass.db";
            app.startInsertToRocksDb();
        } else if (mode == Mode.SORTED_CSV_TO_DACK_DB_5) {
            MainSortedCsvToDackDb app = new MainSortedCsvToDackDb();
            app.fromCsvPath = DIR_GENERATED + "/peptide_mass_sorted.csv";
            app.toDbPath = DIR_GENERATED + "/dack_mass.db";
            app.startInsertToDackDb();

        }else if (mode == Mode.CREATE_ACCESSIONS_DB_CSV_TO_ROCKS_DB_6) {
            MainAccessionDb app = new MainAccessionDb();
            app.setFromCsvPath(  DIR_GENERATED + "/peptide_mass_sorted_console.csv");
            app.setToRocksDbPath( DIR_GENERATED + "/rocks_accessions.db");
            app.startCreateDB();
        }else if (mode == Mode.SEARCH_ACCESSION_DB_7) {
            MainAccessionDb app = new MainAccessionDb();
            app.setToRocksDbPath( DIR_GENERATED + "/rocks_accessions.db");
            long id = app.searchAccessionDb("Q82DM9");
            log.info("ID: {}", id);
        }

        watch.stop();
        log.info("Finished time: {}", DurationFormatUtils.formatDurationHMS(watch.getTime()));
        log.info("Params \nmode: {}, \nlocation: {}, \nminPeptideLength: {}, \nmaxPeptideLength: {}, \nDIR_GENERATED {}", mode, location, minPeptideLength, maxPeptideLength, DIR_GENERATED);
    }


}
