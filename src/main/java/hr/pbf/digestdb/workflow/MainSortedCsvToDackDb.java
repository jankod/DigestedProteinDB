package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.util.MyUtil;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class MainSortedCsvToDackDb {

    public String fromCsvPath = "";
    public String toDbPath = "";


    public void startInsertToDackDb() throws IOException, RocksDBException {

        try (RocksDB db = MyUtil.openWriteDB(toDbPath)) {

            String csvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/csv/peptide_mass_sorted_orig.csv";
            try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(Path.of(csvPath)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("mass")) {
                        continue;
                    }
                    String[] split = line.split(",");

//                    mass,sequence,accession,taxonomy_id
//331.14917543984376,GGGAA,A1ITY1,122587
//331.14917543984376,GGGAA,Q9K153,122586

                    double mass = Double.parseDouble(split[0]);
                    String sequence = split[1];
                    String accession = split[2];
                    int taxonomyId = Integer.parseInt(split[3]);
                }
            }

        }


    }







    public static void main(String[] args) {
        double p1 = 345.1648309574219;
        double p2 = 345.1648309574219;
        double p3 = 347.144109521875;


        System.out.println(MyUtil.roundTo4(p1));
        System.out.println(MyUtil.roundTo4(p2));
        System.out.println(MyUtil.roundTo4(p3));

    }


}
