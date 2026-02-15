package hr.pbf.digestdb.tools;

import hr.pbf.digestdb.util.UniprotXMLParser;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class UniprotToParquet {
    private static final int BATCH_SIZE = 10_000;

    public static void main(String[] args) throws XMLStreamException, IOException {

        String swisprotXml = args.length > 0 ? args[0] : "/home/tag/Downloads/uniprot_sprot.xml.gz";
        String outputParquet = args.length > 1 ? args[1] : "/home/tag/IdeaProjects/DigestedProteinDB/misc/db/proteins.parquet";

        // TaxID -> TaxonomyNode
        //   Map<Integer, TaxonomyParser.TaxonomyNode> tax = TaxonomyParser.parseNodes("/misc/ncbi/taxdump/nodes.dmp");
        // Use only rank ∈ {species, subspecies, strain, varietas, forma}


        //      FilterBioType filterBioType = new FilterBioType(tax);

        StopWatch stopWatch = StopWatch.createStarted();
        final long[] c = {0};
        final int[] pendingBatchRows = {0};

        try (Connection connection = DriverManager.getConnection("jdbc:duckdb:");
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            statement.execute("""
                    CREATE TABLE proteins (
                        accession VARCHAR,
                        protein_name VARCHAR,
                        taxonomy_id INTEGER,
                        division_id INTEGER,
                        sequence VARCHAR
                    )
                    """);

            try (PreparedStatement insertProtein = connection.prepareStatement("""
                    INSERT INTO proteins (accession, protein_name, taxonomy_id, division_id, sequence)
                    VALUES (?, ?, ?, ?, ?)
                    """)) {
                UniprotXMLParser parser = new UniprotXMLParser();
                parser.parseProteinsFromXMLstream(swisprotXml, new UniprotXMLParser.ProteinHandler() {
                    @Override
                    public void gotProtein(UniprotXMLParser.ProteinInfo p) throws IOException {
                        try {
                            insertProtein.setString(1, p.getAccession());
                            insertProtein.setString(2, p.getProteinName());
                            insertProtein.setInt(3, p.getTaxonomyId());
                            insertProtein.setInt(4, p.getDivisionId());
                            insertProtein.setString(5, p.getSequence());
                            insertProtein.addBatch();

                            pendingBatchRows[0]++;
                            c[0]++;

                            if (pendingBatchRows[0] >= BATCH_SIZE) {
                                insertProtein.executeBatch();
                                connection.commit();
                                pendingBatchRows[0] = 0;
                            }
                        } catch (SQLException e) {
                            throw new IOException("Failed to insert protein into DuckDB", e);
                        }
                    }
                });

                if (pendingBatchRows[0] > 0) {
                    insertProtein.executeBatch();
                    connection.commit();
                }
            }

            String parquetPath = Path.of(outputParquet).toAbsolutePath().toString().replace("'", "''");
            statement.execute("COPY proteins TO '" + parquetPath + "' (FORMAT PARQUET)");
        } catch (SQLException e) {
            throw new IOException("Failed to write proteins to parquet", e);
        }

        stopWatch.stop();
        System.out.println(DurationFormatUtils.formatDurationHMS(stopWatch.getTime()) + " " + c[0] + " -> " + outputParquet);

    }
}
