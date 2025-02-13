package hr.pbf.digestdb.rocksdb;

import hr.pbf.digestdb.rocksdb.SwisprotXMLParser.ProteinHandler;
import hr.pbf.digestdb.util.BioUtil;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class MainSwisprotToCsv {

    public static void main(String[] args) throws RocksDBException, IOException {
        String swisprotPath = "/Users/tag/DevProject/blast-ncbi/uniprot_sprot.xml";
        String csvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/csv/peptide_mass.csv";

        SwisprotXMLParser parser = new SwisprotXMLParser();
        FastBufferedOutputStream out = new FastBufferedOutputStream(new FileOutputStream(csvPath), 8 * 1024 * 4);

//        RocksDbMass massDb = new RocksDbMass("/Users/tag/IdeaProjects/DigestedProteinDB/misc/nr-db/peptide_mass.db");
//        massDb.openDB();
        int counter = 0;

        try {
            parser.parseProteinsFromXMLstream(swisprotPath, new ProteinHandler() {
                @Override
                public void gotProtein(ProteinInfo p) {
//                    if (counter > 10000) {
//                        stopped = true;
//                        return;
//                    }

                    BioUtil.tripsyn(p.getSequence(), 5, 30).forEach(peptide -> {
                        if(peptide.contains("X") || peptide.contains("Z") || peptide.contains("B")) {
                            return;
                        }
                        double mass =  BioUtil.calculateMassWidthH2O(peptide);
                        String row = addToCsv(p, mass, peptide);
                        try {
                            out.write(row.getBytes(StandardCharsets.US_ASCII));
                            counter++;

                            if (counter % 10_000 == 0) {
                                log.debug("Counter: {}", counter);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    });

//                    massDb.saveToDatabase(p, 5, 30);
                }
            });
        } finally {
            log.debug("Counter finish: {}", counter);
            out.close();
//            massDb.closeDB();
        }


    }

    private static String addToCsv(ProteinInfo p, double mass, String peptide) {
        return mass + "," + peptide + "," + p.getAccession() + "\n";
    }
}
