package hr.pbf.digestdb.rocksdb;

import java.nio.ByteBuffer;

public class RocksUtil {
    public static byte[] floatToByteArray(float floatValue) {
        return ByteBuffer.allocate(4).putFloat(floatValue).array();
    }

    public static float byteArrayToFloat(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).getFloat();
    }
}
