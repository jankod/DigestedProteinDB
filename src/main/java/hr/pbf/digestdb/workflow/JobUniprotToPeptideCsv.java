package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import hr.pbf.digestdb.exception.ValidationException;
import hr.pbf.digestdb.model.Enzyme;
import hr.pbf.digestdb.model.TaxonomyDivision;
import hr.pbf.digestdb.util.*;
import hr.pbf.digestdb.util.UniprotXMLParser.ProteinHandler;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;

/**
 * Create CSV file with peptide mass, peptide sequence, protein accession [and taxonomy id].
 * Read uniprot XML file and create csv file with peptide mass, peptide sequence, protein accession and taxonomy id.
 */
@Data
@Slf4j
public class JobUniprotToPeptideCsv {

    public String resultPeptideMassAccCsvPath = "";

    public String resultAccTaxCsvPath = "";

    public String fromSwisprotPath = "";
    public String ncbiTaxonomyPath = null;


    public long maxProteinCount = Long.MAX_VALUE - 1;
    public int minPeptideLength = 0;
    public int maxPeptideLength = 0;
    public int missedClevage = 1;
    private Enzyme enzyme;

    public int[] taxonomyParentsIds;

    private TaxonomyDivision taxonomyDivision = TaxonomyDivision.ALL;

    @Data
    public static class Result {
        long proteinCount;
        long peptideCount;
    }

    public Result start() throws Exception {
        ValidatateUtil.fileMustExist(fromSwisprotPath);
        ValidatateUtil.fileMustNotExist(resultPeptideMassAccCsvPath);

        if (missedClevage != 1 && missedClevage != 2) {
            throw new ValidationException("Miss clevage must be 1 or 2 curently.");
        }
        if (minPeptideLength < 1 || minPeptideLength > maxPeptideLength) {
            throw new ValidationException("minPeptideLength must be > 0 and minPeptideLength < maxPeptideLength. minPeptideLength: "
                                          + minPeptideLength + " maxPeptideLength: " + maxPeptideLength);
        }
        NcbiTaksonomyRelations ncbiTaksonomyRelations = null;
        if (ncbiTaxonomyPath != null) {
            ValidatateUtil.fileMustExist(ncbiTaxonomyPath);
            try {
                ncbiTaksonomyRelations = NcbiTaksonomyRelations.loadTaxonomyNodes(ncbiTaxonomyPath);

            } catch (NcbiTaxonomyException e) {
                throw new RuntimeException(e);
            }

            if (taxonomyParentsIds == null) {
                throw new ValidationException("Taxonomy parent IDs should not be empty if NCBI taxonomy is used.");
            }
        }

        UniprotXMLParser parser = new UniprotXMLParser();


        LongCounter proteinCounterParent = new LongCounter();

        /*
         * Non-unique peptide count, so it can be more than protein count.
         */
        LongCounter peptideCount = new LongCounter();

        Int2IntOpenHashMap taxIdPeptideCount = new Int2IntOpenHashMap();

        Charset standardCharset = StandardCharsets.UTF_8;
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(resultPeptideMassAccCsvPath), 8 * 1024 * 16);
             BufferedOutputStream outTaxonomy = new BufferedOutputStream(new FileOutputStream(resultAccTaxCsvPath), 4 * 1024 * 16)) {

            NcbiTaksonomyRelations finalNcbiTaksonomyRelations = ncbiTaksonomyRelations;
            parser.parseProteinsFromXMLstream(fromSwisprotPath, new ProteinHandler() {
                @Override
                public void gotProtein(UniprotXMLParser.ProteinInfo p) {

                    if (super.stopped) {
                        return;
                    }
                    if (super.proteinCounter > maxProteinCount) {
                        stopped = true;
                        log.info("Finish read proteins, max protein count reached: {}", maxProteinCount);
                        return;
                    }
                    if (taxonomyDivision != TaxonomyDivision.ALL) {
                        if (p.getDivisionId() != taxonomyDivision.getId()) {
                            return;
                        }
                    }

                    if (finalNcbiTaksonomyRelations != null) {
                        boolean hasParent = false;

                        for (int parentsId : taxonomyParentsIds) {
                            int taxId = p.getTaxonomyId();
                            if (finalNcbiTaksonomyRelations.isAncestor(taxId, parentsId)) {
                                hasParent = true;
                                break;
                            }
                        }
                        if (!hasParent) {
                            return;
                        }
                    }
//                    writeToDebug(p);

                    //proteinCount.increment();
                    saveTaxonomy(p.getAccession(), p.getTaxonomyId(), outTaxonomy);

                    List<String> peptides;
                    peptides = enzyme.cleavage(p.getSequence(), missedClevage, minPeptideLength, maxPeptideLength);

                    if (proteinCounter % 100_000_000 == 0) {
                        log.debug("Current protein count: {}", NumberFormat.getInstance().format(proteinCounter));
                    }
                    proteinCounterParent.increment();

                    peptides.forEach(peptide -> {
                        if (peptide.contains("X") || peptide.contains("Z") || peptide.contains("B")) {
                            return;
                        }
                        taxIdPeptideCount.addTo(p.getTaxonomyId(), 1);

                        peptideCount.increment();
                        double mass = BioUtil.calculateMassWidthH2O(peptide);
                        double mass4 = MyUtil.roundTo4(mass);
                        String row = mass4 + "," + peptide + "," + p.getAccession() + "\n";
                        try {
                            out.write(row.getBytes(standardCharset));


                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            });
        }


        //saveTaxIdPeptideCount(taxIdPeptideCount);

        Result result = new Result();
        result.setPeptideCount(peptideCount.get());
        result.setProteinCount(proteinCounterParent.get());

        return result;
    }


    /**
     * Statistics: save taxonomy ID and peptide count to CSV file.
     *
     * @param taxIdPeptideCount
     * @throws IOException
     */
    private void saveTaxIdPeptideCount(Int2IntOpenHashMap taxIdPeptideCount) throws IOException {
        // replace path resultAccTaxCsvPath only last file name with "taxId_peptide_count.csv" with FileUtils class
        String taxIdPeptideCountPath = FileUtils.getFile(resultAccTaxCsvPath).
                                             getParentFile().getAbsolutePath() + "/taxId_peptide_count.csv";

        boolean fistLine = true;
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(taxIdPeptideCountPath), StandardCharsets.UTF_8))) {
            for (Int2IntMap.Entry entry : taxIdPeptideCount.int2IntEntrySet()) {
                if (fistLine) {
                    writer.write("Taxonomy ID,Peptide Count\n");
                    fistLine = false;
                } else {
                    writer.write('\n');
                }

                writer.write(String.valueOf(entry.getIntKey()));
                writer.write(',');
                writer.write(String.valueOf(entry.getIntValue()));

            }
            log.info("Taxonomy ID => peptide count saved to: {}", taxIdPeptideCountPath);
        } catch (IOException e) {
            log.error("Error saving taxonomy ID peptide count to file: {}", taxIdPeptideCountPath, e);
            throw e;
        }
    }

    private void writeToDebug(UniprotXMLParser.ProteinInfo p) {
        int taxonomyId = p.getTaxonomyId();
        System.out.println(taxonomyId + " " + p.getAccession() + " " + p.getDivisionId());
    }

    @SneakyThrows
    private void saveTaxonomy(String accession, int taxonomyId, BufferedOutputStream outTaxonomy) {
        outTaxonomy.write((accession + "," + taxonomyId + "\n").getBytes(StandardCharsets.UTF_8));
    }
}
