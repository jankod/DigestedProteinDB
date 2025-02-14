package hr.pbf.digestdb.rocksdb;

import org.rocksdb.CompressionType;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.nio.ByteBuffer;

public class RocksDbUtil {
    public static byte[] floatToByteArray(float floatValue) {
        return ByteBuffer.allocate(4).putFloat(floatValue).array();
    }

    public static float byteArrayToFloat(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).getFloat();
    }

     public static RocksDB openDB(String path) throws RocksDBException {
        Options options = new Options().setCreateIfMissing(true);

        // Postavljanje tipa kompresije
        options.setCompressionType(CompressionType.ZLIB_COMPRESSION);
        try {
            return RocksDB.open(options, path);


        } catch (RocksDBException e) {
            System.err.println("Error opening RocksDB: " + e.getMessage());
            throw e; // Ponovno bacite iznimku da se obradi više razine
        }
    }

    public static byte[] doubleToByteArray(double mass) {
        return ByteBuffer.allocate(Double.BYTES).putDouble(mass).rewind().array();
    }
    public static double byteArrayToDouble(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).getDouble();
    }


}
