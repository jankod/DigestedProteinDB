package hr.pbf.digestdb.util;

import hr.pbf.digested.proto.Peptides;
import lombok.Data;

@Data
public class ParseUniprotDatFile {


    private final String path;

    public ParseUniprotDatFile(String path) {
        this.path = path;
    }

    public static void main(String[] args) {

    }
}
