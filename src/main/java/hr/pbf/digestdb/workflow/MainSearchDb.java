package hr.pbf.digestdb.workflow;

import org.apache.commons.lang3.time.StopWatch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MainSearchDb {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {


        String csvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/csv/peptide_mass_sorted_orig.csv";

        Class.forName("org.duckdb.DuckDBDriver"); // Load the driver

        Properties readOnlyProperty = new Properties();
        readOnlyProperty.setProperty("duckdb.read_only", "true");
        String url = "jdbc:duckdb:/Users/tag/IdeaProjects/DigestedProteinDB/misc/duck_db/mass_db";
        String urlParquet = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/csv/peptide_mass_sorted.parquet";

        Connection connection = DriverManager.getConnection(url);
        java.sql.Statement statement = connection.createStatement();

      //  statement.execute("CREATE INDEX mass_idx ON peptides (mass)");


        // duration
        String sql = "SELECT * FROM peptides WHERE mass BETWEEN 700.0 AND 700.3";
        StopWatch s = StopWatch.createStarted();
        java.sql.ResultSet resultSet = statement.executeQuery(sql);
        s.stop();
        System.out.println("Duration: " + s.getTime() + " ms");
        if (!resultSet.next()) {
            System.out.println("No results found");
        }
        while (resultSet.next()) {
            double mass = resultSet.getDouble("mass");
            String sequence = resultSet.getString("sequence");
            String accession = resultSet.getString("accession");
            System.out.println("Mass: " + mass + ", Sequence: " + sequence + ", Accession: " + accession);
        }

    }
}
