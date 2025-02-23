package hr.pbf.digestdb.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CustomAccessionDb {

    private String fromCsvPath = "";
    private String toDbPath = "";

    public static void main(String[] args) throws IOException {
        CustomAccessionDb db = new CustomAccessionDb();
        db.fromCsvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/accession_map_sorted.csv";
        StopWatch watch = StopWatch.createStarted();
        List<String> map = db.startReadCsvToMap();
        watch.stop();
        MyUtil.printMiliSec(watch, "Read time");
    }

    public void startCreateCustomAccessionDb() throws IOException {
        List<String> map = startReadCsvToMap();
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(toDbPath)));

        out.writeInt(map.size());
        for (String acc : map) {
            out.writeUTF(acc);
        }
        out.close();
    }

    public List<String> startReadCsvToMap() throws IOException {
        BufferedReader reader = IOUtils.toBufferedReader(new FileReader(fromCsvPath));
        String line;
        ArrayList<String> accMap = new ArrayList<>(100_000);
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            //int accNum = Integer.parseInt(parts[0]);
            String acc = parts[1];
            //accMap.put(accNum, acc);
            accMap.add(acc);
        }
        reader.close();
        return accMap;


    }
}
