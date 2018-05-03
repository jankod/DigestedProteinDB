package hr.pbf.digestdb.test.experiments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import hr.pbf.digestdb.web.WebListener;

public class MysqlPopulateExperiment {
	private static final Logger log = LoggerFactory.getLogger(MysqlPopulateExperiment.class);
	private static DB db;

	
	// LOAD DATA CONCURRENT LOCAL INFILE 'F:\\ProteinReader\\UniprotDBfiles\\trembl_for_test.csv' INTO TABLE `proba`.`peptides` CHARACTER SET 'ascii' FIELDS ESCAPED BY '\\' TERMINATED BY '\t' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' (`mass`, `peptide`, `acc_tax`); 
	public static void main(String[] args) throws ManagedProcessException, IOException {

	//	db = DB.newEmbeddedDB(3306);
		DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
		configBuilder.setPort(3306); // OR, default: setPort(0); => autom. detect free port
		configBuilder.setDataDir("F:\\ProteinReader\\UniprotDBfiles\\mariadb-10.2.14-winx64\\data2"); // just an example
		configBuilder.setBaseDir("F:\\ProteinReader\\UniprotDBfiles\\mariadb-10.2.14-winx64");
		db = DB.newEmbeddedDB(configBuilder.build());
	
		db.start();

		
		listenLine();
	}

	private static void listenLine() throws IOException {
		log.debug("Maria db listen from console: s i r");
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("reading...");
		String line = r.readLine();
		while (line != null) {
			if ("s".equals(line.trim())) {
				System.out.println("STOP");
				try {
					db.stop();
					System.out.println("Stoped je...");
					r.close();
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			line = r.readLine();
		}
	}
}
