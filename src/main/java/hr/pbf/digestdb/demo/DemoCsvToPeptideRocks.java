package hr.pbf.digestdb.demo;

import gnu.trove.map.hash.TObjectIntHashMap;
import hr.pbf.digestdb.util.MyUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DemoCsvToPeptideRocks {

    public static void main(String[] args) {

//        mass,sequence,accession,taxonomy_id
        String csv = """
              
              331.14917543984376,GGGAA,Q9K153,122586
              331.14917543984376,GGGAA,Q5F694,242231
              331.14917543984376,GGGAA,A1ITY1,122587
              333.1284540042969,SGGGG,Q1AU71,266117
              333.1284540042969,SGGGG,P23692,9606
              345.1648309574219,AAGGA,B4R8T3,450851
              345.1648309574219,AGAGA,B1W3Z0,455632
              345.1648309574219,AGAGA,Q9L0C3,100226
              345.1648309574219,AGAGA,Q82DM9,227882
              345.1648309574219,AGGAA,B7X2B6,399795
              345.1648309574219,GAAAG,P18080,9031
              345.1648309574219,GAAGA,Q9RSQ3,243230
              345.1648309574219,GGVGG,Q4WDH9,330879
              """;
        DemoCsvToPeptideRocks d = new DemoCsvToPeptideRocks();
        d.addRow(331.14917543984376, "GGGAA", "Q9K153", 122586);
        d.addRow(331.14917543984376, "GGGAA", "Q5F694", 242231);
        d.addRow(331.14917543984376, "GGGAA", "A1ITY1", 122587);
        d.addRow(333.1284540042969, "SGGGG", "Q1AU71", 266117);
        d.addRow(333.1284540042969, "SGGGG", "P23692", 9606);
        d.addRow(345.1648309574219, "AAGGA", "B4R8T3", 450851);
        d.addRow(345.1648309574219, "AGAGA", "B1W3Z0", 455632);

        /*
        331.1491 -> {GGGAA : 1, GGGAA : 2, GGGAA : 3},
        333.1284 -> {SGGGG : 4, SGGGG : 2},
        345.1648 -> {AAGGA : 5, AGAGA : 6}
         */


    }


    Map<String, Set<Integer>> currentPeptides = new HashMap<>();
    //   TIntObjectHashMap<String> intAccessionMap = new TIntObjectHashMap<>();
    TObjectIntHashMap<String> accessionIntMap = new TObjectIntHashMap<>();

    double currentMass4 = 0;

    private void addRow(double mass, String seq, String acc, int taxonomyId) {

        int accInt;
        // mapp acc to int
        if (accessionIntMap.containsKey(acc)) {
            accInt = accessionIntMap.get(acc);
        } else {
            accInt = accessionIntMap.size() + 1;
            accessionIntMap.put(acc, accInt);
        }


        double mass4 = MyUtil.roundTo4(mass);
        if (mass4 != currentMass4) {
            currentPeptides.clear();
            currentMass4 = mass4;
        }

    }
}
