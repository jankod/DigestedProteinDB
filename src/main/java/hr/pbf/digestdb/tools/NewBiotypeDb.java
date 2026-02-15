package hr.pbf.digestdb.tools;

import hr.pbf.digestdb.util.MyUtil;
import hr.pbf.digestdb.util.TaxonomyParser;
import hr.pbf.digestdb.util.UniprotXMLParser;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;

public class NewBiotypeDb {
    public static void main(String[] args) throws XMLStreamException, IOException {

        String swisprotXml = "/home/tag/Downloads/uniprot_sprot.xml.gz";

        // TaxID -> TaxonomyNode
        //   Map<Integer, TaxonomyParser.TaxonomyNode> tax = TaxonomyParser.parseNodes("/misc/ncbi/taxdump/nodes.dmp");
        // Use only rank ∈ {species, subspecies, strain, varietas, forma}


        //      FilterBioType filterBioType = new FilterBioType(tax);

        StopWatch stopWatch = StopWatch.createStarted();
        final int[] c = {0};
        UniprotXMLParser parser = new UniprotXMLParser();
        parser.parseProteinsFromXMLstream(swisprotXml, new UniprotXMLParser.ProteinHandler() {
            @Override
            public void gotProtein(UniprotXMLParser.ProteinInfo p) {
                c[0]++;
            }
        });

        stopWatch.stop();
        System.out.println(DurationFormatUtils.formatDurationHMS(stopWatch.getTime()) + " " + c[0]  );


    }
}
