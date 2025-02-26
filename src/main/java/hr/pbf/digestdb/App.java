package hr.pbf.digestdb;

import gnu.trove.map.hash.TObjectIntHashMap;
import hr.pbf.digestdb.util.*;
import hr.pbf.digestdb.workflow.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.util.Properties;

@Slf4j
public class App {


    public static void main(String[] args) throws Throwable {
        WorkflowConfig config = new WorkflowConfig("/Users/tag/IdeaProjects/DigestedProteinDB/misc/db_human_swisprot");

        StopWatch watch = StopWatch.createStarted();
        final String DB_DIR_PATH = config.getDbDir();
        final String PEPTIDE_MASS_CSV_PATH = DB_DIR_PATH + "/gen/peptide_mass.csv";
        final String PEPTIDE_MASS_CSV_SORTED_PATH = DB_DIR_PATH + "/gen/peptide_mass_sorted.csv";
        final String GROUPED_WITH_IDS_CSV_PATH = DB_DIR_PATH + "/gen/grouped_with_ids.csv";
        final String ACCESSION_MAP_CSV_PATH = DB_DIR_PATH + "/gen/accession_map.csv";
        final String ACCESSION_MAP_CSV_SORTED_PATH = DB_DIR_PATH + "/gen/accession_map.csv.sorted";
        final String CUSTOM_ACCESSION_DB_DIR_PATH = DB_DIR_PATH + "/custom_accession.db";
        final String ROCKDB_DB_DIR_PATH = DB_DIR_PATH + "/rocksdb_mass.db";

        File genDir = new File(DB_DIR_PATH + "/gen");

        log.info("Start workflow, params  {}!", config);

        FileUtils.forceMkdir(genDir);

        MainUniprotToPeptideCsv app1UniprotToCsv = new MainUniprotToPeptideCsv();
        {            // 1. Uniprot xml to CSV
            app1UniprotToCsv.minPeptideLength = config.getMinPeptideLength();
            app1UniprotToCsv.maxPeptideLength = config.getMaxPeptideLength();
            app1UniprotToCsv.fromSwisprotPath = config.toUniprotXmlFullPath();
            app1UniprotToCsv.missClevage = config.getMissCleavage();
            app1UniprotToCsv.setResultPeptideMassAccCsvPath(PEPTIDE_MASS_CSV_PATH);
            app1UniprotToCsv.start();
        }

        {
            // 2. export TMPDIR=/disk4/janko/temp_dir # Stvorite ovaj direktorij ako ne postoji
            //    sort -t',' -k1n peptide_mass.csv -o peptide_mass_sorted_console.csv
            MainExecuteCommand app2CmdSortMass = new MainExecuteCommand();

            String sortTempDir = config.getSortTempDir();
            String cmdString = "";
            if (sortTempDir != null) {
                if (!FileUtils.isDirectory(new File(sortTempDir))) {
                    log.info("Temp dir not exists, i will create.: {}", sortTempDir);
                    FileUtils.forceMkdir(new File(sortTempDir));
                }
                cmdString += " export TMPDIR=${sort_temp_dir} && ";
            }
            cmdString += "sort -t',' -k1n ${peptide_mass_csv_path} -o ${peptide_mass_sorted_console}";

            cmdString = new MyFormatter(cmdString)
                  .param("sort_temp_dir", sortTempDir)
                  .param("peptide_mass_csv_path", PEPTIDE_MASS_CSV_PATH)
                  .param("peptide_mass_sorted_console", PEPTIDE_MASS_CSV_SORTED_PATH)
                  .format();


            log.debug("Execute command: {} in dir {}", cmdString, genDir);

            app2CmdSortMass.exe(cmdString, genDir);
        }

        long PROTEIN_COUNT = 0;
        { // 3.
            MainCsvMassGrouperWithAccIds app3csvMassGroup = new MainCsvMassGrouperWithAccIds();
            app3csvMassGroup.setInputCsvPeptideMassSorted(PEPTIDE_MASS_CSV_SORTED_PATH);
            app3csvMassGroup.setOutputGroupedCsv(GROUPED_WITH_IDS_CSV_PATH);
            app3csvMassGroup.setOutputAccessionMapCsv(ACCESSION_MAP_CSV_PATH);

            TObjectIntHashMap<String> accCustomDb = app3csvMassGroup.startAccAndGroup();
            PROTEIN_COUNT = accCustomDb.size();
        }


        { // 4.
            //  sort -t',' -k1n accession_map.csv -o accession_map_sorted.csv
            MainExecuteCommand cmd = new MainExecuteCommand();
            String cmdSortAccession = " sort -t',' -k1n ${accession_map.csv}  -o ${accession_map.csv.sorted} ";
            cmdSortAccession = new MyFormatter(cmdSortAccession)
                  .param("accession_map.csv",  ACCESSION_MAP_CSV_PATH)
                  .param("accession_map.csv.sorted",  ACCESSION_MAP_CSV_SORTED_PATH)
                  .format();

            log.debug("Execute command: {} in dir {}", cmdSortAccession, genDir);
            cmd.exe(cmdSortAccession, genDir);
        }

        { // 5. Create rocksdb mass
            MainMassRocksDb app4createMassRocksDb = new MainMassRocksDb();

            app4createMassRocksDb.setToDbPath( ROCKDB_DB_DIR_PATH);
            app4createMassRocksDb.setFromCsvPath(GROUPED_WITH_IDS_CSV_PATH);
            app4createMassRocksDb.startCreateToRocksDb();
        }

        { // 6. Create custom accession db
            CustomAccessionDb app5createCustomAccessionDb = new CustomAccessionDb();
            app5createCustomAccessionDb.setFromCsvPath(ACCESSION_MAP_CSV_SORTED_PATH);

            app5createCustomAccessionDb.setToDbPath(CUSTOM_ACCESSION_DB_DIR_PATH);
            app5createCustomAccessionDb.startCreateCustomAccessionDb();
        }

        { // 7. Properties
           config.produceDbInfo(PROTEIN_COUNT);
        }

        log.info("Finished time: {}", DurationFormatUtils.formatDurationHMS(watch.getTime()));
    }

}
