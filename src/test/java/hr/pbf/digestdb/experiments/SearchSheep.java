package hr.pbf.digestdb.experiments;

import hr.pbf.digestdb.db.AccessionDbReader;
import hr.pbf.digestdb.db.MassRocksDbReader;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.util.*;

@Slf4j
public class SearchSheep {

    public static void main(String[] args) throws RocksDBException {
        MassRocksDbReader db = new MassRocksDbReader("/home/tag/IdeaProjects/DigestedProteinDB/misc/db/sheep/rocksdb_mass.db");
        db.open();

        AccessionDbReader accessionDbReader = new AccessionDbReader("/home/tag/IdeaProjects/DigestedProteinDB/misc/db/sheep/custom_accession.db");


        List<Double> sheepMasses = getMassesSheep("/media/tag/D/digested-db/Trypsin_HTXdigest-ovca.txt");
        log.debug("Sheep masses: " + sheepMasses.size());
        ProteinScorer scorer = new ProteinScorer();


        for (Double mass : sheepMasses) {
            double mass1 = mass - 0.02;
            double mass2 = mass + 0.02;
            List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> peptides = db.searchByMassPaginated(mass1, mass2);
            //log.debug("Mass: " + mass + " found peptides: " + peptides.size());
            //  System.out.println("Mass: " + mass + " found peptides: " + peptides.size());


            for (Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>> entry : peptides) {
                for (BinaryPeptideDbUtil.PeptideAccids peptideAccids : entry.getValue()) {
                    String peptide = peptideAccids.getSeq();
                    int[] proteins = peptideAccids.getAccids();
                    Set<String> accessions = new HashSet<>(proteins.length);
                    for (int protIdInt : proteins) {
                        String acc = accessionDbReader.getAccession(protIdInt);
                        if (acc == null || acc.isEmpty()) {
                            log.warn("Protein accession is null or empty for ID: " + protIdInt);
                            continue;
                        }
                        accessions.add(acc);
                        // System.out.println("Protein: " + acc + ", Peptide: " + peptide);
                    }
                    scorer.addPeptide(peptide, accessions);
                }
            }

        }

        // Get ranked proteins
        List<Map.Entry<String, Integer>> ranked = scorer.getRankedProteins();
        for (Map.Entry<String, Integer> entry : ranked) {
            System.out.println(entry.getKey() + ", unique peptides: " + entry.getValue());
        }

    }

    private static List<Double> getMassesSheep(String path) {
        try (BufferedReader reader = java.nio.file.Files.newBufferedReader(java.nio.file.Paths.get(path))) {
            return reader.lines()
                  .map(line -> line.split("\t")[0])
                  .map(Double::parseDouble)
                  .toList();
        } catch (Exception e) {
            throw new RuntimeException("Error reading masses from file: " + path, e);
        }
    }
}

class ProteinScorer {

    // Map: protein accession -> set of unique peptides found in sample
    private final Map<String, Set<String>> proteinToPeptides = new HashMap<>();

    // Call this for each peptide found in your sample
    public void addPeptide(String peptide, Set<String> proteinAccessions) {
        for (String protein : proteinAccessions) {
            proteinToPeptides.computeIfAbsent(protein, k -> new HashSet<>()).add(peptide);
        }
    }

    // Returns a map: protein accession -> number of unique peptides found
    public Map<String, Integer> getProteinScores() {
        Map<String, Integer> scores = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : proteinToPeptides.entrySet()) {
            scores.put(entry.getKey(), entry.getValue().size());
        }
        return scores;
    }

    // Returns proteins sorted by number of unique peptides (descending)
    public List<Map.Entry<String, Integer>> getRankedProteins() {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(getProteinScores().entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return list;
    }
}
