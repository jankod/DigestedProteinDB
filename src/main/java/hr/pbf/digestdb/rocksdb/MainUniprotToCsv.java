package hr.pbf.digestdb.rocksdb;

import hr.pbf.digestdb.rocksdb.UniprotXMLParser.ProteinHandler;
import hr.pbf.digestdb.util.BioUtil;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class MainUniprotToCsv {

    public static void main(String[] args) throws RocksDBException, IOException {
        String swisprotPath = "/Users/tag/DevProject/blast-ncbi/uniprot_sprot.xml";
        String csvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/csv/peptide_mass.csv";
        final int maxCount = Integer.MAX_VALUE - 1;


        UniprotXMLParser parser = new UniprotXMLParser();

        Charset standardCharset = StandardCharsets.UTF_8;

        try (FastBufferedOutputStream out = new FastBufferedOutputStream(new FileOutputStream(csvPath), 8 * 1024 * 4)) {
         //   out.write(getCsvHeader().getBytes(standardCharset));

            parser.parseProteinsFromXMLstream(swisprotPath, new ProteinHandler() {
                @Override
                public void gotProtein(ProteinInfo p) {
                    if (counter > maxCount) {
                        stopped = true;
                        log.info("Max count reached: {}", maxCount);
                        return;
                    }

                    BioUtil.tripsyn(p.getSequence(), 5, 30).forEach(peptide -> {
                        if (peptide.contains("X") || peptide.contains("Z") || peptide.contains("B")) {
                            return;
                        }
                        double mass = BioUtil.calculateMassWidthH2O(peptide);
                        String row = getCsvRow(p, mass, peptide);
                        try {
                            out.write(row.getBytes(standardCharset));
                            counter++;

                            if (counter % 100_000 == 0) {
                                log.debug("Protein count: {}", counter);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    });
                }
            });


        } finally {
            log.debug("Finish, protein count: {}", parser.getTotalCount());
        }


    }

    private static String getCsvHeader() {
        return "#mass,peptide,accession,taxonomyId\n";
    }


    private static String getCsvRow(ProteinInfo p, double mass, String peptide) {
        return mass + "," + peptide + "," + p.getAccession() + "," + p.taxonomyId + "\n";
    }
}
