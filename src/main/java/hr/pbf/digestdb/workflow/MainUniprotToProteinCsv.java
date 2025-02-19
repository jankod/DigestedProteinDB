package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.workflow.UniprotXMLParser.ProteinHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * Converts a Uniprot XML file to a CSV file containing protein sequences, accessions, and taxonomy IDs.
 */
@Slf4j
public class MainUniprotToProteinCsv {
    public String fromSwisprotPath = "";

    public String toCsvPath = "";
    public int maxProteinCount = Integer.MAX_VALUE - 1;


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
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(toCsvPath), 8 * 1024 * 24)) {
            //   out.write(getCsvHeader().getBytes(standardCharset));

            parser.parseProteinsFromXMLstream(fromSwisprotPath, new ProteinHandler() {
                @Override
                public void gotProtein(ProteinInfo p) throws IOException {
                    if (counter > maxProteinCount) {
                        stopped = true;
                        log.info("Max protein count reached: {}", maxProteinCount);
                        return;
                    }

                    String csvRow = getCsvRow(p);
                    out.write(csvRow.getBytes(standardCharset));
                    counter++;

                    if (counter % 1_000_000 == 0) {
                        log.debug("Current protein count: {}", counter);
                    }


                }
            });


        } finally {
            log.debug("Finish, protein count: {}", parser.getTotalCount());
        }

        log.info("Finish uniprot to csv: " + toCsvPath);


    }

    private String getCsvHeader() {
        return "#protein,accession,taxonomyId\n";
    }


    private String getCsvRow(ProteinInfo p) {
        return p.getSequence() + "," + p.getAccession() + "," + p.taxonomyId + "\n";
    }
}
