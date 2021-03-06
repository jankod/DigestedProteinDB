package hr.pbf.digestdb;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import hr.pbf.digestdb.uniprot.sprot.A10_UniprotFormat1toCSV;
import hr.pbf.digestdb.uniprot.sprot.UngroupCsv;
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
import hr.pbf.digestdb.uniprot.A8_UniprotLevelDBtoIndex;
import hr.pbf.digestdb.uniprot.A9_CountLongestValueInCSVColumn;
import hr.pbf.digestdb.uniprot.A_X2_UniprotCompressSmallFilesLevelDb;
import hr.pbf.digestdb.uniprot.UniprotSearchFormat2;
import hr.pbf.digestdb.util.TimeScheduler;
import hr.pbf.digestdb.util.UniprotConfig;
import hr.pbf.digestdb.util.UniprotConfig.Name;

public class GlobalMain {

    public static void main(String[] args) {
        StopWatch s = new StopWatch();
        s.start();
        try {
            // App_11_MakeLevelDbStore.main(args);
            // App_12_CreateTaxIDAccessionDB.main(args);
            // App_11_MakeLevelDbStore_TEST.main(args);
            // App_14_MaveMVstoreAccessionTaxid.main(args);
            // App_15_AddTaxIdToCSV.main(args);

            if (argsFirstElementContain("format1_to_csv_a10", args)) {
                A10_UniprotFormat1toCSV.main(argsMinusFirstElement(args));
                return;
            }

            if (argsFirstElementContain("uniprot-a1", args)) {
                A1_UniprotToFormat1.main(argsMinusFirstElement(args));
                return;
            }

            if (argsFirstElementContain("uniprot-a3", args)) {
                A3_UniprotFormat2ToOther.main(argsMinusFirstElement(args));
                // A1_UniprotToFormat1.main(args);
                // A_X2_UniprotCompressSmallFilesLevelDb.main(args);
                // A2_UniprotToFormat2.main(args);
                // UniprotSearchFormat2.main(args);
                return;
            }
            if (argsFirstElementContain("uniprot-csvtoleveldb", args)) {
                A4_UniprotCsvToLevelDB.main(argsMinusFirstElement(args));
                return;
            }

            if (argsFirstElementContain("uniprot-search", args)) {
                A4_UniprotCsvToLevelDB.searchMain(argsMinusFirstElement(args));
                return;
            }

            if (argsFirstElementContain("uniprot-createindex", args)) {
                A4_UniprotCsvToLevelDB.createIndexfromLeveldb(UniprotConfig.get(Name.PATH_TREMB_LEVELDB));
                return;
            }

            if (argsFirstElementContain("uniprot-statistic", args)) {
                A6_UniprotStatistic.main(argsMinusFirstElement(args));
                return;
            }
            if (argsFirstElementContain("uniprot-mvstore-proteins", args)) {
                A7_UniprotProtNamesToLevelDB.main(argsMinusFirstElement(args));
                return;
            }
            if (argsFirstElementContain("uniprot-make-index-csv", args)) {
                A8_UniprotLevelDBtoIndex.makeIndexDefaultPath();
                //A7_UniprotProtNamesToLevelDB.main(argsMinusFirstElement(args));
                return;
            }

            if (argsFirstElementContain("ungroup-csv", args)) {
                UngroupCsv.main(args);
                return;

            }
            
            if(argsFirstElementContain("count-longest", args)) {
            	A9_CountLongestValueInCSVColumn c = new A9_CountLongestValueInCSVColumn();
            	c.start();
            	return;
            }

             System.out.println("Not find parameter?");
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            s.stop();
            System.out.println("Finish " + formatDurationHMS(s.getTime()));
            TimeScheduler.stopAll();
            System.out.println("TOTAL finish");
        }
    }

    public static boolean argsFirstElementContain(String search, String[] args) {
        if (args.length >= 1) {
            if (search.equals(args[0].trim())) {
                return true;
            }
        }
        return false;
    }

    public static String[] argsMinusFirstElement(String[] args) {
        ArrayList<String> s = new ArrayList<>(Arrays.asList(args));
        s.remove(0);
        return s.toArray(new String[args.length - 1]);
    }

}
