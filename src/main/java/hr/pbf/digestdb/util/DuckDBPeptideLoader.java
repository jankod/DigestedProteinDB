package hr.pbf.digestdb.util;

import java.sql.*;
import java.io.*;

public class DuckDBPeptideLoader {
    public static void main(String[] args) {
        String csvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated/grouped_with_ids.csv";
        String dbPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated/peptides_grouped.duckdb";

        // Učitaj CSV u DuckDB
      //  loadCsvIntoDuckDB(csvPath, dbPath);

        // Primjer range upita
        performRangeQuery(dbPath, 431.0, 445.2);
    }

    private static void loadCsvIntoDuckDB(String csvPath, String dbPath) {
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath)) {
            Statement stmt = conn.createStatement();

            // Kreiraj tablicu
            stmt.execute("CREATE TABLE peptides (mass DOUBLE, peptides VARCHAR)");

            // Učitaj CSV direktno u tablicu
            // DuckDB podržava učitavanje CSV-a s auto-detekcijom separatora
            String loadQuery = String.format(
                "COPY peptides FROM '%s' (DELIMITER ',', HEADER FALSE)",
                csvPath.replace("\\", "\\\\") // Escape za Windows putanje ako je potrebno
            );
            stmt.execute(loadQuery);

            // Kreiraj indeks na mass za brže pretraživanje
            stmt.execute("CREATE INDEX idx_mass ON peptides (mass)");

            System.out.println("CSV successfully loaded into DuckDB.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void performRangeQuery(String dbPath, double massFrom, double massTo) {
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT mass, peptides FROM peptides WHERE mass BETWEEN ? AND ? ORDER BY mass")) {
            pstmt.setDouble(1, massFrom);
            pstmt.setDouble(2, massTo);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("Range query results (" + massFrom + " to " + massTo + "):");
            while (rs.next()) {
                double mass = rs.getDouble("mass");
                String peptides = rs.getString("peptides");
                System.out.printf("Mass: %.4f, Peptides: %s%n", mass, peptides);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
