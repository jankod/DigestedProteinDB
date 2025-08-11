package hr.pbf.digestdb.experiments;

import hr.pbf.digestdb.db.AccessionDbReader;
import hr.pbf.digestdb.db.MassRocksDbReader;
import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import hr.pbf.digestdb.util.AccTaxDB;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static hr.pbf.digestdb.experiments.SearchSheep.getMassesSheep;

@Slf4j
public class SearchSheep2 {

    static NCBITaxaEte ncbi;
    static AccessionDbReader accessionDbReader;

    public static void main(String[] args) throws RocksDBException, NcbiTaxonomyException, IOException {
        String dbDir = "/home/tag/IdeaProjects/DigestedProteinDB/misc/db/sheep2/";
        //dbDir = "/Users/tag/PBF radovi/digestedproteindb/sheep/rocksdb_mass.db"; // For testing on local machine
        dbDir = "/home/tag/IdeaProjects/DigestedProteinDB/misc/db/trembl/";

        String pathSheep = "/media/tag/D/digested-db/Trypsin_HTXdigest-ovca.txt";
        //pathSheep = "'/Users/tag/PBF radovi/digestedproteindb'/Trypsin_HTXdigest_sheep_butorka.txt";

        String pathToNodesDmp = "/home/tag/IdeaProjects/DigestedProteinDB/misc/ncbi/taxdump/nodes.dmp";

        MassRocksDbReader db = new MassRocksDbReader(dbDir + "rocksdb_mass.db");
        db.open();

        accessionDbReader = new AccessionDbReader(dbDir + "custom_accession.db");

        //NcbiTaksonomyRelations taxonomy = NcbiTaksonomyRelations.loadTaxonomyNodes(pathToNodesDmp);
        ncbi = new NCBITaxaEte();

        String resultPath = "/home/tag/peptides_hits_sheep.csv";

        AccTaxDB accessionTaxDb = AccTaxDB.loadFromDiskCsv(dbDir + "/acc_taxid.csv");
        List<Double> sheepMasses = getMassesSheep(pathSheep);

        log.debug("Sheep masses: " + sheepMasses.size());


        // Step 2: // Sort the result file by taxID and accID  (Peptide,AccId,TaxId)
        // LC_ALL=C sort -t',' -k3,3n -k2,2 --parallel="$(nproc)" -S 70% peptides_hits_sheep.csv -o peptides_hits_sheep_sorted.csv

        // Step 3:
        analyzeCsvResultWriteSummaryCsv(resultPath, new File(resultPath).getParent() + "/peptides_hits_sheep_analyzed.csv");
        if (true) {
            return;
        }

        try (BufferedWriter result = Files.newBufferedWriter(Path.of(resultPath))) {

            // result.write("Peptide,AccId,TaxId\n");
            for (Double mass : sheepMasses) {
                double mass1 = mass - 0.02;
                double mass2 = mass + 0.02;

                List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> peptides = db.searchByMass(mass1, mass2);


                // Step 1: Search for peptides and write to result csv
                for (Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>> entry : peptides) {
                    for (BinaryPeptideDbUtil.PeptideAccids peptideAccids : entry.getValue()) {
                        String peptide = peptideAccids.getSeq();
                        int[] accIds = peptideAccids.getAccids();




                        for (int accIdInt : accIds) {
                            String acc = accessionDbReader.getAccession(accIdInt);
                            int taxId = accessionTaxDb.getTaxonomyId(acc);
                            result.write(peptide + "," + acc + "," + taxId);
                            result.newLine();

                        }
                    }
                }
            } // end of mass loop


        }
    }

    private static void writeToDuckDb(int[] accIds, String peptide, AccTaxDB accessionTaxDb) {



    }

    record PeptideAcc(String peptide, int accId, int taxId) {
    }

    static void analyzeCsvResultWriteSummaryCsv(String csvInputPath, String csvResultPath) throws IOException {
        int currentTaxId = -1;
        List<PeptideAcc> oneTaxResults = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(csvInputPath));
             BufferedWriter writer = Files.newBufferedWriter(Path.of(csvResultPath))) {
            // sorted csv file with columns: Peptide,AccId,TaxId
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String peptide = parts[0].trim();
                int accId = Integer.parseInt(parts[1].trim());
                int taxId = Integer.parseInt(parts[2].trim());
                if (currentTaxId == -1) {
                    currentTaxId = taxId;
                } else if (currentTaxId != taxId) {
                    // new taxId
                    OneTaxResult oneTaxResult = analyzeOneTaxonomy(currentTaxId, oneTaxResults);
                    oneTaxResult.taxName = ncbi.getBestName(currentTaxId);
                    writer.write(oneTaxResult.toCsvLine(accessionDbReader));
                    oneTaxResults = new ArrayList<>();
                }
                oneTaxResults.add(new PeptideAcc(peptide, accId, taxId));

            }

        }


    }

    private static OneTaxResult analyzeOneTaxonomy(int taxId, List<PeptideAcc> oneTaxResults) {
        int proteinCount = 0;
        int peptideCount = 0;
        long uniquePeptideCount = oneTaxResults.stream().map(PeptideAcc::peptide).distinct().count();

        Set<Integer> uniqueAccIds = new HashSet<>();
        for (PeptideAcc pa : oneTaxResults) {
            uniqueAccIds.add(pa.accId);
            peptideCount++;
        }
        proteinCount = uniqueAccIds.size();

        return new OneTaxResult(taxId, proteinCount, peptideCount, uniquePeptideCount);


    }

    @Data
    static class OneTaxResult {
        int taxId;
        String taxName;
        int proteinCount;
        int peptideCount;
        long uniquePeptideCount;
        //List<PeptideAcc> peptideResults;

        public OneTaxResult(int taxId, int proteinCount, int peptideCount, long uniquePeptideCount) {
            this.taxId = taxId;
            this.proteinCount = proteinCount;
            this.peptideCount = peptideCount;
            this.uniquePeptideCount = uniquePeptideCount;
        }


        public String toCsvLine(AccessionDbReader accessionDb) {
            return accessionDb.getAccession(taxId) + "," + taxName + "," + proteinCount + "," + peptideCount + "," + uniquePeptideCount;
        }
    }


    private static void analyzeAndWriteResults(Int2ObjectOpenHashMap<HitTaxonomy> taxonIdMap, BufferedWriter result) {
        // sort by peptide count descending, then by taxId ascending
        Stream<HitTaxonomy> sorted = taxonIdMap.values().stream().sorted(Comparator.comparingInt(HitTaxonomy::countPeptides).reversed());
        // ispisi samo prvih 100
        //log.debug("Total unique taxa found: " + sorted.count());
        log.debug("Writing results to file...");

        sorted.limit(200).forEach(hitTaxonomy -> {
            try {
                int taxId = hitTaxonomy.taxId;
                String taxName = ncbi.getBestName(taxId);
                int proteinCount = hitTaxonomy.proteins.size();
                int peptideCount = hitTaxonomy.countPeptides();
//                long uniquePeptideCount = hitTaxonomy.proteins.values().stream()
//                      .flatMap(protein -> protein.peptides.stream())
//                      .distinct()
//                      .count();
                long uniquePeptideCount = -1L; // Not calculated in this version, can be added if needed


                result.write(String.format("%s\t%d\t%d\t%d\n", taxName, proteinCount, peptideCount, uniquePeptideCount));
            } catch (IOException e) {
                log.error("Error writing results for taxId: " + hitTaxonomy.taxId, e);
            }
        });
    }


    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    static class HitTaxonomy {
        @EqualsAndHashCode.Include
        int taxId;
        //String taxName;

        Int2ObjectOpenHashMap<HitProtein> proteins = new Int2ObjectOpenHashMap<>();

        public void add(int accId, String peptide) {
            if (proteins.containsKey(accId)) {
                HitProtein existingProtein = proteins.get(accId);
                existingProtein.addPeptide(peptide);
            } else {
                HitProtein hitProtein = new HitProtein(accId, peptide);
                proteins.put(accId, hitProtein);
            }
        }

        public int countPeptides() {
            int count = 0;
            for (HitProtein protein : proteins.values()) {
                count += protein.peptides.size();
            }
            return count;
        }


    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    static class HitProtein {
        @EqualsAndHashCode.Include
        int accId;

        //String proteinName;

        Set<String> peptides = new HashSet<>();

        public HitProtein(int accId, String peptide) {
            this.accId = accId;
            addPeptide(peptide);
        }

        public void addPeptide(String peptide) {
            peptides.add(peptide);
        }

    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    static class HitPeptide {

        @EqualsAndHashCode.Include
        String peptide;

        List<AccTax> accTaxList = new ArrayList<>();

    }

    @Data
    static class AccTax {
        int accId;
        int taxId;
    }

}




