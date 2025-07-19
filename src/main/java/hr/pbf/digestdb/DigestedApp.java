package hr.pbf.digestdb;

import hr.pbf.digestdb.model.TaxonomyDivision;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Slf4j
public class DigestedApp {

	@Command(
			name = "digestdb",
			description = "Protein digestion database tool",
			subcommands = { CreateDbCommand.class, WebServerCommand.class })
	static class RootCommand implements Callable<Integer> {
		@Override
		public Integer call() {
			// Show help if no subcommand provided
			new CommandLine(this).usage(System.out);
			return 0;
		}
	}

	@Command(name = "create-db", description = "Create digestion database")
	static class CreateDbCommand implements Callable<Integer> {
		@Option(names = { "-d", "--db-dir" }, description = "Path to the database directory (for creader or readed)", required = true)
		String dbDir;

		@Option(names = { "-u", "--uniprot-xml" }, description = "Path to the uniprot xml file (.xml.gz or .xml)", required = true)
		String uniprotXmlPath;

		@Option(names = { "-n", "--db-name" }, description = "Name of the database", required = true)
		String dbName;

		@Option(names = { "-c", "--clean" }, description = "Clean all files in database directory before creating")
		boolean cleanDb = false;

		@Option(names = { "-m", "--min-length" }, description = "Min peptide length, default 7", defaultValue = "7")
		int minPeptideLength = 7;

		@Option(names = { "-M", "--max-length" }, description = "Max peptide length, default 30", defaultValue = "30")
		int maxPeptideLength = 30;

		@Option(names = { "-mc", "--missed-cleavage" }, description = "Missed cleavage, default 1 (currently only option)", defaultValue = "1")
		int missedCleavage = 1;

		@Option(
				names = { "-s", "--sort-temp-dir" }, description = "Path to the temporary directory, used " +
																   "for sorting on linux sort command (if needed) on diferent disk")
		String sortTempDir;

		@Option(names = { "-ncbi-nodes", "--ncbi-nodes-taxonomy-path" }, description = "Path to the NCBI taxonomy file (nodes.dmp)")
		String ncbiTaxonomyPath = null;

		@Option(names = { "-p", "--taxonomy-parents" }, description = "Taxonomy parents (ancestors) ids, default all. Use -p taxID1, taxID2 ... for filtering by taxonomy.", split = ",")
		int[] taxonomyParentsIds = null;

		@Option(names = { "-e", "--enzyme" }, description = "Enzyme used for digestion, default trypsin", defaultValue = "Trypsin")
		CreateDatabase.CreateDatabaseConfig.SupportedEnzime enzyme = CreateDatabase.CreateDatabaseConfig.SupportedEnzime.Trypsin;

		@Option(
				names = { "-t", "--taxonomy-division" },
				description = "If not set, all proteins will be used. Set taxonomy division to use only specific proteins.", defaultValue = "ALL")
		TaxonomyDivision taxonomyDivision;

		@Override
		public Integer call() throws Exception {
			System.out.println("Start creating database in dir: " + dbDir);

			CreateDatabase.CreateDatabaseConfig config = new CreateDatabase.CreateDatabaseConfig();
			config.setDbDir(dbDir);
			config.setCleanBefore(cleanDb);
			config.setMinPeptideLength(minPeptideLength);
			config.setMaxPeptideLength(maxPeptideLength);
			config.setMissedCleavage(missedCleavage);
			config.setUniprotXmlPath(uniprotXmlPath);
			config.setSortTempDir(sortTempDir);
			config.setDbName(dbName);
			config.setEnzymeType(enzyme);
			config.setTaxonomyDivision(taxonomyDivision);
			config.setTaxonomyParentsIds(taxonomyParentsIds);
			config.setNcbiTaxonomyPath(ncbiTaxonomyPath);

			if (taxonomyParentsIds != null && taxonomyParentsIds.length > 0 && ncbiTaxonomyPath == null) {
				throw new IllegalArgumentException("If -p option is set, -ncbi option must be set too.");
			}

			CreateDatabase createDatabase = new CreateDatabase(config);
			createDatabase.start();

			return 0;
		}
	}

	@Command(name = "server", description = "Run web server for database search")
	static class WebServerCommand implements Callable<Integer> {
		@Option(names = { "-p", "--port" }, description = "Port of web app, default 7071", defaultValue = "7071")
		int port = 7071;

		@Option(names = { "-d", "--db-dir" }, description = "Path to the database directory", required = true)
		String dbDir;

		@Override
		public Integer call() throws Exception {
			System.out.println("Starting web server on port " + port + " with database: " + dbDir);

			SearchWeb searchWeb = new SearchWeb(dbDir, port);
			searchWeb.start();

			return 0;
		}
	}

	public static void main(String[] args) {
		try {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				log.info("Application is shutting down, possibly due to SIGINT");
			}));

			int exitCode = new CommandLine(new RootCommand()).execute(args);
			log.info("Fininsh, exiting with code {}", exitCode);
			System.exit(exitCode);
		} catch(Throwable e) {
			log.error("Error creating 	database", e);
			e.printStackTrace();
		}

	}
}
