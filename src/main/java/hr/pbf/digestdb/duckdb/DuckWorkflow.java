package hr.pbf.digestdb.duckdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DuckWorkflow {
    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("org.duckdb.DuckDBDriver"); // Load the driver
        String url = "jdbc:duckdb:"; // In-memory database

        Properties readOnlyProperty = new Properties();
        readOnlyProperty.setProperty("duckdb.read_only", "true");
        //Connection conn = DriverManager.getConnection("jdbc:duckdb:/tmp/my_database", readOnlyProperty);
        url = "jdbc:duckdb:/Users/tag/IdeaProjects/DigestedProteinDB/misc/duck_db/mass_db";

        try (Connection connection = DriverManager.getConnection(url);
             java.sql.Statement statement = connection.createStatement()) {
// CREATE INDEX mass_idx ON peptides (mass);

            // COPY peptides FROM 'path/do/your/peptide_data.csv' (HEADER, DELIMITER ',');


            statement.execute("CREATE TABLE peptides (mass DOUBLE, sequence VARCHAR(), accession VARCHAR)");
            statement.execute("INSERT INTO peptides VALUES (100.5, 'PEPTIDE1', 'ACC1'), (120.7, 'PEPTIDE2', 'ACC2')");

            java.sql.ResultSet resultSet = statement.executeQuery("SELECT * FROM peptides WHERE mass BETWEEN 100 AND 130");
            while (resultSet.next()) {
                double mass = resultSet.getDouble("mass");
                String sequence = resultSet.getString("sequence");
                String accession = resultSet.getString("accession");
                System.out.println("Mass: " + mass + ", Sequence: " + sequence + ", Accession: " + accession);
            }

        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

}
