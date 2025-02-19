package hr.pbf.digestdb.workflow;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class MainCsvToDackdb {
    public String fromCsvPath = "";
    public String toDbUrl = "";
    public int maxPeptideLength = 30;
    public int maxaAcessionIdLength=12;

    public void start() throws ClassNotFoundException {

        Class.forName("org.duckdb.DuckDBDriver"); // Load the driver

        if(!new File(fromCsvPath).exists()){
            throw new RuntimeException("File not found: " + fromCsvPath);
        }

        if(new File(toDbUrl).exists()){
            throw new RuntimeException("File already exists: " + toDbUrl);
        }

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
            sql = sql.replace("{accessionLength}", String.valueOf(maxaAcessionIdLength));
            statement.execute(sql);


            String sqlCopy = "COPY peptides FROM '" + fromCsvPath + "' (HEADER TRUE, DELIMITER ',')";

            statement.execute(sqlCopy);
            log.debug("Copy csv to duckdb done!");

        } catch (SQLException e) {
            log.error("SQL error: " + e.getMessage(), e);
        }
    }

}
