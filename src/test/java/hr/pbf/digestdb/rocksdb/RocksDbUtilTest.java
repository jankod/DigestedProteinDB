package hr.pbf.digestdb.rocksdb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RocksDbUtilTest {


    @Test
    void doubleToByteArrayAndBack() {
        byte[] bytes = RocksDbUtil.doubleToByteArray(123.45634567d);

        double v = RocksDbUtil.byteArrayToDouble(bytes);
        assertEquals(123.45634567d, v);

    }

}
