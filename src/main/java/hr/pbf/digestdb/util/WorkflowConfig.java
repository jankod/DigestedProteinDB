package hr.pbf.digestdb.util;

import hr.pbf.digestdb.model.DbInfo;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

@Data
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
        Properties properties = new Properties();
        try (FileReader reader = new FileReader(dbDir + "/workflow.properties")) {
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

    public void produceDbInfo(long proteinCount) {
        Properties prop = new Properties();

        prop.setProperty("uniprot_xml_path", toUniprotXmlFullPath());
        prop.setProperty("min_peptide_length", String.valueOf(getMinPeptideLength()));
        prop.setProperty("max_peptide_length", String.valueOf(getMaxPeptideLength()));
        prop.setProperty("miss_cleavage", String.valueOf(getMissCleavage()));
        prop.setProperty("db_name", dbName);
        prop.setProperty("enzyme_name", enzymeName);
        prop.setProperty("protein_count", proteinCount + "");

        try {
            try (FileWriter writer = new FileWriter(dbDir + "/db_info.properties")) {
                prop.store(writer, "Date " + dateTimeFormater.format(LocalDateTime.now()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
