package hr.pbf.digestdb.rocksdb;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class MainExportSortedDackDb {
    public String toCsvPath = "";
    public String fromDbUrl = "";

    public void start() throws ClassNotFoundException {
        Class.forName("org.duckdb.DuckDBDriver"); // Load the driver


        try (
              Connection connection = DriverManager.getConnection(fromDbUrl);
              java.sql.Statement statement = connection.createStatement()) {

            String sqlExportCsv = """
                  COPY (SELECT * FROM peptides ORDER BY mass, "sequence")
                  TO '%s' (HEADER, DELIMITER ',');""".formatted(toCsvPath);

            String sqlExportParquet = """
                  COPY (SELECT * FROM peptides ORDER BY mass, "sequence")
                  TO '..../csv/peptide_mass_sorted.parquet' (FORMAT 'parquet');
                  """;

            statement.execute(sqlExportCsv);
            log.debug("Copy done");


        } catch (SQLException e) {
            log.error("SQL error: " + e.getMessage());
        }
    }
}
