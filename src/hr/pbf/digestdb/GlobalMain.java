package hr.pbf.digestdb;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import hr.pbf.digestdb.app.App_10_AddTaxIdToCSV;
import hr.pbf.digestdb.app.App_11_MakeLevelDbStore;

public class GlobalMain {

	public static void main(String[] args) {
		System.out.println("Start");
		StopWatch s = new StopWatch();
		s.start();
		try {
			//App_10_AddTaxIdToCSV.main(args);
			App_11_MakeLevelDbStore.main(args);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		s.stop();
		System.out.println("Finish " + formatDurationHMS(s.getTime()));

	}

}
