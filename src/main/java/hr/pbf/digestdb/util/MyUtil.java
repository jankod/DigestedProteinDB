package hr.pbf.digestdb.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.rocksdb.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MyUtil {
    private static final DecimalFormat df4 = new DecimalFormat("#.####", DecimalFormatSymbols.getInstance(Locale.US));

    public static double roundTo4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    public static double roundTo5(double value) {
        return Math.round(value * 100000.0) / 100000.0;
    }


    public static String discretizedTo4(double mass) {
        return df4.format(mass);
    }

    public static byte[] floatToByteArray(float floatValue) {
        return ByteBuffer.allocate(4).putFloat(floatValue).array();
    }

    public static float byteArrayToFloat(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).getFloat();
    }


    public static byte[] doubleToByteArray(double mass) {
        return ByteBuffer.allocate(Double.BYTES).putDouble(mass).rewind().array();
    }

    public static double byteArrayToDouble(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).getDouble();
    }


    /**
     * Optimized for point read
     *
     * @param dbPath
     * @return
     * @throws RocksDBException
     */
    public static RocksDB openPointReadDB(String dbPath) throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options();
        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
        Cache cache = new LRUCache(32L * 1024L * 1024L * 1024L);
        tableConfig.setBlockCache(cache);
        tableConfig.setCacheIndexAndFilterBlocks(true);
        tableConfig.setPinL0FilterAndIndexBlocksInCache(true);

        //  Optimization for point read
        options.optimizeForPointLookup(32 * 1024 * 1024);

        options.setTableFormatConfig(tableConfig);
        options.setAllowMmapReads(true);


        options.setCompressionType(CompressionType.ZLIB_COMPRESSION);
        return RocksDB.openReadOnly(options, dbPath);
    }

    public static RocksDB openReadDB(String dbPath) throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options();
        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
        Cache cache = new LRUCache(64L * 1024L * 1024L * 1024L); // 64GB cache
        tableConfig.setBlockCache(cache);
        tableConfig.setCacheIndexAndFilterBlocks(true);
        tableConfig.setPinL0FilterAndIndexBlocksInCache(true);

        // Optimization for point read
        // options.optimizeForPointLookup(32  * 1024 * 1024); // 512 MB block cache

        options.setTableFormatConfig(tableConfig);
        options.setAllowMmapReads(true);

        options.setCompressionType(CompressionType.ZLIB_COMPRESSION);
        return RocksDB.openReadOnly(options, dbPath);
    }

    public static RocksDB openWriteDB(String path) throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);

        options.setStrictBytesPerSync(false);
        options.setAllow2pc(false);
        options.setAllowMmapReads(true);
        options.setAllowMmapWrites(true);


        options.setCompactionStyle(CompactionStyle.LEVEL);
        options.setWriteBufferSize(256 * 1024 * 1024); // 256 MB
        options.setCompressionType(CompressionType.ZLIB_COMPRESSION);

        return RocksDB.open(options, path);

    }

    public static byte[] intToByteArray(int someInt) {
        return ByteBuffer.allocate(4).putInt(someInt).array();
    }

    public static int byteArrayToInt(byte[] key) {
        return ByteBuffer.wrap(key).getInt();
    }

    public static List<byte[]> intListToByteList(List<Integer> accs) {
        ArrayList<byte[]> list = new ArrayList<>(accs.size());
        for (Integer acc : accs) {
            list.add(intToByteArray(acc));
        }
        return list;
    }

    public static String stopAndShowTime(StopWatch watch, String msg) {
        watch.stop();
        long nanoTime = watch.getNanoTime();
        long millis = TimeUnit.NANOSECONDS.toMillis(nanoTime);
        return (msg + " " + DurationFormatUtils.formatDuration(millis, "HH:mm:ss,SSS"));

    }

    public static String getFileSize(String filePath) {
        long size = FileUtils.sizeOf(new File(filePath));
        return FileUtils.byteCountToDisplaySize(size);
    }

    public static String getDirSize(String dirPath) {
        long size = FileUtils.sizeOfDirectory(new File(dirPath));
        return FileUtils.byteCountToDisplaySize(size);
    }

}
