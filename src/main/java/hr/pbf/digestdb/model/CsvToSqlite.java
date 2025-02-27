package hr.pbf.digestdb.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

@Slf4j
@Data
public class CsvToSqlite {

    public static void main(String[] args) {
        String csvFilePath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/ncbi_taxonomy/taxdump/digested_taxonomy.csv";
        String dbFilePath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/taxonomy_sqllite.db";
        HashSet<String> uniqueRank = new HashSet<>();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath)) {
            connection.createStatement().execute(
                  """
                        CREATE TABLE IF NOT EXISTS taxonomy (\
                        taxId INTEGER PRIMARY KEY,\
                        parentTaxId INTEGER,\
                        rank TEXT,\
                        name TEXT,\
                        divisionId INTEGER\
                        )""");

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                  "INSERT INTO taxonomy (taxId, parentTaxId, rank, name, divisionId) VALUES (?, ?, ?, ?, ?)")) {

                BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
                String line = null;
                reader.readLine(); // skip header
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split("\t");

                    try {
                        int taxId = Integer.parseInt(data[0]);
                        int parentTaxId = Integer.parseInt(data[1]);
                        String rank = data[2];
                        uniqueRank.add(rank);
                        String name = data[3];
                        int divisionId = Integer.parseInt(data[4]);

                        preparedStatement.setInt(1, taxId);
                        preparedStatement.setInt(2, parentTaxId);
                        preparedStatement.setString(3, rank);
                        preparedStatement.setString(4, name);
                        preparedStatement.setInt(5, divisionId);
                        preparedStatement.executeUpdate();

                    } catch (Exception e) {
                        log.error("Error on line: "+ line, e);
                    }
                }
                log.info("Unique ranks: " + uniqueRank);
                log.debug("CSV data inserted into SQLite database successfully.");
            }

        } catch (SQLException | IOException e) {
            log.error("Error: ", e);
        }
    }
}
