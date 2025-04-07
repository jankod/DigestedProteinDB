package hr.pbf.digestdb.demo;


import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.UniprotXMLParser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class PeptideStatistic {

    @Data
    public static class ProteomeStats {
        private int totalProteins = 0;
        private int totalPeptides = 0;
        private long totalAminoAcids = 0;
        private int proteinsWithNoPeptides = 0;
        private int maxPeptidesInProtein = 0;
        private String proteinWithMaxPeptides = "";
        private Map<Integer, Integer> peptideLengthDistribution = new HashMap<>();
        private Map<Integer, Integer> peptidesPerProteinDistribution = new HashMap<>();
        private Map<Integer, Integer> peptideMassDistribution = new HashMap<>();
        private Map<Character, Integer> aminoAcidFrequency = new HashMap<>();
        private List<String> proteinsWithFewCleavages = new ArrayList<>();
        private double avgPeptidesPerProtein = 0;
        private double avgPeptideLength = 0;
    }

    public static void main(String[] args) {

        String unprotFile = "";
        unprotFile = args[0];
        ProteomeStats stats = analyzeProteome(unprotFile);
        printStatistics(stats);
    }

    public static ProteomeStats analyzeProteome(String unprotFile) {
        ProteomeStats stats = new ProteomeStats();
        AtomicInteger processedProteins = new AtomicInteger(0);

        UniprotXMLParser parser = new UniprotXMLParser();
        parser.parseProteinsFromXMLstream(unprotFile, new UniprotXMLParser.ProteinHandler() {
            @Override
            public void gotProtein(UniprotXMLParser.ProteinInfo p) throws IOException {
                List<String> peptides = BioUtil.tripsyn1mc(p.getSequence(), 7, 300_000);

                // Count proteins
                stats.setTotalProteins(stats.getTotalProteins() + 1);

                // Process peptides
                processPeptides(p, peptides, stats);

                // Log progress periodically
                int count = processedProteins.incrementAndGet();
                if (count % 1000 == 0) {
                    log.info("Processed {} proteins", count);
                }
            }
        });

        // Calculate averages
        if (stats.getTotalProteins() > 0) {
            stats.setAvgPeptidesPerProtein((double) stats.getTotalPeptides() / stats.getTotalProteins());
        }

        if (stats.getTotalPeptides() > 0) {
            stats.setAvgPeptideLength((double) stats.getTotalAminoAcids() / stats.getTotalPeptides());
        }

        return stats;
    }

    private static void processPeptides(UniprotXMLParser.ProteinInfo protein, List<String> peptides, ProteomeStats stats) {
        int peptideCount = peptides.size();

        stats.setTotalPeptides(stats.getTotalPeptides() + peptideCount);

        stats.getPeptidesPerProteinDistribution().merge(peptideCount, 1, Integer::sum);

        if (peptideCount == 0) {
            stats.setProteinsWithNoPeptides(stats.getProteinsWithNoPeptides() + 1);
        }

        // Check proteins with few cleavages (less than 3 peptides and protein length > 200)
        if (peptideCount < 3 && protein.getSequence().length() > 200) {
            stats.getProteinsWithFewCleavages().add(protein.getAccession());
        }

        // Update max peptides in a protein
        if (peptideCount > stats.getMaxPeptidesInProtein()) {
            stats.setMaxPeptidesInProtein(peptideCount);
            stats.setProteinWithMaxPeptides(protein.getAccession());
        }

        // Process individual peptides
        for (String peptide : peptides) {
            int length = peptide.length();

            stats.setTotalAminoAcids(stats.getTotalAminoAcids() + length);

            stats.getPeptideLengthDistribution().merge(length, 1, Integer::sum);

            double mass = BioUtil.calculateMassWidthH2O(peptide);
            // Round to nearest 100 Da to create bins
            int massKey = (int) (Math.round(mass / 100.0) * 100);
            stats.getPeptideMassDistribution().merge(massKey, 1, Integer::sum);

            // Update amino acid frequency
            for (char aa : peptide.toCharArray()) {
                stats.getAminoAcidFrequency().merge(aa, 1, Integer::sum);
            }
        }
    }


    private static void printStatistics(ProteomeStats stats) {
        System.out.println("==== PROTEOME PEPTIDE STATISTICS ====");
        System.out.println("Total proteins analyzed: " + stats.getTotalProteins());
        System.out.println("Total peptides found: " + stats.getTotalPeptides());
        System.out.println("Average peptides per protein: " + String.format("%.2f", stats.getAvgPeptidesPerProtein()));
        System.out.println("Average peptide length: " + String.format("%.2f", stats.getAvgPeptideLength()));
        System.out.println("Proteins with no peptides: " + stats.getProteinsWithNoPeptides());
        System.out.println("Maximum peptides in a protein: " + stats.getMaxPeptidesInProtein() +
                           " (Accession: " + stats.getProteinWithMaxPeptides() + ")");

        System.out.println("\n== Peptide Length Distribution ==");
        printHistogram(stats.getPeptideLengthDistribution(), "Length (aa)");

        System.out.println("\n== Peptides Per Protein Distribution ==");
        printDistribution(stats.getPeptidesPerProteinDistribution(), "Peptide Count", "Proteins");

        System.out.println("\n== Peptide Mass Distribution ==");
        List<Map.Entry<Integer, Integer>> massDist = new ArrayList<>(stats.getPeptideMassDistribution().entrySet());
        massDist.sort(Map.Entry.comparingByKey());
        for (Map.Entry<Integer, Integer> entry : massDist) {
            System.out.printf("%d Da: %d peptides\n", entry.getKey(), entry.getValue());
        }

        System.out.println("\n== Amino Acid Frequency ==");
        Map<Character, Integer> sortedAaFreq = stats.getAminoAcidFrequency().entrySet().stream()
              .sorted(Map.Entry.<Character, Integer>comparingByValue().reversed())
              .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
              ));
        for (Map.Entry<Character, Integer> entry : sortedAaFreq.entrySet()) {
            System.out.printf("%c: %d occurrences\n", entry.getKey(), entry.getValue());
        }

        System.out.println("\n== Proteins With Few Cleavage Sites ==");
        System.out.println("Count: " + stats.getProteinsWithFewCleavages().size());
        if (stats.getProteinsWithFewCleavages().size() <= 10) {
            stats.getProteinsWithFewCleavages().forEach(System.out::println);
        } else {
            System.out.println("First 10 entries:");
            stats.getProteinsWithFewCleavages().stream().limit(10).forEach(System.out::println);
        }
    }

    private static void printDistribution(Map<Integer, Integer> distribution, String keyLabel, String valueLabel) {
        List<Map.Entry<Integer, Integer>> sortedDist = new ArrayList<>(distribution.entrySet());
        sortedDist.sort(Map.Entry.comparingByKey());
        for (Map.Entry<Integer, Integer> entry : sortedDist) {
            System.out.printf("%s: %d, %s: %d\n", keyLabel, entry.getKey(), valueLabel, entry.getValue());
        }
    }

    private static void printHistogram(Map<Integer, Integer> distribution, String label) {
        List<Map.Entry<Integer, Integer>> sortedDist = new ArrayList<>(distribution.entrySet());
        sortedDist.sort(Map.Entry.comparingByKey());

        int maxFreq = sortedDist.stream()
              .mapToInt(Map.Entry::getValue)
              .max()
              .orElse(0);

        int scaleFactor = Math.max(1, maxFreq / 50); // Scale to fit in console

        for (Map.Entry<Integer, Integer> entry : sortedDist) {
            int stars = entry.getValue() / scaleFactor;
            String bar = "*".repeat(stars);
            System.out.printf("%3d: %8d %s\n", entry.getKey(), entry.getValue(), bar);
        }
    }
}
