package hr.pbf.digestdb.util;

import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import hr.pbf.digestdb.model.TaxonomyDivision;
import it.unimi.dsi.fastutil.ints.AbstractInt2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntRBTreeMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


@Slf4j
public class NcbiTaksonomyRelations {

    @Getter
    private final AbstractInt2IntMap childParrentsMap;

    @Getter
    private final Int2IntOpenHashMap taxIdToDivisionIdMap;

    private NcbiTaksonomyRelations(String pathToNodesDmp) throws NcbiTaxonomyException {
        taxIdToDivisionIdMap = new Int2IntOpenHashMap();
        childParrentsMap = loadTaxonomyFromPath(pathToNodesDmp);
    }

    public static NcbiTaksonomyRelations loadTaxonomyNodes(String pathToNodesDmp) throws NcbiTaxonomyException {
        return new NcbiTaksonomyRelations(pathToNodesDmp);
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
                if (line.trim().isEmpty()) {
                    continue;
                }

                //String[] parts = line.split("\\|");
                String[] parts = line.split("\t\\|\t");
                try {
                    int taxId = Integer.parseInt(parts[0].trim());
                    int parentTaxId = Integer.parseInt(parts[1].trim());
                    int divisionId = Integer.parseInt(parts[4].trim());
                    taxIdToDivisionIdMap.put(taxId, divisionId);
                    if (taxId == parentTaxId) {
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

    /**
     * Returns the division ID for a given taxonomy ID.
     *
     * @param taxId The taxonomy ID.
     * @return The division ID, or -1 if not found.
     */
    public int getDivisionForTaxId(int taxId) {
        return taxIdToDivisionIdMap.getOrDefault(taxId, -1);
    }

    /**
     * Checks if a taxonomy ID belongs to a specific division.
     *
     * @param taxId      The taxonomy ID to check.
     * @param divisionId The division ID to check against.
     * @return true if the taxId belongs to the division, false otherwise.
     */
    public boolean isTaxIdInDivision(int taxId, int divisionId) {
        return taxIdToDivisionIdMap.getOrDefault(taxId, -1) == divisionId;
    }

    public boolean isTaxIdInDivision(int taxId, TaxonomyDivision division) {
        return isTaxIdInDivision(taxId, division.getId());
    }

    public boolean isDirectParent(int child, int parent) {
        if (childParrentsMap.containsKey(child)) {
            return childParrentsMap.get(child) == parent;
        }
        return false;
    }

    public boolean isAncestor(int taxIdDescendant, int taxIdAncestor, Integer maxDepth) {
        if (taxIdDescendant == taxIdAncestor) {
            return true; // Same ID
        }
        Integer currentId = taxIdDescendant;
        int depth = 0;
        while (childParrentsMap.containsKey(currentId.intValue())) {
            if (currentId == taxIdAncestor) {
                return true;
            }
            currentId = childParrentsMap.get(currentId.intValue());
            depth++;
            if (maxDepth != null && depth > maxDepth) {
                return false;
            }
            if (currentId.equals(taxIdDescendant)) {
                return false; // Detection that we didn't move
            }
            if (currentId == 1 && taxIdAncestor != 1) {
                return false; // If we reach the root and the search is not for root
            }
        }
        return currentId.equals(taxIdAncestor);
    }

    public boolean isAncestor(int taxIdDescendant, int taxIdAncestor) {
        return isAncestor(taxIdDescendant, taxIdAncestor, null);
    }
}
