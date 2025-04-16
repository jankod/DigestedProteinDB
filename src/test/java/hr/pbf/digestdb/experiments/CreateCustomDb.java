package hr.pbf.digestdb.experiments;

import hr.pbf.digestdb.db.MassRocksDbCreator;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.MyUtil;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class CreateCustomDb {

    @SneakyThrows
    public static void main(String[] args) {


        Int2LongRBTreeMap massPositions = new Int2LongRBTreeMap();

        String massPositionsPath = "/Users/tag/RustroverProjects/demoapp/mass_positions.bin";

        RandomAccessFile raf = new RandomAccessFile("/Users/tag/RustroverProjects/demoapp/seq_data.bin", "rw");


        String fromCsvPath = "/Users/tag/RustroverProjects/demoapp/grouped_with_ids.csv";

        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(Path.of(fromCsvPath)))) {

            String line;

            while ((line = reader.readLine()) != null) {
                // 503.234,SGAGAAA:15-SAAGGAA:14-TGAAAGG:16
                String[] parts = line.split(",", 2);

                double mass = Double.parseDouble(parts[0]);
                int massInt = (int) Math.round(mass * 10_000);
                String seqAccs = parts[1];

                byte[] seqAccsBytes = BinaryPeptideDbUtil.writeGroupedRow(seqAccs);
                //byte[] massIntBytes = MyUtil.intToByteArray(massInt);

                long position = raf.length();
                raf.seek(position);
                raf.write(seqAccsBytes);
                massPositions.put(massInt, position);

            }
        }
        IOUtils.closeQuietly(raf);

        writeMassPositionsToFile(massPositions, massPositionsPath);

        log.debug("Mass positions: {}", massPositions.size());
    }

    private static void writeMassPositionsToFile(Int2LongRBTreeMap massPositions, String massPositionsPath) {
        // write massPositions to file
        try (RandomAccessFile raf = new RandomAccessFile(massPositionsPath, "rw")) {
            raf.writeInt(massPositions.size());
            for (Int2LongMap.Entry entry : massPositions.int2LongEntrySet()) {
                int mass = entry.getIntKey();
                long position = entry.getLongValue();
                raf.writeInt(mass);
                raf.writeLong(position);
            }
        } catch (IOException e) {
            log.error("Error writing mass positions to file: {}", e.getMessage());
        }
    }
}
