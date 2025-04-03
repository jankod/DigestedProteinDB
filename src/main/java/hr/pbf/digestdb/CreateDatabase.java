package hr.pbf.digestdb;

import hr.pbf.digestdb.db.AccessionDbCreator;
import hr.pbf.digestdb.db.MassRocksDbCreator;
import hr.pbf.digestdb.util.*;
import hr.pbf.digestdb.workflow.*;
import hr.pbf.digestdb.workflow.BashCommand;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class CreateDatabase {

	public static final String DEFAULT_ROCKSDB_MASS_DB_FILE_NAME = "rocksdb_mass.db";
	public static final String DEFAULT_DB_FILE_NAME = "custom_accession.db";
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
		Enzyme enzyme = Enzyme.Trypsin;

		public enum Enzyme {
			Trypsin,
			Chymotrypsin,
//            LysC,
//            GluC,
//            AspN,
//            ArgC,
//            LysN
		}
	}

	public CreateDatabase(CreateDatabaseConfig config) {
		this.config = config;
	}

	public void start() throws Throwable {

		List<Integer> steps = List.of(1, 2, 3, 4, 5, 6, 7);

		steps = List.of( 3, 4, 5, 6, 7);

		StopWatch watch = StopWatch.createStarted();
		final String DB_DIR_PATH = config.dbDir;

		final String PEPTIDE_MASS_CSV_PATH = DB_DIR_PATH + "/gen/peptide_mass.csv";
		final String PEPTIDE_MASS_CSV_SORTED_PATH = DB_DIR_PATH + "/gen/peptide_mass_sorted.csv";
		final String GROUPED_WITH_IDS_CSV_PATH = DB_DIR_PATH + "/gen/grouped_with_ids.csv";
		final String ACCESSION_MAP_CSV_PATH = DB_DIR_PATH + "/gen/accession_map.csv";
		final String ACCESSION_MAP_CSV_SORTED_PATH = DB_DIR_PATH + "/gen/accession_map.csv.sorted";
		final String CUSTOM_ACCESSION_DB_DIR_PATH = DB_DIR_PATH + "/" + DEFAULT_DB_FILE_NAME;
		final String ROCKDB_DB_DIR_PATH = DB_DIR_PATH + "/" + DEFAULT_ROCKSDB_MASS_DB_FILE_NAME;
		final String TAX_ACC_CSV_PATH = DB_DIR_PATH + "/gen/tax_acc.csv";
		final String DB_INFO_PROPERTIES_PATH = DB_DIR_PATH + "/db_info.properties";

		File genDir = new File(DB_DIR_PATH + "/gen");

		// if args contain --clean
		if(config.cleanBefore) {
			log.info("Clean all files in dir: {}", DB_DIR_PATH);
			FileUtils.deleteQuietly(genDir);
			FileUtils.deleteQuietly(new File(ROCKDB_DB_DIR_PATH));
			FileUtils.deleteQuietly(new File(CUSTOM_ACCESSION_DB_DIR_PATH));
			FileUtils.deleteQuietly(new File(DB_INFO_PROPERTIES_PATH));
		}

		log.info("Start create DB, params  {}!", config);

		if(!genDir.isDirectory())
			FileUtils.forceMkdir(genDir);

		JobUniprotToPeptideCsv.Result readXmlResult;
		if(steps.contains(1)) {            // 1. Uniprot xml to CSV
			JobUniprotToPeptideCsv app1UniprotToCsv = new JobUniprotToPeptideCsv();
			app1UniprotToCsv.minPeptideLength = config.getMinPeptideLength();
			app1UniprotToCsv.maxPeptideLength = config.getMaxPeptideLength();
			app1UniprotToCsv.missedClevage = config.getMissedCleavage();
			app1UniprotToCsv.fromSwisprotPath = config.getUniprotXmlPath();
			app1UniprotToCsv.setEnzyme(config.getEnzyme());
			app1UniprotToCsv.setResultPeptideMassAccCsvPath(PEPTIDE_MASS_CSV_PATH);
			app1UniprotToCsv.setResultTaxAccCsvPath(TAX_ACC_CSV_PATH);

			readXmlResult = app1UniprotToCsv.start();
			log.info("Uniprot extracted. Protein count: {}, Peptide count: {}", readXmlResult.getProteinCount(), readXmlResult.getPeptideCount());
		} else {
			log.info("Skip step 1");
			readXmlResult = new JobUniprotToPeptideCsv.Result();
		}

		if(steps.contains(2)) {
			// 2. export TMPDIR=.../temp_dir
			//    sort -t',' -k1n peptide_mass.csv -o peptide_mass_sorted_console.csv
			BashCommand app2CmdSortMass = new BashCommand();

			String sortTempDir = config.getSortTempDir();
			String cmdString = "";

			if(sortTempDir != null) {
				if(!FileUtils.isDirectory(new File(sortTempDir))) {
					log.info("Temp dir not exists, i will create.: {}", sortTempDir);
					FileUtils.forceMkdir(new File(sortTempDir));
				}
				cmdString += " export TMPDIR=${sort_temp_dir} && ";
			}
			cmdString += "sort -S 8G -t ',' -k1n ${peptide_mass_csv_path} -o ${peptide_mass_sorted_console}";

			cmdString = new MyFormatter(cmdString)
					.param("sort_temp_dir", sortTempDir)
					.param("peptide_mass_csv_path", PEPTIDE_MASS_CSV_PATH)
					.param("peptide_mass_sorted_console", PEPTIDE_MASS_CSV_SORTED_PATH)
					.format();

			log.debug("Execute command: {} in dir {}", cmdString, genDir);
			app2CmdSortMass.setCmd(cmdString);
			app2CmdSortMass.setDir(genDir);
			//jobLancher.run(app2CmdSortMass);
			app2CmdSortMass.start();
		} else {
			log.info("Skip step 2");
		}

		// create accession map
		{

		}


		long proteinCountResult = 0;
		if(steps.contains(3)) { // 3.
			JobCsvMassGrouperWithAccIds app3csvMassGroup = new JobCsvMassGrouperWithAccIds();
			app3csvMassGroup.setInputCsvPeptideMassSorted(PEPTIDE_MASS_CSV_SORTED_PATH);
			app3csvMassGroup.setOutputGroupedCsv(GROUPED_WITH_IDS_CSV_PATH);
			app3csvMassGroup.setOutputAccessionMapCsv(ACCESSION_MAP_CSV_PATH);

			Object2IntMap<String> accCustomDb = app3csvMassGroup.start();
			proteinCountResult = accCustomDb.size();
			log.info("Grouped with ids: {}", GROUPED_WITH_IDS_CSV_PATH);
		} else {
			log.info("Skip step 3");
		}

		if(steps.contains(4)) { // 4.
			//  sort -t',' -k1n accession_map.csv -o accession_map_sorted.csv
			BashCommand cmd = new BashCommand();
			String cmdSortAccession = " sort -t',' -k1n ${accession_map.csv}  -o ${accession_map.csv.sorted} ";
			cmdSortAccession = new MyFormatter(cmdSortAccession)
					.param("accession_map.csv", ACCESSION_MAP_CSV_PATH)
					.param("accession_map.csv.sorted", ACCESSION_MAP_CSV_SORTED_PATH)
					.format();

			log.debug("Execute command: {} in dir {}", cmdSortAccession, genDir);
			cmd.setCmd(cmdSortAccession);
			cmd.setDir(genDir);
			cmd.start();
			log.info("Accession map sorted: {}", ACCESSION_MAP_CSV_SORTED_PATH);
		} else {
			log.info("Skip step 4");
		}

		if(steps.contains(5)) { // 5. Create rocksdb mass
			//  MassRocksDb app4createMassRocksDb = new MassRocksDb();
			MassRocksDbCreator massDb = new MassRocksDbCreator(GROUPED_WITH_IDS_CSV_PATH, ROCKDB_DB_DIR_PATH);

			massDb.startCreate();
			log.info("RockDB db is created: {}", ROCKDB_DB_DIR_PATH);
		} else {
			log.info("Skip step 5");
		}

		if(steps.contains(6)) { // 6. Create custom accession db
			AccessionDbCreator accDb = new AccessionDbCreator(ACCESSION_MAP_CSV_SORTED_PATH, CUSTOM_ACCESSION_DB_DIR_PATH);
			accDb.startCreate();
			log.info("Custom accession db is created: {}", CUSTOM_ACCESSION_DB_DIR_PATH);
		} else {
			log.info("Skip step 6");
		}

		if(steps.contains(7)) { // 7. Properties
			saveDbInfoToProperties(readXmlResult.getProteinCount(), DB_INFO_PROPERTIES_PATH, readXmlResult.getPeptideCount());
		} else {
			log.info("Skip step 7");
		}

		log.info("Finished time: {}", DurationFormatUtils.formatDurationHMS(watch.getTime()));
	}

	private void saveDbInfoToProperties(long proteinCount, String dbInfoPropertiesPath, long peptideCount) {

		Properties prop = new Properties();
		//      prop.setProperty("uniprot_xml_path", config.getUniprotXmlPath());
		prop.setProperty("min_peptide_length", String.valueOf(config.getMinPeptideLength()));
		prop.setProperty("max_peptide_length", String.valueOf(config.getMaxPeptideLength()));
		prop.setProperty("miss_cleavage", String.valueOf(config.getMissedCleavage()));
		prop.setProperty("db_name", config.getDbName());
		prop.setProperty("enzyme_name", config.getEnzyme().name());
		prop.setProperty("protein_count", proteinCount + "");
		prop.setProperty("peptide_count", peptideCount + "");

		try {
			try(FileWriter writer = new FileWriter(dbInfoPropertiesPath)) {
				prop.store(writer, "Created " + MyFormatter.format(LocalDateTime.now()));
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

}
