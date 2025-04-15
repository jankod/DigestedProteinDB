package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import hr.pbf.digestdb.exception.ValidationException;
import hr.pbf.digestdb.model.Enzyme;
import hr.pbf.digestdb.model.TaxonomyDivision;
import hr.pbf.digestdb.util.*;
import hr.pbf.digestdb.util.UniprotXMLParser.ProteinHandler;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Create CSV file with peptide mass, peptide sequence, protein accession [and taxonomy id].
 * Read uniprot XML file and create csv file with peptide mass, peptide sequence, protein accession and taxonomy id.
 */
@Data
@Slf4j
public class JobUniprotToPeptideCsv {

    public String resultPeptideMassAccCsvPath = "";

    public String resultTaxAccCsvPath = "";

    public String fromSwisprotPath = "";

    public long maxProteinCount = Long.MAX_VALUE - 1;
    public int minPeptideLength = 0;
    public int maxPeptideLength = 0;
    public int missedClevage = 1;
    private Enzyme enzyme;

    public int[] taxonomyParentsIds;
    public String ncbiTaxonomyPath = null;

    private TaxonomyDivision taxonomyDivision = TaxonomyDivision.ALL;

    @Data
    public static class Result {
        long proteinCount;
        long peptideCount;

    }

    public Result start() throws IOException {
        ValidatateUtil.fileMustExist(fromSwisprotPath);
        ValidatateUtil.fileMustNotExist(resultPeptideMassAccCsvPath);

        if (missedClevage != 1) {
            throw new ValidationException("Miss clevage must be 1 curently.");
        }
        if (minPeptideLength < 1 || minPeptideLength > maxPeptideLength) {
            throw new ValidationException("minPeptideLength must be > 0 and minPeptideLength < maxPeptideLength. minPeptideLength: "
                    + minPeptideLength + " maxPeptideLength: " + maxPeptideLength);
        }
        NcbiTaksonomy ncbiTaksonomy = null;
        if (ncbiTaxonomyPath != null) {
            ValidatateUtil.fileMustExist(ncbiTaxonomyPath);
            try {
                ncbiTaksonomy = NcbiTaksonomy.loadTaxonomy(ncbiTaxonomyPath);
            } catch (NcbiTaxonomyException e) {
                throw new RuntimeException(e);
            }

            if (taxonomyParentsIds == null) {
                throw new ValidationException("Taxonomy parent IDs should not be empty if NCBI taxonomy is used.");
            }
        }


        UniprotXMLParser parser = new UniprotXMLParser();

        LongCounter proteinCount = new LongCounter();
        LongCounter peptideCount = new LongCounter();

        Charset standardCharset = StandardCharsets.UTF_8;
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(resultPeptideMassAccCsvPath), 8 * 1024 * 16);
             BufferedOutputStream outTaxonomy = new BufferedOutputStream(new FileOutputStream(resultTaxAccCsvPath), 4 * 1024 * 16)) {

            NcbiTaksonomy finalNcbiTaksonomy = ncbiTaksonomy;
            parser.parseProteinsFromXMLstream(fromSwisprotPath, new ProteinHandler() {
                @Override
                public void gotProtein(UniprotXMLParser.ProteinInfo p) {
                    if (counter > maxProteinCount) {
                        stopped = true;
                        log.info("Finish read proteins, max protein count reached: {}", maxProteinCount);
                        return;
                    }
                    if (taxonomyDivision != TaxonomyDivision.ALL) {
                        if (p.getDivisionId() != taxonomyDivision.getId()) {
                            return;
                        }
                    }
                    if (finalNcbiTaksonomy != null) {
                        boolean hasParent = false;
                        for (int parentsId : taxonomyParentsIds) {
                            if (finalNcbiTaksonomy.isAncestor(p.getTaxonomyId(), parentsId)) {
                                hasParent = true;
                                break;
                            }
                        }
                        if (!hasParent) {
                            return;
                        }
                    }


                    proteinCount.increment();
                    saveTaxonomy(p.getAccession(), p.getTaxonomyId(), outTaxonomy);

                    List<String> peptides;
                    peptides = enzyme.cleavage(p.getSequence(), missedClevage, minPeptideLength, maxPeptideLength);

//					if(enzyme == CreateDatabase.CreateDatabaseConfig.Enzyme.Trypsin) {
//						peptides = BioUtil.tripsyn1mc(p.getSequence(), minPeptideLength, maxPeptideLength);
//					} else {
//						peptides = BioUtil.chymotrypsin1(p.getSequence(), minPeptideLength, maxPeptideLength);
//					}

                    peptides.forEach(peptide -> {
                        if (peptide.contains("X") || peptide.contains("Z") || peptide.contains("B")) {
                            return;
                        }
                        peptideCount.increment();
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

        }

        Result result = new Result();
        result.setPeptideCount(peptideCount.get());
        result.setProteinCount(proteinCount.get());

        return result;
    }

    @SneakyThrows
    private void saveTaxonomy(String accession, int taxonomyId, BufferedOutputStream outTaxonomy) {
        outTaxonomy.write((accession + "," + taxonomyId + "\n").getBytes(StandardCharsets.UTF_8));
    }

}
