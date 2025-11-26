package hr.pbf.digestdb.tools;

import hr.pbf.digestdb.util.MyUtil;
import hr.pbf.digestdb.util.TaxonomyParser;
import hr.pbf.digestdb.util.UniprotXMLParser;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;

public class NewBiotypeDb {
    public static void main(String[] args) throws XMLStreamException, IOException {

        String swisprotXml = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/db_all_swisprot/src/uniprot_sprot.xml";

        // TaxID -> TaxonomyNode
        Map<Integer, TaxonomyParser.TaxonomyNode> tax = TaxonomyParser.parseNodes("/misc/ncbi/taxdump/nodes.dmp");
        // Use only rank ∈ {species, subspecies, strain, varietas, forma}


        FilterBioType filterBioType = new FilterBioType(tax);

        UniprotXMLParser parser = new UniprotXMLParser();
        parser.parseProteinsFromXMLstream(swisprotXml, new UniprotXMLParser.ProteinHandler() {
            @Override
            public void gotProtein(UniprotXMLParser.ProteinInfo p) {

                // mass1, petideId1:taxId1;petideId2:taxId2;...
                // mass2, petideId1:taxId1;petideId2:taxId2;...
                if(!filterBioType.isOK(p)) {
                    return;
                }
                long acc = MyUtil.toAccessionLong36(p.getAccession());
            }
        });


    }
}
