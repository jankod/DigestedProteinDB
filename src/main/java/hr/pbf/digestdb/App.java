package hr.pbf.digestdb;

import hr.pbf.digestdb.util.*;
import hr.pbf.digestdb.workflow.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.*;

@Slf4j
public class App {

    public static void main(String[] args) throws Throwable {
        Properties config = new Properties();
        String paramsPath = "run_params.properties";
        if (args.length >= 1) {
            paramsPath = args[0];
        }
        log.info("Params path: " + paramsPath);
        try (FileReader reader = new FileReader(paramsPath)) {
            config.load(reader);
        }


        StopWatch watch = StopWatch.createStarted();

        String databaseDir = "database_dir";
        String dbDir = config.getProperty(databaseDir);

        log.info("Start workflow, params  {}!", config);

        FileUtils.forceMkdir(new File(dbDir + "/gen"));
        String peptideMassCsvPath = dbDir + "/gen/peptide_mass.csv";

            MainUniprotToPeptideCsv app1UniprotToCsv = new MainUniprotToPeptideCsv();
        {            // 1. Uniprot xml to CSV
            app1UniprotToCsv.minPeptideLength = Integer.parseInt(config.getProperty("min_peptide_length"));
            app1UniprotToCsv.maxPeptideLength = Integer.parseInt(config.getProperty("max_peptide_length"));
            app1UniprotToCsv.fromSwisprotPath = config.getProperty("uniprot_xml_source");
            app1UniprotToCsv.missClevage = Integer.parseInt(config.getProperty("miss_cleavage"));
            app1UniprotToCsv.setResultPeptideMassAccCsvPath(peptideMassCsvPath);
            app1UniprotToCsv.start();
        }

        {
            // 2. export TMPDIR=/disk4/janko/temp_dir # Stvorite ovaj direktorij ako ne postoji
            //    sort -t',' -k1n peptide_mass.csv -o peptide_mass_sorted_console.csv
            MainExecuteCommand app2CmdSortMass = new MainExecuteCommand();

            String sortTempDir = config.getProperty("sort_temp_dir");
            String cmdString = "";
            if (sortTempDir != null) {
                if (!FileUtils.isDirectory(new File(sortTempDir))) {
                    log.info("Temp dir not exists, i will create.: {}", sortTempDir);
                    FileUtils.forceMkdir(new File(sortTempDir));
                }
                cmdString += " export TMPDIR=${sort_temp_dir} && ";
            }
            cmdString += "sort -t',' -k1n ${peptide_mass_csv_path} -o ${peptide_mass_sorted_console}";
            String finalCmd = MyFormatter.format(cmdString,
                  "sort_temp_dir", sortTempDir,
                  "peptide_mass_csv_path", peptideMassCsvPath,
                  "peptide_mass_sorted_console", peptideMassCsvPath + ".sorted");

            File dirGen = new File(dbDir + "/gen");
            log.debug("Execute command: {} in dir {}", finalCmd, dirGen);
            app2CmdSortMass.exe(finalCmd, dirGen);
        }

        { // 3.
            MainCsvMassGrouperWithAccIds app3csvMassGroup = new MainCsvMassGrouperWithAccIds();
            app3csvMassGroup.setInputCsvPeptideMassSorted(peptideMassCsvPath + ".sorted");
            app3csvMassGroup.setOutputGroupedCsv(dbDir + "/gen/grouped_with_ids.csv");
            app3csvMassGroup.setOutputAccessionMapCsv(dbDir + "/gen/accession_map.csv");

            app3csvMassGroup.startAccAndGroup();
        }

        { // 4.
            //  sort -t',' -k1n accession_map.csv -o accession_map_sorted.csv
            MainExecuteCommand cmd = new MainExecuteCommand();
            String cmdFinal = " sort -t',' -k1n accession_map.csv -o accession_map.csv.sorted ";
            File dir = new File(dbDir + "/gen");
            log.debug("Execute command: {} in dir {}", cmdFinal, dir);
            cmd.exe(cmdFinal, dir);
        }

        { // 5. Create rocksdb mass
            MainMassRocksDb app4createMassRocksDb = new MainMassRocksDb();
            app4createMassRocksDb.setToDbPath(dbDir + "/rocksdb_mass.db");
            app4createMassRocksDb.setFromCsvPath(dbDir + "/gen/grouped_with_ids.csv");
            app4createMassRocksDb.startCreateToRocksDb();
        }

        { // 6. Create custom accession db
            CustomAccessionDb app5createCustomAccessionDb = new CustomAccessionDb();
            app5createCustomAccessionDb.setFromCsvPath(dbDir + "/gen/accession_map.csv.sorted");
            app5createCustomAccessionDb.setToDbPath(dbDir + "/rocksdb_accession.db");
            app5createCustomAccessionDb.startCreateCustomAccessionDb();
        }

        { // 7. create db-info.properties
            Properties prop = new Properties();
            prop.put("source", "Uniprot Swis-Prot human");
            prop.put("enzime", "Trypsine");
            prop.put("miss_cleavage", app1UniprotToCsv.getMissClevage());
            prop.put("min_peptide_length", Integer.parseInt(config.getProperty("min_peptide_length")));
            prop.put("max_peptide_length", Integer.parseInt(config.getProperty("max_peptide_length")));
            try (FileOutputStream out = FileUtils.openOutputStream(new File(dbDir + "/db-info.properties"))) {
                prop.store(out, "DB desriptions");
            }

        }



        log.info("Finished time: {}", DurationFormatUtils.formatDurationHMS(watch.getTime()));
        log.info("Params {}!", config);


//    {
//        MainCsvToDackdb app = new MainCsvToDackdb();
//        app.maxPeptideLength = maxPeptideLength;
//        app.fromCsvPath = dbDir + "/peptide_mass.csv";
//        app.toDbUrl = "jdbc:duckdb:" + dbDir + "/dack_mass.db";
//        app.start();
//    } else if(runner ==Runner.DUCKDB_TO_SORTED_CSV_3)
//
//    {
//        MainExportSortedDackDb app = new MainExportSortedDackDb();
//        app.fromDbUrl = "jdbc:duckdb:" + dbDir + "/dack_mass.db";
//        app.toCsvPath = dbDir + "/peptide_mass_sorted.csv";
//        app.start();
//    } else if(runner ==Runner.CREATE_MASS_ROCKS_DB_4)
//
//    {
//        MainMassRocksDb app = new MainMassRocksDb(dbDir);
////            app.fromCsvPath = dbDir + "/peptide_mass_sorted_console.csv";
////            app.toDbPath = dbDir + "/rocks_mass.db";
//        //  app.dbAccessionPath = dbDir + "/rocks_accessions.db";
//        app.startCreateToRocksDb();
//    } else if(runner ==Runner.SEARCH_DB_ROCKS_BY_MASS_4)
//
//    {
//        MainMassRocksDb app = new MainMassRocksDb(dbDir);
//        //app.toDbPath = dbDir + "/rocks_mass.db";
//        //   app.dbAccessionPath = dbDir + "/rocks_accessions.db";
//
//        RocksDB db = app.openReadDB();
//        List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> searchResults = app.searchByMass(db, 700, 700.3);
//        log.debug("Search results: {}", searchResults.size());
//
//
//    } else if(runner ==Runner.SORTED_CSV_TO_DACK_DB_5)
//
//    {
//        MainSortedCsvToDackDb app = new MainSortedCsvToDackDb();
//        app.fromCsvPath = dbDir + "/peptide_mass_sorted.csv";
//        app.toDbPath = dbDir + "/dack_mass.db";
//        app.startInsertToDackDb();
//
//    } else if(runner ==Runner.CREATE_ACCESSIONS_DB_CSV_TO_ROCKS_DB_6)
//
//    {
//        MainAccessionDb app = new MainAccessionDb();
//        app.setFromCsvPath(dbDir + "/peptide_mass_sorted_console.csv");
//        app.setToRocksDbPath(dbDir + "/rocks_accessions.db");
//        app.startCreateDB();
//    } else if(runner ==Runner.SEARCH_ACCESSION_DB_7)
//
//    {
//        MainAccessionDb app = new MainAccessionDb();
//        app.setToRocksDbPath(dbDir + "/rocks_accessions.db");
//        String accc = app.searchAccessionDb(1L);
//        log.info("ID: {}", accc);
//    } else if(runner ==Runner.CSV_GROUP)
//
//    {
//        MainCsvMassGrouper app = new MainCsvMassGrouper();
//        app.inputCsv = dbDir + "/peptide_mass_sorted_console.csv";
//        app.outputCsv = dbDir + "/peptide_mass_sorted_console_grouped.csv";
//        app.start();
//    } else if(runner ==Runner.GROUP_WITH_ACC_ID)
//
//    {
//        MainCsvMassGrouperWithAccIds app = new MainCsvMassGrouperWithAccIds(dbDir);
//        //int bufferSize = 16 * 1024 * 1024; // 16MB buffer
////            app.setInputCsv(dbDir + "/peptide_mass_sorted_console.csv");
////            app.setOutputGroupedCsv(dbDir + "/grouped_with_ids.csv");
////            app.setOutputAccessionMapCsv(dbDir + "/accession_map.csv");
//        app.start();
//
//    } else if(runner ==Runner.CREATE_MAPDB)
//
//    {
//        MapdbPeptideDatabase app = new MapdbPeptideDatabase();
//        app.setGroupedCsvPath(dbDir + "/grouped_with_ids.csv");
//        app.setDbPath(dbDir + "/mapdb_sstable.db");
//        app.startBuild();
//    } else if(runner ==Runner.SEARCH_MAPDB)
//
//    {
//        MapdbPeptideDatabase app = new MapdbPeptideDatabase();
//        app.setDbPath(dbDir + "/mapdb_sstable.db");
//        app.startSearchMain(args);
//    } else if(runner ==Runner.WEB_APP)
//
//    {
//        MainWeb app = new MainWeb(7070, dbDir);
////            app.setRocksDbPath(dbDir + "/rocks_mass.db");
////            app.setAccDbPath(dbDir + "/rocks_accessions.db");
////            app.setDbInfoPath(dbDir + "/db-info_bacteria_trembl.properties");
//        app.startWeb();
//    } else if(runner ==Runner.GENERATE_ACCESSION_CUSTOM_DB)
//

//    {
//        CustomAccessionDb app = new CustomAccessionDb(dbDir);
//        app.startCreateCustomAccessionDb();
//    }


    }

}
