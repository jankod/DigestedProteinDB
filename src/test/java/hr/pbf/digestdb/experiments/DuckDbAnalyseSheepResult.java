package hr.pbf.digestdb.experiments;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DuckDbAnalyseSheepResult {

    public static void main(String[] args) throws SQLException {

        String inputCsvPath = "/home/tag/result_sheep/peptides_hits_sheep.csv";

        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:");
             Statement st = conn.createStatement()) {

            // Tuning pragmas
            st.execute("PRAGMA threads=8");
            //   st.execute("PRAGMA memory_limit='" + mem + "'");
//            st.execute("PRAGMA temp_directory='" + tempDir.replace("'", "''") + "'");

            // Ingest CSV (out-of-core). SAMPLE_SIZE=-1 scans full file for typing.
            String createSql = """
                  CREATE OR REPLACE TABLE hits AS
                  SELECT * FROM read_csv(
                    ?,
                    delim=',',
                    header="",
                    sample_size=-1
                  );
                  """;

            try (var ps = conn.prepareStatement(createSql)) {
                ps.setString(1, inputCsvPath);
                ps.execute();
            }
        }
    }
}
