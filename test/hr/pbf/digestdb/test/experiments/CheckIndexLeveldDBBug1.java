package hr.pbf.digestdb.test.experiments;

import hr.pbf.digestdb.uniprot.UniprotLevelDbFinder;
import hr.pbf.digestdb.uniprot.UniprotModel;
import hr.pbf.digestdb.uniprot.UniprotUtil;
import hr.pbf.digestdb.util.BioUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class CheckIndexLeveldDBBug1 {
    private static final Logger log = LoggerFactory.getLogger(CheckIndexLeveldDBBug1.class);

    public static void main(String[] args) throws IOException {
        UniprotLevelDbFinder finder = new UniprotLevelDbFinder();
        UniprotLevelDbFinder.IndexResult index = finder.searchIndex(400, 501);
        printFirst(index);
        log.debug("index min: {} max: {}", index.map.firstKey(), index.map.lastKey());


        { // leveldb
            List<UniprotModel.PeptideAccTaxNames> result = finder.searchMass(400, 501, 6);
            log.debug("Dobio konacno");
            printFirst(result);
        }

    }

    private static void printFirst(List<UniprotModel.PeptideAccTaxNames> result) {

        if(result.isEmpty()) {
            log.debug("Emtpy resutl mass search");
        }
        Map<String, List<UniprotModel.PeptideAccTaxNames>> group = UniprotUtil.groupByPeptide(result);
        Set<Map.Entry<String, List<UniprotModel.PeptideAccTaxNames>>> entries = group.entrySet();
        int c = 0;
        for (Map.Entry<String, List<UniprotModel.PeptideAccTaxNames>> entry : entries) {
            List<UniprotModel.PeptideAccTaxNames> value = entry.getValue();
            log.debug((float)BioUtil.calculateMassWidthH2O(entry.getKey())+"\t"+ value.size());

            if(c++ > 5) {
                return;
            }
        }
    }

    private static void printFirst(UniprotLevelDbFinder.IndexResult index) {
        SortedMap<Float, Integer> map = index.map;
        Float aFloat = map.firstKey();
        log.debug(aFloat + "\t"+ map.get(aFloat));
        Set<Map.Entry<Float, Integer>> entries = map.entrySet();
        int i = 0;
        for (Map.Entry<Float, Integer> entry : entries) {
            log.debug(entry.getKey() + "\t"+ entry.getValue());
            if( i++ > 5 ) {
                return;
            }
        }

    }
}
