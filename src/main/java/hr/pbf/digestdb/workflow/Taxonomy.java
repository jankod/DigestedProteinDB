package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.util.UniprotXMLParser;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Taxonomy {

    public static void main(String[] args) {
        UniprotXMLParser parser = new UniprotXMLParser();


        String s = "";
        parser.parseProteinsFromXMLstream(s, new UniprotXMLParser.ProteinHandler() {
            @Override
            public void gotProtein(UniprotXMLParser.ProteinInfo p) throws IOException {
                System.out.println(p.getAccession());
            }
        });

    }
}
