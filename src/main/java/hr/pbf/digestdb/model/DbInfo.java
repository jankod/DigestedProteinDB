package hr.pbf.digestdb.model;

import lombok.Data;

@Data
public class DbInfo {
    private String missCleavage;
    private String minPeptideLength;
    private String maxPeptideLength;
    private String dbName;
    private String enzimeName;
    private String proteinCount;

}
