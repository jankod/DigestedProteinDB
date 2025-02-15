package hr.pbf.digestdb;

import hr.pbf.digestdb.rocksdb.MainUniprotToCsv;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;

import java.io.IOException;

@Slf4j
public class App {
    public static void main(String[] args) throws Throwable {

        log.info("DigestDB app");


        MainUniprotToCsv app = new MainUniprotToCsv();
        app.main(args);

    }
}
