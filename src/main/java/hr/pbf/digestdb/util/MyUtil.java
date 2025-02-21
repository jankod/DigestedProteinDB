package hr.pbf.digestdb.util;

import com.google.common.primitives.Longs;
import org.rocksdb.*;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MyUtil {
    public static double roundToFour(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    public static double roundToFive(double value) {
        return Math.round(value * 100000.0) / 100000.0;
    }

    private static final DecimalFormat df = new DecimalFormat("#.####", DecimalFormatSymbols.getInstance(Locale.US)); // Format za 4 decimale

    public static String discretizedToFour(double mass) {
        return df.format(mass);
    }

    public static byte[] floatToByteArray(float floatValue) {
        return ByteBuffer.allocate(4).putFloat(floatValue).array();
    }

    public static float byteArrayToFloat(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).getFloat();
    }


    public static RocksDB openDB(String path) throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        options.setCompressionType(CompressionType.ZLIB_COMPRESSION);
        return RocksDB.open(options, path);

    }

    public static byte[] doubleToByteArray(double mass) {
        return ByteBuffer.allocate(Double.BYTES).putDouble(mass).rewind().array();
    }

    public static double byteArrayToDouble(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).getDouble();
    }


    public static RocksDB openReadDB(String dbPath) throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCompressionType(CompressionType.ZLIB_COMPRESSION);
        return RocksDB.openReadOnly(options, dbPath);
    }

    public static long byteArrayToLong(byte[] accessionValue) {
        return Longs.fromByteArray(accessionValue);
    }

    public static byte[] longToByteArray(Long longNum) {
        return Longs.toByteArray(longNum);
    }
}
