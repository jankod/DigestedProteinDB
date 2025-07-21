package hr.pbf.digestdb.experiments;

import hr.pbf.digestdb.db.AccessionDbReader;
import hr.pbf.digestdb.db.MassRocksDbReader;
import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import hr.pbf.digestdb.util.AccTaxDB;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.NcbiTaksonomyRelations;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.util.*;

@Slf4j
public class SearchSheep {

    public static void main(String[] args) throws RocksDBException, NcbiTaxonomyException {
        String dbDir = "/home/tag/IdeaProjects/DigestedProteinDB/misc/db/sheep2/";
        dbDir = "/Users/tag/PBF radovi/digestedproteindb/sheep/rocksdb_mass.db"; // For testing on local machine
        MassRocksDbReader db = new MassRocksDbReader(dbDir + "rocksdb_mass.db");
        db.open();

        AccessionDbReader accessionDbReader = new AccessionDbReader(dbDir + "custom_accession.db");


        String pathSheep = "/media/tag/D/digested-db/Trypsin_HTXdigest-ovca.txt";
        pathSheep = "'/Users/tag/PBF radovi/digestedproteindb'/Trypsin_HTXdigest_sheep_butorka.txt";
        List<Double> sheepMasses = getMassesSheep(pathSheep);
        log.debug("Sheep masses: " + sheepMasses.size());
        ProteinScorer scorer = new ProteinScorer();


        for (Double mass : sheepMasses) {
            double mass1 = mass - 0.02;
            double mass2 = mass + 0.02;

            PeptideMS1Search peptideMS1Search = new PeptideMS1Search(db, accessionDbReader, mass1, mass2, PTM.CARBAMIDOMETHYL, PTM.OXIDATION, PTM.DEAMIDATION, PTM.PHOSPHORYLATION);
            List<PeptideMS1Search.PtmSearchResult> res = peptideMS1Search.search();
            String filePath = "/Users/tag/peptides_sheep.csv";
            peptideMS1Search.saveToFile(filePath, res);
            System.out.println("Save to file: " + filePath);
            if(true) return; // For testing, remove this line to process all masses


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
            //  System.out.println(entry.getKey() + ", unique peptides: " + entry.getValue());
        }

        NcbiTaksonomyRelations taxonomy = NcbiTaksonomyRelations.loadTaxonomyNodes("/home/tag/IdeaProjects/DigestedProteinDB/misc/ncbi/taxdump/nodes.dmp");

        AccTaxDB accessionTaxDb = new AccTaxDB();
        accessionTaxDb.readFromDiskCsv(dbDir + "gen/acc_taxids.csv");

        Map<Integer, List<ProteinResult>> taxIdToProteins = new HashMap<>();


        // 3. Grupirajte proteine po TaxID-u
        for (Map.Entry<String, Integer> entry : ranked) {
            String accession = entry.getKey();
            int peptideCount = entry.getValue();
            List<Integer> taxonomyIds = accessionTaxDb.getTaxonomyIds(accession);
            for (Integer taxonomyId : taxonomyIds) {
                taxIdToProteins.computeIfAbsent(taxonomyId, k -> new ArrayList<>())
                      .add(new ProteinResult(accession, peptideCount, taxonomyId));
            }
        }


        // 4. Ispišite rezultate
        // sortiraj po navecem broju peptida


        for (Map.Entry<Integer, List<ProteinResult>> entry : taxIdToProteins.entrySet()) {
            Integer taxId = entry.getKey();
            List<ProteinResult> proteins = entry.getValue();
            //  log.info("TaxID: " + taxId + ", Proteins: " + proteins.size());
            proteins.sort(Comparator.naturalOrder()); // Sort by peptide count descending
            int c = 1;
//            if (taxId == 9940) {
            System.out.println("TaxID: " + taxId + ", Proteins: " + proteins.size());
            System.out.println("#, Accession, Peptide Count");
//            }
            for (ProteinResult protein : proteins) {
                //  if (taxId == 9940) {
                //log.info("  Accession: " + protein.getAccession() + ", Peptide Count: " + protein.getPeptideCount());
                System.out.println(c++ + "  " + protein.getAccession() + ", " + protein.getPeptideCount());
                // }
//                if (c++ > 10) {
//                    break; // Limit to top 10 proteins per taxId
//                }
            }
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

    @Getter
    @AllArgsConstructor

    static class ProteinResult implements Comparable<ProteinResult> {
        private String accession;
        private int peptideCount;
        private Integer taxId;

        @Override
        public int compareTo(ProteinResult o) {
            return Integer.compare(o.peptideCount, this.peptideCount); // Sort by peptide count descending
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
