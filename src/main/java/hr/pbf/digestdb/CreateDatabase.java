package hr.pbf.digestdb;

import hr.pbf.digestdb.db.AccessionDbCreator;
import hr.pbf.digestdb.db.MassRocksDbCreator;
import hr.pbf.digestdb.model.Chymotrypsin;
import hr.pbf.digestdb.model.Enzyme;
import hr.pbf.digestdb.model.TaxonomyDivision;
import hr.pbf.digestdb.model.Trypsine;
import hr.pbf.digestdb.util.*;
import hr.pbf.digestdb.workflow.*;
import hr.pbf.digestdb.workflow.BashCommand;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CreateDatabase {

    public static final String DEFAULT_ROCKSDB_MASS_DB_FILE_NAME = "rocksdb_mass.db";
    public static final String DEFAULT_ACCESSION_DB_FILE_NAME = "custom_accession.db";
    public static final String DEFAULT_ACC_TAX_DB_FILE_NAME = "accession_tax.db";

    private final CreateDatabaseConfig config;

    @Data
    public static class CreateDatabaseConfig {
        int minPeptideLength = 7;
        int maxPeptideLength = 50;
        int missedCleavage = 2;
        boolean cleanBefore = false;
        String dbDir;
        String uniprotXmlPath;
        String sortTempDir;
        String dbName;
        SupportedEnzime enzymeType = SupportedEnzime.Trypsin;
        TaxonomyDivision taxonomyDivision = TaxonomyDivision.ALL;
        int[] taxonomyParentsIds;
        String ncbiTaxonomyPath = null;


        public enum SupportedEnzime {

            Trypsin(new Trypsine()),
            Chymotrypsin(new Chymotrypsin());

            @Getter
            private final Enzyme enzyme;

            SupportedEnzime(Enzyme enzyme) {
                this.enzyme = enzyme;
            }
        }
    }

    public CreateDatabase(CreateDatabaseConfig config) {
        this.config = config;
    }

    public void start() throws Exception {

        List<Integer> steps = List.of(1, 2, 3, 4, 5, 6, 7);
        //	steps = List.of(3, 4, 5, 6, 7); // for biopro

        StopWatch watch = StopWatch.createStarted();
        final String DB_DIR_PATH = config.dbDir;

        // ACC is a String accession number
        // ACCID is an int accession id
        // PEP is a String peptide sequence
        // TAXID is an int taxonomy id from NCBI taxonomy


        final String MASS_PEP_ACC_CSV_PATH = DB_DIR_PATH + "/gen/mass_pep_acc.csv";
        final String MASS_PEP_ACC_CSV_SORTED_PATH = DB_DIR_PATH + "/gen/mass_pep_acc_sorted.csv";
        final String MASS_PEP_ACCID_GROUPED_CSV_PATH = DB_DIR_PATH + "/gen/mass_pep_accid_grouped.csv";
        final String ACCID_ACC_CSV_PATH = DB_DIR_PATH + "/gen/accid_acc.csv";
        final String ACCID_ACC_CSV_SORTED_PATH = DB_DIR_PATH + "/gen/accid_acc_sorted.csv";
        final String CUSTOM_ACCESSION_DB_DIR_PATH = DB_DIR_PATH + "/" + DEFAULT_ACCESSION_DB_FILE_NAME;
        final String ROCKDB_DB_DIR_PATH = DB_DIR_PATH + "/" + DEFAULT_ROCKSDB_MASS_DB_FILE_NAME;
        final String ACC_TAXID_CSV_PATH = DB_DIR_PATH + "/acc_taxid.csv";
        final String DB_INFO_PROPERTIES_PATH = DB_DIR_PATH + "/db_info.properties";

        File genDir = new File(DB_DIR_PATH + "/gen");

        // if args contain --clean
        if (config.cleanBefore) {
            log.info("Clean all files in dir: {}", DB_DIR_PATH);
            FileUtils.deleteQuietly(genDir);
            FileUtils.deleteQuietly(new File(ROCKDB_DB_DIR_PATH));
            FileUtils.deleteQuietly(new File(CUSTOM_ACCESSION_DB_DIR_PATH));
            FileUtils.deleteQuietly(new File(DB_INFO_PROPERTIES_PATH));
        }

        log.info("Start create DB, params  {}!", config);

        if (!genDir.isDirectory())
            FileUtils.forceMkdir(genDir);

        JobUniprotToPeptideCsv.Result readXmlResult;
        if (steps.contains(1)) {            // 1. Uniprot xml to CSV
            JobUniprotToPeptideCsv app1UniprotToCsv = new JobUniprotToPeptideCsv();
            app1UniprotToCsv.minPeptideLength = config.getMinPeptideLength();
            app1UniprotToCsv.maxPeptideLength = config.getMaxPeptideLength();
            app1UniprotToCsv.missedClevage = config.getMissedCleavage();
            app1UniprotToCsv.fromSwisprotPath = config.getUniprotXmlPath();
            app1UniprotToCsv.setTaxonomyDivision(config.getTaxonomyDivision());
            app1UniprotToCsv.setTaxonomyParentsIds(config.getTaxonomyParentsIds());

            //    app1UniprotToCsv.setMaxProteinCount(500); // FIXME: remove this after testing

            app1UniprotToCsv.setEnzyme(config.enzymeType.getEnzyme());
            app1UniprotToCsv.setResultPeptideMassAccCsvPath(MASS_PEP_ACC_CSV_PATH);
            app1UniprotToCsv.setResultAccTaxCsvPath(ACC_TAXID_CSV_PATH);
            app1UniprotToCsv.setNcbiTaxonomyPath(config.getNcbiTaxonomyPath());
            app1UniprotToCsv.setTaxonomyParentsIds(config.getTaxonomyParentsIds());

            readXmlResult = app1UniprotToCsv.start();
            log.info("Uniprot extracted. Protein count: {}, Peptide count: {}", readXmlResult.getProteinCount(), readXmlResult.getPeptideCount());
        } else {
            log.info("Skip step 1");
            readXmlResult = new JobUniprotToPeptideCsv.Result();
        }

        if (steps.contains(2)) {
            // 2. export TMPDIR=.../temp_dir
            //    sort -t',' -k1n peptide_mass.csv -o peptide_mass_sorted_console.csv
            BashCommand app2CmdSortMass = new BashCommand();

            String sortTempDir = config.getSortTempDir();
            String cmdString = "";

            if (sortTempDir != null) {
                if (!FileUtils.isDirectory(new File(sortTempDir))) {
                    log.info("Temp dir not exists, i will create.: {}", sortTempDir);
                    FileUtils.forceMkdir(new File(sortTempDir));
                }
                cmdString += " export TMPDIR=${sort_temp_dir} && ";
            }
            // -S 8G for memory
            cmdString += "sort  -t',' -k1n ${peptide_mass_csv_path} -o ${peptide_mass_sorted_console}";

            cmdString = new MyFormatter(cmdString)
                  .param("sort_temp_dir", sortTempDir)
                  .param("peptide_mass_csv_path", MASS_PEP_ACC_CSV_PATH)
                  .param("peptide_mass_sorted_console", MASS_PEP_ACC_CSV_SORTED_PATH)
                  .format();

            log.debug("Execute command: {} in dir {}", cmdString, genDir);
            app2CmdSortMass.setCmd(cmdString);
            app2CmdSortMass.setDir(genDir);
            app2CmdSortMass.start();

            // delete old file
            File oldMassCsv = new File(MASS_PEP_ACC_CSV_PATH);
            if (oldMassCsv.exists()) {
                try {
                    FileUtils.forceDelete(oldMassCsv);
                    log.info("Deleted old mass csv file: {}", MASS_PEP_ACC_CSV_PATH);
                } catch (IOException e) {
                    log.error("Error deleting old mass csv file: {}", MASS_PEP_ACC_CSV_PATH, e);
                }
            } else {
                log.info("Old mass csv file does not exist: {}", MASS_PEP_ACC_CSV_PATH);
            }

        } else {
            log.info("Skip step 2");
        }

        if (steps.contains(3)) { // 3.
            JobCsvMassGrouperWithAccIds app3csvMassGroup = new JobCsvMassGrouperWithAccIds();
            app3csvMassGroup.setInputCsvPeptideMassSorted(MASS_PEP_ACC_CSV_SORTED_PATH);
            app3csvMassGroup.setOutputGroupedCsv(MASS_PEP_ACCID_GROUPED_CSV_PATH);
            app3csvMassGroup.setOutputAccessionMapCsv(ACCID_ACC_CSV_PATH);
            app3csvMassGroup.setProteinCount(readXmlResult.getProteinCount());
            Long2IntMap accCustomDb = app3csvMassGroup.start();
            log.info("Grouped with ids: {}", MASS_PEP_ACCID_GROUPED_CSV_PATH);
            log.info("Protein count: {}", readXmlResult.getProteinCount());
        } else {
            log.info("Skip step 3");
        }

        if (steps.contains(4)) { // 4.
            //  sort -t',' -k1n accession_map.csv -o accession_map_sorted.csv
            BashCommand cmd = new BashCommand();
            String cmdSortAccession = "";
            String sortTempDir = config.getSortTempDir();
            if (sortTempDir != null) {
                if (!FileUtils.isDirectory(new File(sortTempDir))) {
                    log.info("Temp dir not exists, i will create.: {}", sortTempDir);
                    FileUtils.forceMkdir(new File(sortTempDir));
                }
                cmdSortAccession += " export TMPDIR=${sort_temp_dir} && ";
            }

            cmdSortAccession += " sort -t',' -k1n ${accession_map.csv}  -o ${accession_map.csv.sorted} ";
            cmdSortAccession = new MyFormatter(cmdSortAccession)
                  .param("sort_temp_dir", sortTempDir)
                  .param("accession_map.csv", ACCID_ACC_CSV_PATH)
                  .param("accession_map.csv.sorted", ACCID_ACC_CSV_SORTED_PATH)
                  .format();

            log.debug("Execute command: {} in dir {}", cmdSortAccession, genDir);
            cmd.setCmd(cmdSortAccession);
            cmd.setDir(genDir);
            cmd.start();
            log.info("Accession map sorted: {}", ACCID_ACC_CSV_SORTED_PATH);

            // delete old file
            File oldAccCsv = new File(ACCID_ACC_CSV_PATH);
            if (oldAccCsv.exists()) {
                try {
                    FileUtils.forceDelete(oldAccCsv);
                    log.info("Deleted old accession csv file: {}", ACCID_ACC_CSV_PATH);
                } catch (IOException e) {
                    log.error("Error deleting old accession csv file: {}", ACCID_ACC_CSV_PATH, e);
                }
            }
        } else {
            log.info("Skip step 4");
        }

        if (steps.contains(5)) { // 5. Create rocksdb mass
            //  MassRocksDb app4createMassRocksDb = new MassRocksDb();
            MassRocksDbCreator massDb = new MassRocksDbCreator(MASS_PEP_ACCID_GROUPED_CSV_PATH, ROCKDB_DB_DIR_PATH);

            massDb.startCreate();
            log.info("RockDB db is created: {}", ROCKDB_DB_DIR_PATH);
        } else {
            log.info("Skip step 5");
        }

        if (steps.contains(6)) { // 6. Create custom accession db
            AccessionDbCreator accDb = new AccessionDbCreator(ACCID_ACC_CSV_SORTED_PATH, CUSTOM_ACCESSION_DB_DIR_PATH);
            accDb.startCreate();
            log.info("Custom accession db is created: {}. File: size: {}", CUSTOM_ACCESSION_DB_DIR_PATH, MyUtil.getFileSize(CUSTOM_ACCESSION_DB_DIR_PATH));
        } else {
            log.info("Skip step 6");
        }

        if (steps.contains(7)) { // 7. Properties
            saveDbInfoToProperties(readXmlResult.getProteinCount(), DB_INFO_PROPERTIES_PATH, readXmlResult.getPeptideCount());
        } else {
            log.info("Skip step 7");
        }

        log.info("Finished time: {}", DurationFormatUtils.formatDurationHMS(watch.getTime(TimeUnit.NANOSECONDS)));
    }

    private void saveDbInfoToProperties(long proteinCount, String dbInfoPropertiesPath, long peptideCount) {

        Properties prop = new Properties();
        //      prop.setProperty("uniprot_xml_path", config.getUniprotXmlPath());
        prop.setProperty("min_peptide_length", String.valueOf(config.getMinPeptideLength()));
        prop.setProperty("max_peptide_length", String.valueOf(config.getMaxPeptideLength()));
        prop.setProperty("miss_cleavage", String.valueOf(config.getMissedCleavage()));
        prop.setProperty("db_name", config.getDbName());
        prop.setProperty("enzyme_name", config.getEnzymeType().name());
        prop.setProperty("protein_count", proteinCount + "");
        prop.setProperty("peptide_count", peptideCount + "");

        try {
            try (FileWriter writer = new FileWriter(dbInfoPropertiesPath)) {
                prop.store(writer, "Created " + MyFormatter.format(LocalDateTime.now()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
