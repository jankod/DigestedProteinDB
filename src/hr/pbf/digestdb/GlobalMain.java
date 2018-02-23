package hr.pbf.digestdb;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import hr.pbf.digestdb.app.App_15_AddTaxIdToCSV;
import hr.pbf.digestdb.uniprot.A1_UniprotTextParser;
import hr.pbf.digestdb.uniprot.A2_UniprotDeltaMassReader;
import hr.pbf.digestdb.util.TimeScheduler;
import hr.pbf.digestdb.app.App_11_MakeLevelDbStore;
import hr.pbf.digestdb.app.App_11_MakeLevelDbStore_TEST;
import hr.pbf.digestdb.app.App_12_CreateTaxIDAccessionDB;
import hr.pbf.digestdb.app.App_14_MaveMVstoreAccessionTaxid;

public class GlobalMain {

	public static void main(String[] args) {
		System.out.println("Start");
		StopWatch s = new StopWatch();
		s.start();
		try {
			// App_11_MakeLevelDbStore.main(args);
			//App_12_CreateTaxIDAccessionDB.main(args);
			// App_11_MakeLevelDbStore_TEST.main(args);
			// App_14_MaveMVstoreAccessionTaxid.main(args);
			//App_15_AddTaxIdToCSV.main(args);
			//A1_UniprotTextParser.main(args);
			A2_UniprotDeltaMassReader.main(args);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			s.stop();
			System.out.println("Finish " + formatDurationHMS(s.getTime()));
			TimeScheduler.stopAll();
		}
	}

}
