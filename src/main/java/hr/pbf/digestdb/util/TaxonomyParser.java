package hr.pbf.digestdb.util;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TaxonomyParser {

    @Data
    public static class TaxonomyNode {
        int taxId;
        int parentTaxId;
        String rank;
        String name;
        short divisionId;
        //List<TaxonomyNode> children = new ArrayList<>();
        int[] childrenTaxIds = new int[0];

        public TaxonomyNode(int taxId, int parentTaxId, String rank, short divisionId) {
            this.taxId = taxId;
            this.parentTaxId = parentTaxId;
            this.rank = rank;
            this.divisionId = divisionId;
        }
    }

    @Data
    public static class Division {
        short divisionId;
        String name;

        public Division(short divisionId, String name) {
            this.divisionId = divisionId;
            this.name = name;
        }
    }



    /**
     * @param nodesPath path to nodes.dmp file
     * @return map of taxId to TaxonomyNode
     */
    public static Map<Integer, TaxonomyNode> parseNodes(String nodesPath) throws IOException {
        Map<Integer, TaxonomyNode> taxonomyMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(nodesPath))) {
            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|\\t");
                    int taxId = Integer.parseInt(parts[0].trim());
                    int parentTaxId = Integer.parseInt(parts[1].trim());
                    String rank = parts[2].trim();
                    short divisionId = Short.parseShort(parts[4].trim());
                    taxonomyMap.put(taxId, new TaxonomyNode(taxId, parentTaxId, rank, divisionId));
                }
            } catch (Exception e) {
                log.error("Error on line: " + line, e);
            }

        }
        return taxonomyMap;
    }

    public static void parseNames(String namesFilePath, Map<Integer, TaxonomyNode> taxonomyMap) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(namesFilePath))) {
            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    int taxId = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    String nameClass = parts[3].trim().toLowerCase();
                    if (taxonomyMap.containsKey(taxId) && nameClass.equals("scientific name")) {
                        taxonomyMap.get(taxId).name = name;
                    }
                }
            } catch (Exception e) {
                log.error("Error on line: " + line, e);
            }
        }
    }

    public static Map<Short, Division> parseDivision(String divisionPath) throws IOException {
// 0	|	BCT	|	Bacteria	|	 	|
//1	|	INV	|	Invertebrates	|	 	|
//2	|	MAM	|	Mammals	|	 	|
        Map<Short, Division> divisions = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(divisionPath))) {
            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    short divisionId = Short.parseShort(parts[0].trim());
                    String name = parts[1].trim();
                    divisions.put(divisionId, new Division(divisionId, name));
                }
            } catch (Exception e) {
                log.error("Error on line: " + line, e);
            }
        }
        return divisions;

    }




    private static void saveToDb(Map<Integer, TaxonomyNode> taxonomyMap, Map<Short, Division> divisions, String dir) throws IOException {
        try (FileWriter writer = new FileWriter(dir + "/digested_taxonomy.csv")) {
            writer.write("taxId,parentTaxId,rank,name,divisionId\n");
            for (TaxonomyNode t : taxonomyMap.values()) {
                //String name = StringUtils.replace(t.name, ",", "");
                writer.write(t.taxId + "\t" + t.parentTaxId + "\t" + t.rank + "\t" + t.name + "\t" + t.divisionId + "\n");
            }
        }
    }

    private static void printChildren(TaxonomyNode child, final int level, Map<Integer, TaxonomyNode> taxonomyMap) {
        for (int i = 0; i < child.childrenTaxIds.length; i++) {
            int taxId = child.childrenTaxIds[i];
            TaxonomyNode node = taxonomyMap.get(taxId);
            log.debug(" ".repeat(level) + node.name + " Division: " + node.divisionId);
            printChildren(node, level + 1, taxonomyMap);
        }
    }

    private static void createTaxonomyTree(Map<Integer, TaxonomyNode> taxonomyMap, Map<Short, Division> divisions, TaxonomyNode parent) {
        parent.childrenTaxIds = findChildren(taxonomyMap, divisions, parent).toIntArray();
        log.debug("Tree: " + parent.name + " has children " + parent.childrenTaxIds.length);
        for (int i = 0; i < parent.childrenTaxIds.length; i++) {
            TaxonomyNode child = taxonomyMap.get(parent.childrenTaxIds[i]);
            createTaxonomyTree(taxonomyMap, divisions, child);
        }
    }

    private static IntSet findChildren(Map<Integer, TaxonomyNode> taxonomyMap, Map<Short, Division> divisions, TaxonomyNode parent) {
        IntSet childrenTaxIds = new IntOpenHashSet();
        for (TaxonomyNode node : taxonomyMap.values()) {
            if (node.parentTaxId == parent.taxId) {
                childrenTaxIds.add(node.taxId);
            }
        }
        return childrenTaxIds;
    }

    private static TaxonomyNode findParent(List<TaxonomyNode> taxTreeNodes, int parentTaxId) {
        for (TaxonomyNode node : taxTreeNodes) {
            if (node.taxId == parentTaxId) {
                return node;
            }
        }
        return null;
    }

 public static void main(String[] args) {
        String dir = ".../misc/ncbi_taxonomy/taxdump";
        String nodesPath = dir + "/nodes.dmp";
        String namesPath = dir + "/names.dmp";
        String divisionPath = dir + "/division.dmp";

        try {
            Map<Integer, TaxonomyNode> taxonomyMap = parseNodes(nodesPath);


            parseNames(namesPath, taxonomyMap);
            // Koristite taxonomyMap
            log.debug("Broj taksonomskih čvorova: " + taxonomyMap.size());
            TaxonomyNode bacteria = taxonomyMap.get(2);
            log.debug("Bacteria scientific names: " + bacteria.name);


            Map<Short, Division> divisions = parseDivision(divisionPath);

            saveToDb(taxonomyMap, divisions, dir);

//            TaxonomyNode root = new TaxonomyNode(1, 0, "root", (short) 0);
//            root.name = "root";
         /*   createTaxonomyTree(taxonomyMap, divisions, root);

            log.debug("Root has children: " + root.childrenTaxIds.length);
            printChildren(root, 0, taxonomyMap);*/

        } catch (IOException e) {
            log.error("Error while parsing taxonomy files", e);
        }
    }
}


