package hr.pbf.digestdb;

import java.io.IOException;
import java.sql.SQLException;

import hr.pbf.digestdb.app.App_1_PrepareNr;
import hr.pbf.digestdb.cli.MainCli;

/**
 * {@link Deprecated} Use {@link MainCli}
 * 
 * @author tag
 *
 */
@Deprecated()
public class GlobalMainOld {

	// mozda jos dodati SET GLOBAL time_zone = '+3:00';
	/**
	 * @depreced, uzeti {@link AppConstants}
	 * 
	 */
	//public static final String DB_URL = "jdbc:mysql://localhost/createnr?useSSL=false&autoReconnect=true&serverTimezone=UTC";
	//public static final String DB_USER = "root";
	//public static final String DB_PASSWORD = "ja";

	public static void main2(String[] args) throws IOException, SQLException, InterruptedException {

		if (args.length <= 1) {
			System.out.println("Not call args!");
			App_2_1_PrepareAccessionTaxIdCSV.printArgs();
			App_1_PrepareNr.printArgs();
			App_Statistic.printArgs();
			return;
		}

		String argAppName = args[0];
		if (argAppName.equalsIgnoreCase(App_2_1_PrepareAccessionTaxIdCSV.ARG_APP_NAME)) {

			System.out.println("preprare accession taxid csv");

			App_2_1_PrepareAccessionTaxIdCSV.main(args);
			System.out.println("Finish");
			return;
		}
		if (argAppName.equalsIgnoreCase(App_1_PrepareNr.ARG_APP_NAME)) {
			System.out.println("nr prepare");
			App_1_PrepareNr.main(args);
			System.out.println("Finish");
			return;
		}

		if (argAppName.equalsIgnoreCase(App_Statistic.ARG_APP_NAME)) {
			System.out.println("stat");
			App_Statistic.main(args);
			System.out.println("Finish");
			return;
		}

		System.out.println("Not find compressApp...");

	}
}
