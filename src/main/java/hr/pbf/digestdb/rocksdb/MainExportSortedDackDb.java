package hr.pbf.digestdb.rocksdb;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class MainExportSortedDackDb {
    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("org.duckdb.DuckDBDriver"); // Load the driver

        Properties readOnlyProperty = new Properties();
        readOnlyProperty.setProperty("duckdb.read_only", "true");
        String url = "jdbc:duckdb:/Users/tag/IdeaProjects/DigestedProteinDB/misc/duck_db/mass_db";

        try (
              Connection connection = DriverManager.getConnection(url);
              java.sql.Statement statement = connection.createStatement()) {

            String sqlExportCsv = """
                  COPY (SELECT * FROM peptides ORDER BY mass, "sequence")
                  TO '/Users/tag/IdeaProjects/DigestedProteinDB/misc/csv/peptide_mass_sorted.csv' (HEADER, DELIMITER ',');""";

            String sqlExportParquet = """
                  COPY (SELECT * FROM peptides ORDER BY mass, "sequence")
                  TO '/Users/tag/IdeaProjects/DigestedProteinDB/misc/csv/peptide_mass_sorted.parquet' (FORMAT 'parquet');
                  """;
            statement.execute(sqlExportCsv);
            log.debug("Copy done");


        } catch (SQLException e) {
            log.error("SQL error: " + e.getMessage());
        }
    }
}
