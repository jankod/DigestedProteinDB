package hr.pbf.digestdb.tools;

import hr.pbf.digestdb.util.TaxonomyParser;
import hr.pbf.digestdb.util.UniprotXMLParser;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class FilterBioType {
    private final Map<Integer, TaxonomyParser.TaxonomyNode> taxNodeMap;

    public FilterBioType(Map<Integer, TaxonomyParser.TaxonomyNode> taxNodeMap) {
        this.taxNodeMap = taxNodeMap;
    }

    public boolean isOK(UniprotXMLParser.ProteinInfo p) {

        if (isWrongRank(p)) {
            return false;
        }

        if (isEnvironmentalSample(p)) {
            return false;
        }


        return true; // all ok
    }

    public boolean isEnvironmentalSample(UniprotXMLParser.ProteinInfo p) {
        int taxonomyId = p.getTaxonomyId();
        // Odbaci i posebne “kolektivne” čvorove: synthetic construct (32630), metagenome (408169), environmental samples itd.
        switch (taxonomyId) {
            case 32630: // synthetic construct
            case 408169: // metagenome
            case 28384: // environmental samples
            case 12908: // uncultured bacterium
            case 77333: // uncultured archaeon
            case 39463: // uncultured eukaryote
            case 9999999: // other sequences
                return false;
            default:
                return true;
        }

    }

    public boolean isWrongRank(UniprotXMLParser.ProteinInfo p) {
        // Prihvati ako je rank ∈ {species, subspecies, strain, varietas, forma} (što ti treba).
        int taxonomyId = p.getTaxonomyId();

        TaxonomyParser.TaxonomyNode node = taxNodeMap.get(taxonomyId);
        if (node == null) {
            log.warn("Taxonomy id " + taxonomyId + " not found");
            return false;
        }

        String rank = node.getRank();
        if (rank == null) {
            log.warn("Taxonomy id " + taxonomyId + " has null rank");
            return false;
        }

        return switch (rank) {
            case "species", "subspecies", "strain", "varietas", "forma" -> true;
            default -> false;
        };
    }
}
