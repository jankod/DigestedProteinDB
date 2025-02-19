package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.workflow.UniprotXMLParser.ProteinHandler;
import hr.pbf.digestdb.util.BioUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
//https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/proteomics_mapping/README
public class MainUniprotToPeptideCsv {
    public String fromSwisprotPath = "";

    public String toCsvPath = "";
    public int maxProteinCount = Integer.MAX_VALUE - 1;

    public int minPeptideLength = 7; // 7
    public int maxPeptideLength = 30;

    public void start() throws IOException {

        UniprotXMLParser parser = new UniprotXMLParser();

        Charset standardCharset = StandardCharsets.UTF_8;

        if (!new File(fromSwisprotPath).exists()) {
            throw new RuntimeException("File not found: " + fromSwisprotPath);
        }
        if (new File(toCsvPath).exists()) {
            throw new RuntimeException("File already exists: " + toCsvPath);
        }

        // Default DEFAULT_MAX_BUFFER_SIZE = 8192
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(toCsvPath), 8 * 1024 * 8)) {
            //   out.write(getCsvHeader().getBytes(standardCharset));

            parser.parseProteinsFromXMLstream(fromSwisprotPath, new ProteinHandler() {
                @Override
                public void gotProtein(ProteinInfo p) {
                    if (counter > maxProteinCount) {
                        stopped = true;
                        log.info("Max protein count reached: {}", maxProteinCount);
                        return;
                    }

                    BioUtil.tripsyn(p.getSequence(), minPeptideLength, maxPeptideLength).forEach(peptide -> {
                        if (peptide.contains("X") || peptide.contains("Z") || peptide.contains("B")) {
                            return;
                        }
                        double mass = BioUtil.calculateMassWidthH2O(peptide);
                        String row = getCsvRow(p, mass, peptide);
                        try {
                            out.write(row.getBytes(standardCharset));
                            counter++;

                            if (counter % 1_000_000 == 0) {
                                log.debug("Current protein count: {}", counter);
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

        log.info("Finish uniprot to csv: " + toCsvPath);


    }

    private String getCsvHeader() {
        return "#mass,peptide,accession,taxonomyId\n";
    }


    private String getCsvRow(ProteinInfo p, double mass, String peptide) {
        return mass + "," + peptide + "," + p.getAccession() + "," + p.taxonomyId + "\n";
    }
}
