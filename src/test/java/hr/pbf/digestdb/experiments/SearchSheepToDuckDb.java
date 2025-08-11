package hr.pbf.digestdb.experiments;

import hr.pbf.digestdb.db.AccessionDbReader;
import hr.pbf.digestdb.db.MassRocksDbReader;
import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import hr.pbf.digestdb.util.AccTaxDB;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.ConsoleProgress;
import hr.pbf.digestdb.util.MyStopWatch;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.duckdb.DuckDBAppender;
import org.duckdb.DuckDBConnection;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

import static hr.pbf.digestdb.experiments.SearchSheep.getMassesSheep;

@Slf4j
public class SearchSheepToDuckDb {

    static NCBITaxaEte ncbi;
    static AccessionDbReader accessionDbReader;
    static String duckDbPath = "/home/tag/IdeaProjects/DigestedProteinDB/misc/db/sheep_hits.duckdb"; // postavi željeni path

    //String dbDir = "/home/tag/IdeaProjects/DigestedProteinDB/misc/db/sheep2/";
    //dbDir = "/Users/tag/PBF radovi/digestedproteindb/sheep/rocksdb_mass.db"; // For testing on local machine
    static String dbDir = "/home/tag/IdeaProjects/DigestedProteinDB/misc/db/trembl/";

    public static void exportResult() throws SQLException, IOException {
        //accessionDbReader = new AccessionDbReader(dbDir + "custom_accession.db");
        //AccTaxDB accessionTaxDb = AccTaxDB.loadFromDiskCsv(dbDir + "/acc_taxid.csv");
        ncbi = new NCBITaxaEte();
        String sql = """
                  SELECT taxid,
                         COUNT(DISTINCT accid)   AS accid_count,
                         COUNT(DISTINCT peptide) AS peptide_count
                  FROM hits
                  GROUP BY taxid
                  ORDER BY peptide_count DESC
              """;

        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + duckDbPath);
             Statement st = conn.createStatement()) {
            st.execute("PRAGMA threads=6");
            st.execute("PRAGMA memory_limit='38GB'");

            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            try (BufferedWriter writer = Files.newBufferedWriter(Path.of(duckDbPath + "_result.csv"))) {

                while (rs.next()) {
                    int taxid = rs.getInt("taxid");
                    int accidCount = rs.getInt("accid_count");
                    int peptideCount = rs.getInt("peptide_count");
                    String taxName = ncbi.getBestName(taxid);

                    // ovdje možeš raditi analizu po jednom taxid-u
                    //System.out.printf("%d\t%d\t%d%n", taxid, accidCount, peptideCount);
                    writer.write(taxid + "," + taxName + "," + accidCount + "," + peptideCount + "\n");
                }
            }
            log.info("Results exported to: " + duckDbPath + "_result.csv");

        }
    }

    public static void main(String[] args) throws SQLException, IOException {
        exportResult();
    }

    public static void main1(String[] args) throws
          RocksDBException, NcbiTaxonomyException, IOException, SQLException {


        String pathSheep = "/media/tag/D/digested-db/Trypsin_HTXdigest-ovca.txt";
        //pathSheep = "'/Users/tag/PBF radovi/digestedproteindb'/Trypsin_HTXdigest_sheep_butorka.txt";

        String pathToNodesDmp = "/home/tag/IdeaProjects/DigestedProteinDB/misc/ncbi/taxdump/nodes.dmp";

        MyStopWatch stopWatch = new MyStopWatch();
        MassRocksDbReader db = new MassRocksDbReader(dbDir + "rocksdb_mass.db");
        db.open();
        log.debug("RocksDB opened in: " + stopWatch.getCurrentDuration());

        accessionDbReader = new AccessionDbReader(dbDir + "custom_accession.db");
        log.debug("Accession DB opened in: " + stopWatch.getCurrentDuration());
        //NcbiTaksonomyRelations taxonomy = NcbiTaksonomyRelations.loadTaxonomyNodes(pathToNodesDmp);
        ncbi = new NCBITaxaEte();
        log.debug("NCBITaxaEte opened in: " + stopWatch.getCurrentDuration());


        AccTaxDB accessionTaxDb = AccTaxDB.loadFromDiskCsv(dbDir + "/acc_taxid.csv");
        log.debug("AccTaxDB loaded in: " + stopWatch.getCurrentDuration());


        List<Double> sheepMasses = getMassesSheep(pathSheep);
        log.debug("Sheep masses loaded in: " + stopWatch.getCurrentDuration());
        log.debug("Sheep masses: " + sheepMasses.size());


        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + duckDbPath);


             Statement st = conn.createStatement()) {

            st.execute("PRAGMA threads=6");
            st.execute("PRAGMA memory_limit='8GB'");

            st.execute("""
                  CREATE TABLE IF NOT EXISTS hits (
                    peptide text NOT NULL,
                    taxid   integer NOT NULL,
                    accid   integer NOT NULL
                  )
                  """);

            DuckDBConnection duck = conn.unwrap(DuckDBConnection.class);
            try (DuckDBAppender app = duck.createAppender(null, "hits")) {
                // ... tvoja petlja ...

                // za svaki red:
                int countMass = 0;
                int rows = 0;

                for (Double mass : sheepMasses) {
                    double mass1 = mass - 0.02;
                    double mass2 = mass + 0.02;

                    List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> peptides = db.searchByMass(mass1, mass2);
                    if (countMass++ % 10 == 0) {
                        ConsoleProgress.setProgress(countMass, sheepMasses.size(), "Searching peptides for mass: " + mass);
                    }

                    for (Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>> entry : peptides) {
                        for (BinaryPeptideDbUtil.PeptideAccids peptideAccids : entry.getValue()) {
                            String peptide = peptideAccids.getSeq();
                            int[] accIds = peptideAccids.getAccids();
                            for (int accIdInt : accIds) {
                                String acc = accessionDbReader.getAccession(accIdInt);
                                int taxId = accessionTaxDb.getTaxonomyId(acc);

                                app.beginRow();
                                app.append(peptide);   // TEXT
                                app.append(taxId);     // INT
                                app.append(accIdInt);  // INT
                                app.endRow();
                                // možeš raditi periodični conn.commit() svakih n redova:
                            }
                        }
                    }
                }
            }
        }
    }
}
