package hr.pbf.digestdb.experiments;

import hr.pbf.digestdb.db.AccessionDbReader;
import hr.pbf.digestdb.db.MassRocksDbReader;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


@Slf4j
public class PeptideMS1Search {
    private final MassRocksDbReader db;
    private final AccessionDbReader accessionDbReader;
    private final double mass1;
    private final double mass2;
    private final PTM[] ptms;

    public PeptideMS1Search(MassRocksDbReader db, AccessionDbReader accessionDbReader, double mass1, double mass2, PTM... ptms) {
        this.db = db;
        this.accessionDbReader = accessionDbReader;
        this.mass1 = mass1;
        this.mass2 = mass2;
        this.ptms = ptms;

        if (db == null) {
            throw new RuntimeException("Database is not initialized.");
        }
        if (mass1 <= 0 || mass2 <= 0) {
            throw new RuntimeException("Masses must be positive.");
        }
        if (mass1 > mass2) {
            throw new RuntimeException("Mass1 must be less than or equal to Mass2.");
        }
    }

    public List<PtmSearchResult> search() {

        List<PtmSearchResult> ptmSearchResults = new ArrayList<>();
        for (PTM ptm : ptms) {
            double deltaMass = ptm.getDeltaMass();
            double adjustedMass1 = mass1 - deltaMass;
            double adjustedMass2 = mass2 - deltaMass;
            List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> peptides = db.searchByMassPaginated(adjustedMass1, adjustedMass2);

            for (Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>> entry : peptides) {

                Double mass = entry.getKey();
                for (BinaryPeptideDbUtil.PeptideAccids peptideAccids : entry.getValue()) {
                    String peptide = peptideAccids.getSeq();
                    int[] accIds = peptideAccids.getAccids();
                    Set<String> accessions = new HashSet<>(accIds.length);
                    for (int protIdInt : accIds) {
                        String acc = accessionDbReader.getAccession(protIdInt);
                        if (acc == null || acc.isEmpty()) {
                            log.warn("Protein accession is null or empty for ID: " + protIdInt);
                            continue;
                        }
                        accessions.add(acc);
                    }
                    PtmSearchResult res = new PtmSearchResult(
                          peptide,
                          mass,
                          new PTM[]{ptm},
                          accessions
                    );
                    ptmSearchResults.add(res);
                }
            }
        }

        return ptmSearchResults;
    }

    @SneakyThrows
    public void saveToFile(String filePath, List<PtmSearchResult> results) {
        log.info("Saving results to file: " + filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Peptide\tMass\tPTMs\tAccessions");
            for (PtmSearchResult result : results) {
                String line = String.format("%s\t%.4f\t%s\t%s",
                        result.getPeptide(),
                        result.getMass(),
                        Arrays.toString(result.getPtms()),
                        String.join(",", result.getAccessions()));
                writer.write(line);
                writer.newLine();
            }
        } catch (Exception e) {
            log.error("Error writing to file: " + filePath, e);
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class PtmSearchResult {
        private final String peptide;
        private final double mass;
        private final PTM[] ptms;
        private final Set<String> accessions;
    }
}
