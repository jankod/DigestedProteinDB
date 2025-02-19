package hr.pbf.digestdb.workflow;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class MainExportSortedDackDb {
    public String toCsvPath = "";
    public String fromDbUrl = "";

    public void start() throws ClassNotFoundException {
        Class.forName("org.duckdb.DuckDBDriver"); // Load the driver

        if (new java.io.File(toCsvPath).exists()) {
            throw new RuntimeException("File already exists: " + toCsvPath);
        }
        if (new java.io.File(fromDbUrl).exists()) {
            throw new RuntimeException("File not found: " + fromDbUrl);
        }

        try (
              Connection connection = DriverManager.getConnection(fromDbUrl);
              java.sql.Statement statement = connection.createStatement()) {

            try {
                Statement stIOndex = connection.createStatement();
                stIOndex.execute("CREATE INDEX peptides_mass_IDX ON peptides (mass)");
                stIOndex.close();
                log.info("Index is created");
            } catch (Exception e) {
                log.error("Index is not created", e);
            }

            String sqlExportCsv = """
                  COPY (SELECT * FROM peptides ORDER BY mass, "sequence")
                  TO '%s' (HEADER, DELIMITER ',');""".formatted(toCsvPath);

            statement.execute(sqlExportCsv);
            log.debug("Copy done");


            // sort -t',' -k1n peptide_mass.csv -o peptide_mass_sorted_console.csv

        } catch (SQLException e) {
            log.error("SQL error: " + e.getMessage(), e);
        }
    }

    private void startExposrtIntoChunks() throws ClassNotFoundException, SQLException {
        Class.forName("org.duckdb.DuckDBDriver"); // Load the driver

        if (new java.io.File(toCsvPath).exists()) {
            throw new RuntimeException("File already exists: " + toCsvPath);
        }
        if (new java.io.File(fromDbUrl).exists()) {
            throw new RuntimeException("File not found: " + fromDbUrl);
        }

        try (
              Connection connection = DriverManager.getConnection(fromDbUrl);
              java.sql.Statement statement = connection.createStatement()) {
            int CHUNK_SIZE = 200_000;

            // Get total row count in one query
            int totalCount;
            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM peptides")) {
                rs.next();
                totalCount = rs.getInt(1);
            }

            for (int offset = 0; offset < totalCount; offset += CHUNK_SIZE) {
                String sqlChunkCopy = """
                      COPY (SELECT * FROM peptides ORDER BY mass, "sequence" LIMIT %d OFFSET %d)
                      TO '%s'
                      (HEADER, DELIMITER ',', APPEND);
                      """.formatted(CHUNK_SIZE, offset, toCsvPath);

                statement.execute(sqlChunkCopy);
                log.debug("Exported chunk with offset " + offset);
            }
            log.debug("Copy done in chunks");
        }
    }
}
