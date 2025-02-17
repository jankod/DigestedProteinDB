package hr.pbf.digestdb.rocksdb;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class MainCsvToDackdb {
    public String fromCsvPath = "";
    public String toDbUrl = "";
    public int maxPeptideLength = 30;

    public void start() throws ClassNotFoundException {

        Class.forName("org.duckdb.DuckDBDriver"); // Load the driver

        try (Connection connection = DriverManager.getConnection(toDbUrl);
             java.sql.Statement statement = connection.createStatement()) {

            // Enable parallel processing. Adjust thread count as needed.
            statement.execute("PRAGMA threads=8");
            String sql = """
                  CREATE TABLE IF NOT EXISTS peptides (
                  mass DOUBLE, 
                  sequence VARCHAR({sequenceLength}), 
                  accession VARCHAR({accessionLength}), 
                  taxonomy_id INT)
                  """;
            sql = sql.replace("{sequenceLength}", String.valueOf(maxPeptideLength + 1));
            sql = sql.replace("{accessionLength}", String.valueOf(12));
            statement.execute(sql);


            String sqlCopy = "COPY peptides FROM '" + fromCsvPath + "' (HEADER TRUE, DELIMITER ',')";

            statement.execute(sqlCopy);
            log.debug("Copy csv to duckdb done!");

        } catch (SQLException e) {
            log.error("SQL error: " + e.getMessage(), e);
        }
    }

}
