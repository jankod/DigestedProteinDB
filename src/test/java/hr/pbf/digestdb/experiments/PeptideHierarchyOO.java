package hr.pbf.digestdb.experiments;

import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.NcbiTaksonomyRelations;
import lombok.Getter;

import java.io.*;
import java.nio.file.*;
import java.util.*;


/**
 *
 */
public class PeptideHierarchyOO {

    public static void main(String[] args) throws IOException, NcbiTaxonomyException {

        String inputPath = "/home/tag/peptides_sheep.txt";
        String outputPath = "/home/tag/peptides_sheep_sorted.txt";

        String pathToNodesDmp = "/home/tag/IdeaProjects/DigestedProteinDB/misc/ncbi/taxdump/nodes.dmp";
       // NcbiTaksonomyRelations taxonomy = NcbiTaksonomyRelations.loadTaxonomyNodes(pathToNodesDmp);


        List<PeptideRecord> records = Files.lines(Paths.get(inputPath))
              .skip(1)
              .flatMap(line -> parseLine(line).stream())
              .toList();

        // Grouping to tree structure
        Map<Integer, Taxon> taxa = new TreeMap<>();
        for (PeptideRecord rec : records) {
            taxa.computeIfAbsent(rec.taxId(), Taxon::new)
                  .addPeptide(rec.accession(), rec.peptide());
        }

        // Ispis u stablastom formatu
        try (PrintWriter out = new PrintWriter(outputPath); NCBITaxaEte ncbi = new NCBITaxaEte()) {

            // Print first just the summary of taxa
            out.println("TaxId\tName\tProteins\tPeptides");
            taxa.values().stream()
                  .sorted(
                        Comparator.<Taxon>comparingInt(Taxon::getPeptideCount)
                              .reversed()
                              .thenComparing(Taxon::getTaxId)
                  )
                  .forEach(taxon -> {
                      out.printf("%d\t%s\t%d\t%d\n",
                            taxon.getTaxId(),
                            ncbi.getTaxidTranslator(Set.of(taxon.getTaxId()), true),
                            taxon.getProteinCount(),
                            taxon.getPeptideCount());

                  });


            out.println();
            out.println();
            taxa.values().stream()
                  .sorted(
                        Comparator.<Taxon>comparingInt(Taxon::getPeptideCount)
                              .reversed()
                              .thenComparing(Taxon::getTaxId)
                  )
                  .forEach(taxon -> {
                      // ispis...
                      out.printf("TaxId: %d\tProteins: %d\tPeptides: %d%n",
                            taxon.getTaxId(),
                            taxon.getProteinCount(),
                            taxon.getPeptideCount());
                      for (Protein prot : taxon.getProteinsSorted()) {
                          out.printf("  Accession: %s %n",
                                prot.getAccession()
                          );
                          for (String pep : prot.getPeptidesSorted()) {
                              double mass = BioUtil.calculateMassWidthH2O(pep);
                              out.printf("    %s  %.4f%n", pep, mass);
                          }
                      }
                      out.println();
                  });

        }
    }

    // Parsira red i vraća listu svih kombinacija Peptide–Accession–TaxID
    private static List<PeptideRecord> parseLine(String line) {
        String[] parts = line.split(",", 3);
        if (parts.length < 3) return Collections.emptyList();
        String peptide = parts[0].trim();
        String[] accs = parts[1].trim().split(";");
        String[] taxIds = parts[2].trim().split(";");
        List<PeptideRecord> list = new ArrayList<>();
        for (String acc : accs) {
            for (String tax : taxIds) {
                try {
                    list.add(new PeptideRecord(peptide, acc.trim(), Integer.parseInt(tax.trim())));
                } catch (NumberFormatException e) {
                    System.err.println("Neispravan TaxID: " + tax);
                }
            }
        }
        return list;
    }


    // ----- PODACI -----

    record PeptideRecord(String peptide, String accession, int taxId) {
    }

    static class Taxon {
        @Getter
        private final int taxId;
        private final Map<String, Protein> proteins = new TreeMap<>();

        public Taxon(int taxId) {
            this.taxId = taxId;
        }

        public void addPeptide(String accession, String peptide) {
            proteins.computeIfAbsent(accession, Protein::new)
                  .addPeptide(peptide);
        }

        public int getProteinCount() {
            return proteins.size();
        }

        public int getPeptideCount() {
            return proteins.values().stream()
                  .mapToInt(p -> p.getPeptides().size()).sum();
        }

        public List<Protein> getProteinsSorted() {
            return proteins.values().stream()
                  .sorted(
                        Comparator.<Protein>comparingInt(p -> p.getPeptides().size())
                              .reversed() // silazno po broju peptida
                              .thenComparing(Protein::getAccession) // ako je isti broj, sort po accessionu
                  )
                  .toList();
        }

    }

    static class Protein {
        private final String accession;
        private final Set<String> peptides = new TreeSet<>();

        public Protein(String accession) {
            this.accession = accession;
        }

        public void addPeptide(String peptide) {
            peptides.add(peptide);
        }

        public String getAccession() {
            return accession;
        }

        public Set<String> getPeptides() {
            return peptides;
        }

        public List<String> getPeptidesSorted() {
            return new ArrayList<>(peptides);
        }
    }
}
