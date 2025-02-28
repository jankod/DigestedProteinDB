package hr.pbf.digestdb;

import gnu.trove.map.hash.TObjectIntHashMap;
import hr.pbf.digestdb.util.*;
import hr.pbf.digestdb.workflow.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import picocli.CommandLine;

import java.io.File;

@Slf4j
public class AppWorkflow {

    @Data
    static class ArgsParams {

        @CommandLine.Option(names = {"--clean", "-c"}, description = "Clean all files in db dir")
        boolean clean = false;

        @CommandLine.Option(names = {"--db-dir", "-d"}, required = true, description = "Path to the directory with workflow.properties file")
        String dbDir = "";
    }

    public static void main(String[] args) throws Throwable {

        ArgsParams params = new ArgsParams();
        new CommandLine(params).parseArgs(args);

        WorkflowConfig config = new WorkflowConfig(params.dbDir);

        StopWatch watch = StopWatch.createStarted();
        final String DB_DIR_PATH = config.getDbDir();
        final String PEPTIDE_MASS_CSV_PATH = DB_DIR_PATH + "/gen/peptide_mass.csv";
        final String PEPTIDE_MASS_CSV_SORTED_PATH = DB_DIR_PATH + "/gen/peptide_mass_sorted.csv";
        final String GROUPED_WITH_IDS_CSV_PATH = DB_DIR_PATH + "/gen/grouped_with_ids.csv";
        final String ACCESSION_MAP_CSV_PATH = DB_DIR_PATH + "/gen/accession_map.csv";
        final String ACCESSION_MAP_CSV_SORTED_PATH = DB_DIR_PATH + "/gen/accession_map.csv.sorted";
        final String CUSTOM_ACCESSION_DB_DIR_PATH = DB_DIR_PATH + "/" + CustomAccessionDb.CUSTOM_ACCESSION_DB_FILE_NAME;
        final String ROCKDB_DB_DIR_PATH = DB_DIR_PATH + "/" + MassRocksDb.ROCKSDB_MASS_DB_FILE_NAME;
        final String TAX_ACC_CSV_PATH = DB_DIR_PATH + "/gen/tax_acc.csv";
        final String DB_INFO_PROPERTIES_PATH = DB_DIR_PATH + "/db_info.properties";

        File genDir = new File(DB_DIR_PATH + "/gen");

        // if args contain --clean
        if (params.isClean()) {
            log.info("Clean all files in dir: {}", DB_DIR_PATH);
            FileUtils.deleteQuietly(genDir);
            FileUtils.deleteQuietly(new File(ROCKDB_DB_DIR_PATH));
            FileUtils.deleteQuietly(new File(CUSTOM_ACCESSION_DB_DIR_PATH));
            FileUtils.deleteQuietly(new File(DB_INFO_PROPERTIES_PATH));
        }


        log.info("Start workflow, params  {}!", config);

        FileUtils.forceMkdir(genDir);

        JobLancher jobLancher = new JobLancher();

        {            // 1. Uniprot xml to CSV
            JobUniprotToPeptideCsv app1UniprotToCsv = new JobUniprotToPeptideCsv();
            app1UniprotToCsv.minPeptideLength = config.getMinPeptideLength();
            app1UniprotToCsv.maxPeptideLength = config.getMaxPeptideLength();
            app1UniprotToCsv.fromSwisprotPath = config.toUniprotXmlFullPath();
            app1UniprotToCsv.missClevage = config.getMissCleavage();
            app1UniprotToCsv.setResultPeptideMassAccCsvPath(PEPTIDE_MASS_CSV_PATH);
            app1UniprotToCsv.setResultTaxAccCsvPath(TAX_ACC_CSV_PATH);
            JobResult<JobUniprotToPeptideCsv.Result> result = jobLancher.run(app1UniprotToCsv);
        }

        {
            // 2. export TMPDIR=/disk4/janko/temp_dir # Stvorite ovaj direktorij ako ne postoji
            //    sort -t',' -k1n peptide_mass.csv -o peptide_mass_sorted_console.csv
            ExecJob app2CmdSortMass = new ExecJob();

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

            app2CmdSortMass.setCmd(cmdString);
            app2CmdSortMass.setDir(genDir);
            jobLancher.run(app2CmdSortMass);
        }

        long proteinCountResult = 0;
        { // 3.
            MainCsvMassGrouperWithAccIds app3csvMassGroup = new MainCsvMassGrouperWithAccIds();
            app3csvMassGroup.setInputCsvPeptideMassSorted(PEPTIDE_MASS_CSV_SORTED_PATH);
            app3csvMassGroup.setOutputGroupedCsv(GROUPED_WITH_IDS_CSV_PATH);
            app3csvMassGroup.setOutputAccessionMapCsv(ACCESSION_MAP_CSV_PATH);

            TObjectIntHashMap<String> accCustomDb = app3csvMassGroup.startAccAndGroup();
            proteinCountResult = accCustomDb.size();
        }

        { // 4.
            //  sort -t',' -k1n accession_map.csv -o accession_map_sorted.csv
            ExecJob cmd = new ExecJob();
            String cmdSortAccession = " sort -t',' -k1n ${accession_map.csv}  -o ${accession_map.csv.sorted} ";
            cmdSortAccession = new MyFormatter(cmdSortAccession)
                  .param("accession_map.csv", ACCESSION_MAP_CSV_PATH)
                  .param("accession_map.csv.sorted", ACCESSION_MAP_CSV_SORTED_PATH)
                  .format();

            log.debug("Execute command: {} in dir {}", cmdSortAccession, genDir);
            cmd.setCmd(cmdSortAccession);
            cmd.setDir(genDir);
            jobLancher.addJob(cmd);
            jobLancher.run(cmd);
        }

        { // 5. Create rocksdb mass
            MassRocksDb app4createMassRocksDb = new MassRocksDb();

            app4createMassRocksDb.setToDbPath(ROCKDB_DB_DIR_PATH);
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

            config.saveDbInfoProperties(0, DB_INFO_PROPERTIES_PATH);
        }

        log.info("Finished time: {}", DurationFormatUtils.formatDurationHMS(watch.getTime()));
    }

}
