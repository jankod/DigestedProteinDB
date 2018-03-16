package hr.pbf.digestdb;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import hr.pbf.digestdb.nr.App_11_MakeLevelDbStore;
import hr.pbf.digestdb.nr.App_11_MakeLevelDbStore_TEST;
import hr.pbf.digestdb.nr.App_12_CreateTaxIDAccessionDB;
import hr.pbf.digestdb.nr.App_14_MaveMVstoreAccessionTaxid;
import hr.pbf.digestdb.nr.App_15_AddTaxIdToCSV;
import hr.pbf.digestdb.uniprot.A1_UniprotToFormat1;
import hr.pbf.digestdb.uniprot.A2_UniprotToFormat2;
import hr.pbf.digestdb.uniprot.A3_UniprotFormat2ToOther;
import hr.pbf.digestdb.uniprot.A4_UniprotCsvToLevelDB;
import hr.pbf.digestdb.uniprot.A6_UniprotStatistic;
import hr.pbf.digestdb.uniprot.A7_UniprotProtNamesToLevelDB;
import hr.pbf.digestdb.uniprot.A_X2_UniprotCompressSmallFilesLevelDb;
import hr.pbf.digestdb.uniprot.UniprotSearchFormat2;
import hr.pbf.digestdb.util.TimeScheduler;

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
			
			if(MyUtil.argsFirstElementContain("uniprot-a3", args)) {
				A3_UniprotFormat2ToOther.main(MyUtil.argsMinusFirstElement(args));				
				//A1_UniprotToFormat1.main(args);
				//A_X2_UniprotCompressSmallFilesLevelDb.main(args);
				//A2_UniprotToFormat2.main(args);
				//UniprotSearchFormat2.main(args);
				return;
			}
			if(MyUtil.argsFirstElementContain("uniprot-csvtoleveldb", args)) {
				A4_UniprotCsvToLevelDB.main(MyUtil.argsMinusFirstElement(args));
				return;
			}
			
			if(MyUtil.argsFirstElementContain("uniprot-search", args)) {
				A4_UniprotCsvToLevelDB.searchMain(MyUtil.argsMinusFirstElement(args));
				return;
			}
			
			if(MyUtil.argsFirstElementContain("uniprot-createindex", args)) {
				A4_UniprotCsvToLevelDB.createIndexfromLeveldb();
				return;
			}
			
			if(MyUtil.argsFirstElementContain("uniprot-statistic", args)) {
				A6_UniprotStatistic.main(MyUtil.argsMinusFirstElement(args));
				return;
			}
			if(MyUtil.argsFirstElementContain("uniprot-mvstore-proteins", args)) {
				A7_UniprotProtNamesToLevelDB.main(MyUtil.argsMinusFirstElement(args));
				return;
			}
			
			
//			System.out.println("Not find parameter?");
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			s.stop();
			System.out.println("Finish " + formatDurationHMS(s.getTime()));
			TimeScheduler.stopAll();
			System.out.println("TOTAL finish");
		}
	}

}
