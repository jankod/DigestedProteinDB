package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.util.MyUtil;
import hr.pbf.digestdb.util.UniprotXMLParser;
import hr.pbf.digestdb.util.UniprotXMLParser.ProteinHandler;
import hr.pbf.digestdb.util.BioUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * Create CSV file with peptide mass, peptide sequence, protein accession [and taxonomy id].
 * Read uniprot xml file and create csv file with peptide mass, peptide sequence, protein accession and taxonomy id.
 * https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/proteomics_mapping/README
 */
@Data
@Slf4j
public class MainUniprotToPeptideCsv {

    //public static final String PEPTIDE_MASS_CSV = "peptide_mass.csv";
    //public static final String PEPTIDE_MASS_CSV_SORTED = "peptide_mass_acc_sorted.csv";
    // result
    public String resultPeptideMassAccCsvPath = "";

    // params
//    private final String dbDir;
    public String fromSwisprotPath = "";
    public long maxProteinCount = Long.MAX_VALUE - 1;
    public int minPeptideLength = 0;
    public int maxPeptideLength = 0;
    public int missClevage = 1;

//    public MainUniprotToPeptideCsv(String dbDir) throws IOException {
//        this.dbDir = dbDir;
//        if (!FileUtils.isDirectory(new File(dbDir))) {
//            throw new IllegalArgumentException("Not a directory: " + dbDir);
//        }
//        FileUtils.forceMkdir(new File(dbDir + "/gen"));
//        this.resultPeptideMassAccCsvPath = dbDir + "/gen/peptide_mass.csv";
//    }

    public void start() throws IOException {
        if (!new File(fromSwisprotPath).exists()) {
            throw new RuntimeException("File not found: " + fromSwisprotPath);
        }

        if (new File(resultPeptideMassAccCsvPath).exists()) {
            throw new RuntimeException("File already exists: " + resultPeptideMassAccCsvPath);
        }

        if (missClevage != 1) {
            throw new RuntimeException("Miss clevage must be 1");
        }
        if (minPeptideLength < 1 || minPeptideLength > maxPeptideLength) {
            throw new RuntimeException("minPeptideLength must be > 0 and minPeptideLength < maxPeptideLength. minPeptideLength: "
                                       + minPeptideLength + " maxPeptideLength: " + maxPeptideLength);
        }

        UniprotXMLParser parser = new UniprotXMLParser();

        Charset standardCharset = StandardCharsets.UTF_8;
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(resultPeptideMassAccCsvPath), 8 * 1024 * 16)) {

            parser.parseProteinsFromXMLstream(fromSwisprotPath, new ProteinHandler() {
                @Override
                public void gotProtein(UniprotXMLParser.ProteinInfo p) {
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
                        double mass4 = MyUtil.roundTo4(mass);
                        String row = mass4 + "," + peptide + "," + p.getAccession() + "\n";
                        try {
                            out.write(row.getBytes(standardCharset));
                            counter++;

                            if (counter % 5_000_000 == 0) {
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

        log.info("Finish uniprot to csv: " + resultPeptideMassAccCsvPath);


    }

}
