package hr.pbf.digestdb.db;

import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.ValidatateUtil;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * Read sorted CSV file with accNum, acc and create binary db file.
 * CSV is sorted by accNum like this:
 * <pre>
 *               1,B8IPW1
 *               2,A7IJ80
 *               3,A8INZ3
 *               4,A1WLI5
 *               5,Q1IQ54
 *               6,Q3AW77
 *               </pre>
 */
public class AccessionDbCreator {

    private final String fromCsvPath;

    private final String toDbPath;

    /**
     * @param fromCsvPath CSV
     * @param toDbPath    DB path on disk
     */
    public AccessionDbCreator(String fromCsvPath, String toDbPath) {
        this.fromCsvPath = fromCsvPath;
        this.toDbPath = toDbPath;

        ValidatateUtil.fileMustExist(fromCsvPath);
        ValidatateUtil.fileMustNotExist(toDbPath);
    }

    public void startCreate() throws IOException {
        List<String> accList = readCsvToList();
        writeBinaryDb(accList);
    }

    private void writeBinaryDb(List<String> accList) throws IOException {
        {
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(toDbPath)))) {
                out.writeInt(accList.size());
                for (String acc : accList) {
                    byte[] accBytes = acc.getBytes(StandardCharsets.UTF_8);
                    BinaryPeptideDbUtil.writeVarInt(out, accBytes.length);
                    out.write(accBytes);
                }
            }
        }
    }

    public List<String> readCsvToList() throws IOException {
        ArrayList<String> accMap;
        try (BufferedReader reader = IOUtils.toBufferedReader(new FileReader(fromCsvPath))) {
            String line;
            accMap = new ArrayList<>(100_000);
            accMap.add("0"); // 0 index is empty
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int accNum = Integer.parseInt(parts[0]);
                if (accNum != accMap.size()) {
                    throw new RuntimeException("Acc num is not in order. Expected: " + accMap.size() + " but got: " + accNum);
                }
                String acc = parts[1];
                accMap.add(acc);
            }
        }
        return accMap;


    }
}
