package hr.createnr;

import java.io.IOException;
import java.sql.SQLException;

public class GlobalMain {
	
	
	// mozda jos dodati SET GLOBAL time_zone = '+3:00';
	public static final String DB_URL = "jdbc:mysql://localhost/createnr?useSSL=false&autoReconnect=true&serverTimezone=UTC";
	public static final String DB_USER = "root";
	public static final String DB_PASSWORD = "ja";

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {

		if (args.length <= 1) {
			System.out.println("Not call args!");
			PrepareAccessionTaxIdCSV_APP.printArgs();
			PrepareNr_APP.printArgs();
			Statistic_App.printArgs();
			return;
		}

		String argAppName = args[0];
		if (argAppName.equalsIgnoreCase(PrepareAccessionTaxIdCSV_APP.ARG_APP_NAME)) {

			System.out.println("preprare accession taxid csv");

			PrepareAccessionTaxIdCSV_APP.main(args);
			System.out.println("Finish");
			return;
		}
		if (argAppName.equalsIgnoreCase(PrepareNr_APP.ARG_APP_NAME)) {
			System.out.println("nr prepare");
			PrepareNr_APP.main(args);
			System.out.println("Finish");
			return;
		}
		
		if (argAppName.equalsIgnoreCase(Statistic_App.ARG_APP_NAME)) {
			System.out.println("stat");
			Statistic_App.main(args);
			System.out.println("Finish");
			return;
		}

		System.out.println("Not find app...");

	}
}
