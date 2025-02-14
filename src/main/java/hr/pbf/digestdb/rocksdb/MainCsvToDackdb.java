package hr.pbf.digestdb.rocksdb;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class MainCsvToDackdb {
    public static void main(String[] args) throws ClassNotFoundException {
        String csvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/csv/peptide_mass.csv";

        Class.forName("org.duckdb.DuckDBDriver"); // Load the driver

        String url = "jdbc:duckdb:/Users/tag/IdeaProjects/DigestedProteinDB/misc/duck_db/mass_db";

        try (Connection connection = DriverManager.getConnection(url);
             java.sql.Statement statement = connection.createStatement()) {

            statement.execute("""
                  CREATE TABLE IF NOT EXISTS peptides (mass DOUBLE, sequence VARCHAR(200), accession VARCHAR(14), taxonomy_id INT)
                  """);

            String sqlCopy = "COPY peptides FROM '" + csvPath + "' (HEADER TRUE, DELIMITER ',')";

            statement.execute(sqlCopy);
            log.debug("Copy done");

        } catch (SQLException e) {
            log.error("SQL error: " + e.getMessage(), e);
        }
    }

}
