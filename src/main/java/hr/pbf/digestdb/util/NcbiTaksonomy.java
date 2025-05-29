package hr.pbf.digestdb.util;

import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import it.unimi.dsi.fastutil.ints.AbstractInt2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntRBTreeMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


@Slf4j
public class NcbiTaksonomy {

    @Getter
    private final AbstractInt2IntMap childParrents;

    private NcbiTaksonomy(String pathToCsv) throws NcbiTaxonomyException {
        childParrents = loadTaxonomyFromPath(pathToCsv);
    }

    public static NcbiTaksonomy loadTaxonomy(String pathToNodesDmp) throws NcbiTaxonomyException {
        return new NcbiTaksonomy(pathToNodesDmp);
    }

    /**
     * Load taxonomy from a file nodes.dmp of NCBI taxonomy database.
     *
     * @param pathToNodesDmp File path to nodes.dmp
     * @return A map (TadID => ParrentTaxID) of tax IDs to their parent tax IDs
     */
    private AbstractInt2IntMap loadTaxonomyFromPath(String pathToNodesDmp) throws NcbiTaxonomyException {
        AbstractInt2IntMap relations = new Int2IntRBTreeMap();
        try (BufferedReader br = new BufferedReader(new FileReader(pathToNodesDmp))) {
            String line;
            // Skip header line if exists
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                try {
                    int taxId = Integer.parseInt(parts[0].trim());
                    int parentTaxId = Integer.parseInt(parts[1].trim());
                    if(taxId == parentTaxId) {
                        // root taxon
                        continue;
                    }
                    relations.put(taxId, parentTaxId);
                } catch (NumberFormatException e) {
                    throw new NcbiTaxonomyException("Error parsing tax ID or parent tax ID: " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new NcbiTaxonomyException("Error reading file: " + e.getMessage(), e);
        }
        return relations;
    }

    public boolean isDirectParent(int child, int parent) {
        if (childParrents.containsKey(child)) {
            return childParrents.get(child) == parent;
        }
        return false;
    }

    public boolean isAncestor(int taxIdDescendant, int taxIdAncestor, Integer maxDepth) {
        if(taxIdDescendant == taxIdAncestor) {
            return true; // Same ID
        }
        Integer currentId = taxIdDescendant;
        int depth = 0;
        while (childParrents.containsKey(currentId.intValue())) {
            if (currentId == taxIdAncestor) {
                return true;
            }
            currentId = childParrents.get(currentId);
            depth++;
            if (maxDepth != null && depth > maxDepth) {
                return false;
            }
            if (currentId != null && currentId.equals(taxIdDescendant)) {
                return false; // Detection that we didn't move
            }
            if (currentId != null && currentId == 1 && taxIdAncestor != 1) {
                return false; // If we reach the root and the search is not for root
            }
            if (currentId == null) {
                return false; // If there's no parent
            }
        }
        return currentId.equals(taxIdAncestor);
    }

    public boolean isAncestor(int taxIdDescendant, int taxIdAncestor) {
        return isAncestor(taxIdDescendant, taxIdAncestor, null);
    }
}