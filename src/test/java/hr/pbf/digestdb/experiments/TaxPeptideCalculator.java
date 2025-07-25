package hr.pbf.digestdb.experiments;

import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.LongCounter;
import hr.pbf.digestdb.util.NcbiTaksonomyRelations;
import hr.pbf.digestdb.util.UniprotXMLParser;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaxPeptideCalculator {

    public static void main(String[] args) throws NcbiTaxonomyException, XMLStreamException, IOException {

        String ncbiTaxonomyPath = "";
        String uniprotXmlPath = "";

        NcbiTaksonomyRelations ncbiTaksonomyRelations = NcbiTaksonomyRelations.loadTaxonomyNodes(ncbiTaxonomyPath);

         UniprotXMLParser parser = new UniprotXMLParser();


         // taxId -> peptide count
        Int2IntArrayMap taxIdPeptideCount = new Int2IntArrayMap();

        parser.parseProteinsFromXMLstream(uniprotXmlPath, new UniprotXMLParser.ProteinHandler() {
              @Override
              public void gotProtein(UniprotXMLParser.ProteinInfo p) {

                  List<String> peptides = BioUtil.tripsyn2mc(p.getSequence(), 6, 50);
                  for (String peptide : peptides) {




                  }

              }
          });

    }
}
