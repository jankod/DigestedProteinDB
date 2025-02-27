package hr.pbf.digestdb.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

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

    public void WorkflowConfigOLD(String dbDir) throws IOException {
        File fileDbDir = new File(dbDir);
        if (!FileUtils.isDirectory(fileDbDir)) {
            throw new FileNotFoundException("Not a directory: " + dbDir);
        }

        File fileWorkflowProperties = new File(dbDir + "/workflow.properties");
        if (!fileWorkflowProperties.exists()) {
            throw new FileNotFoundException("File not found: " + dbDir + "/workflow.properties");
        }

        Properties properties = new Properties();

        try (FileReader reader = new FileReader(fileWorkflowProperties)) {
            properties.load(reader);
            minPeptideLength = Integer.parseInt(properties.getProperty("min_peptide_length"));
            maxPeptideLength = Integer.parseInt(properties.getProperty("max_peptide_length"));
            missCleavage = Integer.parseInt(properties.getProperty("miss_cleavage"));
            dbName = properties.getProperty("db_name");
            enzymeName = properties.getProperty("enzyme_name");
            uniprotXmlPath = properties.getProperty("uniprot_xml_path");
            sortTempDir = properties.getProperty("sort_temp_dir");
        }
    }

    public String toUniprotXmlFullPath() {
        return dbDir + "/" + uniprotXmlPath;
    }

    public static void main(String[] args) throws IOException {
        WorkflowConfig config = new WorkflowConfig("/Users/tag/IdeaProjects/DigestedProteinDB/misc/db_bacteria_swisprot");
    }

//    public DbInfo getDbInfo() {
//        hr.pbf.digestdb.model.DbInfo dbInfo = new DbInfo();
//        dbInfo.setMissCleavage(String.valueOf(missCleavage));
//        dbInfo.setMinPeptideLength(String.valueOf(minPeptideLength));
//        dbInfo.setMaxPeptideLength(String.valueOf(maxPeptideLength));
//        dbInfo.setDbName(dbName);
//        dbInfo.setEnzimeName(enzymeName);
//        return dbInfo;
//    }


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
