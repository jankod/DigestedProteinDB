package hr.pbf.digestdb.experiments;

import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import hr.pbf.digestdb.util.NcbiTaksonomyRelations;
import lombok.Data;

import java.io.IOException;
import java.util.*;

public class TaxonomySearchCalc {


    public static void main2(String[] args) throws IOException, InterruptedException, NcbiTaxonomyException {
        double mass1 = 1600.0;
        double mass2 = 1600.1;


        NcbiTaksonomyRelations taksonomy = NcbiTaksonomyRelations.loadTaxonomyNodes("/Users/tag/IdeaProjects/DigestedProteinDB/misc/ncbi_taxonomy/taxdump/nodes.dmp");


        TaxonomySearch.TaxonomySearchResult taxonomyResults = TaxonomySearch.searchTaxonomy(mass1, mass2);

        MassSpectrometryDatabase database = new MassSpectrometryDatabase();

        taxonomyResults.result.forEach(stringListMap -> {

            stringListMap.forEach((key, value) -> {

            });

        });
    }


    @Data
    class Peptide {
        private double mass;
        private Set<Protein> proteins = new HashSet<>();
    }

    class Taxon {
        private String taxId;
        private String name;
        private Taxon parent;
        private List<Taxon> children = new ArrayList<>();
    }


    @Data
    class Protein {
        String accession;
        private Set<Taxon> taxons = new HashSet<>();
        private List<Peptide> peptides = new ArrayList<>();
    }


    @Data
    static class MassSpectrometryDatabase {
        private TreeMap<Double, Set<Peptide>> massIndex = new TreeMap<>();

        public void addPeptide(Peptide peptide) {
            massIndex.computeIfAbsent(peptide.getMass(), k -> new HashSet<>()).add(peptide);
        }

        public List<Peptide> findPeptidesInRange(double mass1, double mass2) {
            NavigableMap<Double, Set<Peptide>> subMap = massIndex.subMap(mass1, true, mass2, true);
            List<Peptide> peptides = new ArrayList<>();
            subMap.values().forEach(peptides::addAll);
            return peptides;
        }

        public Map<Taxon, Integer> getTaxonFrequency(double mass1, double mass2) {
            List<Peptide> peptides = findPeptidesInRange(mass1, mass2);
            Map<Taxon, Integer> taxonFrequency = new HashMap<>();

            for (Peptide peptide : peptides) {
                for (Protein protein : peptide.getProteins()) {
                    for (Taxon taxon : protein.getTaxons()) {
                        taxonFrequency.put(taxon, taxonFrequency.getOrDefault(taxon, 0) + 1);
                    }
                }
            }

            return taxonFrequency;
        }
    }


}
