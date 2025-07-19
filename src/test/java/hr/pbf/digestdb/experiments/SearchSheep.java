package hr.pbf.digestdb.experiments;

import hr.pbf.digestdb.db.MassRocksDbReader;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SearchSheep {

    public static void main(String[] args) throws RocksDBException {
        MassRocksDbReader db = new MassRocksDbReader("/home/tag/IdeaProjects/DigestedProteinDB/misc/db/sheep");
        db.open();

        List<Double> sheepMasses = getMassesSheep("/media/tag/D/digested-db/Trypsin_HTXdigest-ovca.txt");
        log.debug("Sheep masses: " + sheepMasses);

        for (Double mass : sheepMasses) {
            // margina 0.02
            double mass1 = mass - 0.02;
            double mass2 = mass + 0.02;
            List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> peptides = db.searchByMassPaginated(mass1, mass2);

        }

    }

    private static List<Double> getMassesSheep(String path) {
        try (BufferedReader reader = java.nio.file.Files.newBufferedReader(java.nio.file.Paths.get(path))) {
            return reader.lines()
                    .map(line -> line.split("\t")[0])
                    .map(Double::parseDouble)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Error reading masses from file: " + path, e);
        }
    }
}
