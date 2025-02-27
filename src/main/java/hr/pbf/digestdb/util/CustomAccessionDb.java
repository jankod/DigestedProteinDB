package hr.pbf.digestdb.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * Create custom binary accession db from csv file. Csv file must be sorted by accession number (accNum, acc).
 */
@Data
@Slf4j
public class CustomAccessionDb {

    public static final String CUSTOM_ACCESSION_DB_FILE_NAME = "custom_accession.db";
    //public static final String DEFAULT_DB_DIR_NAME = "custom_accession.db";
    private String fromCsvPath = "";
    private String toDbPath = "";

    private String[] accList;

//    public CustomAccessionDb(String dbDir) {
//        if (dbDir == null) {
//            throw new IllegalArgumentException("dbDir is null");
//        }
//        if (!new File(dbDir).isDirectory()) {
//            throw new IllegalArgumentException("Not a directory: " + dbDir);
//        }
//        this.toDbPath = dbDir + "/custom_accession.db";
//        this.fromCsvPath = dbDir + "/gen/accession_map_sorted.csv";
//    }

    public static void main(String[] args) throws IOException {
        CustomAccessionDb db = new CustomAccessionDb();

        boolean create = false;

        if (!create) {
            StopWatch watch = StopWatch.createStarted();
            String[] accList = db.loadDb();
            MyUtil.stopAndShowTime(watch, "Read time");
            log.info("First acc: {}", accList[1]);
            log.info("Last acc: {}", accList[accList.length - 1]);
        }

        if (create) {
            StopWatch watch = StopWatch.createStarted();
            db.startCreateCustomAccessionDb();
            MyUtil.stopAndShowTime(watch, "Write DB time");
        }

    }

    public String getAcc(int index) {
        if (accList == null) {
            throw new RuntimeException("Acc list is not loaded");
        }
        if (index < 0 || index >= accList.length) {
            throw new RuntimeException("Acc list, index out of bounds: " + index);
        }
        return accList[index];
    }

    public void startCreateCustomAccessionDb() throws IOException {
        List<String> map = startReadCsvToMap();

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(toDbPath)))) {
            out.writeInt(map.size());
            for (String acc : map) {
                byte[] accBytes = acc.getBytes(StandardCharsets.UTF_8);
                BinaryPeptideDbUtil.writeVarInt(out, accBytes.length);
                out.write(accBytes);
            }
        }
    }

    public String[] loadDb() {
        if (!new File(toDbPath).exists()) {
            throw new RuntimeException("File not found: " + toDbPath);
        }
        String[] accList;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(toDbPath)))) {
            int size = in.readInt();
            accList = new String[size];
            accList[0] = "0"; // 0 index is empty
            for (int i = 0; i < size; i++) {
                int len = BinaryPeptideDbUtil.readVarInt(in);
                byte[] accBytes = new byte[len];
                in.readFully(accBytes);
                String acc = new String(accBytes, StandardCharsets.UTF_8);
            /*    if(i == 1) {
                    log.debug("First acc: {}", acc);
                }
                if (i == 2) {
                    log.debug("2 acc: {}", acc);
                }*/
                accList[i] = acc;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.accList = accList;

        return accList;
    }

    public List<String> startReadCsvToMap() throws IOException {
        BufferedReader reader = IOUtils.toBufferedReader(new FileReader(fromCsvPath));
        String line;
        ArrayList<String> accMap = new ArrayList<>(100_000);
        accMap.add("0"); // 0 index is empty
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            int accNum = Integer.parseInt(parts[0]);
            if (accNum != accMap.size()) {
                throw new RuntimeException("Acc num is not in order. Expected: " + accMap.size() + " but got: " + accNum);
            }
//            if (accNum == 1) {
//                log.debug("First acc 1: {}", parts[1]);
//            }
            String acc = parts[1];
            accMap.add(acc);
        }
        reader.close();
        return accMap;


    }
}
