package hr.pbf.digestdb.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RocksDbUtilTest {


    @Test
    void doubleToByteArrayAndBack() {
        byte[] bytes = MyUtil.doubleToByteArray(123.45634567d);

        double v = MyUtil.byteArrayToDouble(bytes);
        assertEquals(123.45634567d, v);

    }

}
