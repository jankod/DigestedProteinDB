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
        String dbPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/duck_db/mass_db";

        Class.forName("org.duckdb.DuckDBDriver"); // Load the driver

        Properties readOnlyProperty = new Properties();
        readOnlyProperty.setProperty("duckdb.read_only", "false");
        String url = "jdbc:duckdb:/Users/tag/IdeaProjects/DigestedProteinDB/misc/duck_db/mass_db";

        try (Connection connection = DriverManager.getConnection(url);
             java.sql.Statement statement = connection.createStatement()) {
// CREATE INDEX mass_idx ON peptides (mass);

            // COPY peptides FROM 'path/do/your/peptide_data.csv' (HEADER, DELIMITER ',');


            statement.execute("CREATE TABLE IF NOT EXISTS peptides (mass DOUBLE, sequence VARCHAR(200), accession VARCHAR(14))");
            // statement.execute("INSERT INTO peptides VALUES (100.5, 'PEPTIDE1', 'ACC1'), (120.7, 'PEPTIDE2', 'ACC2')");

            String sqlCopy = "COPY peptides FROM '" + csvPath + "' (HEADER, DELIMITER ',')";
            statement.execute(sqlCopy);
            log.debug("Copy done");

           /* java.sql.ResultSet resultSet = statement.executeQuery("SELECT * FROM peptides WHERE mass BETWEEN 100 AND 130");
            while (resultSet.next()) {
                double mass = resultSet.getDouble("mass");
                String sequence = resultSet.getString("sequence");
                String accession = resultSet.getString("accession");
                System.out.println("Mass: " + mass + ", Sequence: " + sequence + ", Accession: " + accession);
            }*/

        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

}
