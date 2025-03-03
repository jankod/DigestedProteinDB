package hr.pbf.digestdb.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Data
@Slf4j
public class WorkflowConfig {
    private final String dbDir;

    int minPeptideLength;
    int maxPeptideLength;
    int missCleavage;
    String dbName;
    String enzymeName;
    String uniprotXmlPath;
    String sortTempDir;


    public WorkflowConfig(String dbDir) throws IOException {
        this.dbDir = dbDir;
        String workflofPath = dbDir + "/workflow.properties";
        log.debug("Load config: {}", workflofPath);
        Config config = ConfigFactory.parseFile(new File(workflofPath));

        // uniprot_xml_path=src/uniprot_sprot_bacteria.xml.gz
        //min_peptide_length=7
        //max_peptide_length=30
        //miss_cleavage=1
        //db_name=Uniprot Swis-Prot bacteria
        //enzyme_name=Trypsine
        //#sort_temp_dir=/disk4/janko/temp_dir

        minPeptideLength = config.getInt("min_peptide_length");
        if (minPeptideLength < 1) {
            throw new IllegalArgumentException("min_peptide_length must be > 0");
        }
        maxPeptideLength = config.getInt("max_peptide_length");
        if (maxPeptideLength < 1) {
            throw new IllegalArgumentException("max_peptide_length must be > 0");
        }
        if (minPeptideLength > maxPeptideLength) {
            throw new IllegalArgumentException("min_peptide_length must be < max_peptide_length");
        }

        missCleavage = config.getInt("miss_cleavage");
        if (missCleavage < 0) {
            throw new IllegalArgumentException("miss_cleavage must be >= 0");
        }
        if (missCleavage != 1) {
            throw new IllegalArgumentException("miss_cleavage must be 1");
        }
        dbName = config.getString("db_name");
        enzymeName = config.getString("enzyme_name");
        uniprotXmlPath = config.getString("uniprot_xml_path");

        sortTempDir = config.hasPath("sort_temp_dir") ? config.getString("sort_temp_dir") : null;
        if(sortTempDir != null && !new File(sortTempDir).isDirectory()) {
            throw new IllegalArgumentException("sort_temp_dir is not a directory: " + sortTempDir);
        }
    }

    public String toUniprotXmlFullPath() {
        return dbDir + "/" + uniprotXmlPath;
    }

    public static void main(String[] args) throws IOException {
        WorkflowConfig config = new WorkflowConfig("/Users/tag/IdeaProjects/DigestedProteinDB/misc/db_bacteria_swisprot");
    }

    final static DateTimeFormatter dateTimeFormater = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");


    @SneakyThrows
    public Properties getDbInfo() {
        Properties prop = new Properties();
        try (FileReader reader = new FileReader(dbDir + "/db_info.properties")) {
            prop.load(reader);
        }
        return prop;
    }

    public void saveDbInfoProperties(long proteinCount, String dbInfoPropertiesPath) {
        Properties prop = new Properties();

        prop.setProperty("uniprot_xml_path", toUniprotXmlFullPath());
        prop.setProperty("min_peptide_length", String.valueOf(getMinPeptideLength()));
        prop.setProperty("max_peptide_length", String.valueOf(getMaxPeptideLength()));
        prop.setProperty("miss_cleavage", String.valueOf(getMissCleavage()));
        prop.setProperty("db_name", dbName);
        prop.setProperty("enzyme_name", enzymeName);
        prop.setProperty("protein_count", proteinCount + "");

        try {
            try (FileWriter writer = new FileWriter(dbInfoPropertiesPath)) {
                prop.store(writer, "Date " + dateTimeFormater.format(LocalDateTime.now()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
