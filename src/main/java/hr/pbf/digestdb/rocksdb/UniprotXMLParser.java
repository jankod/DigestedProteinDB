package hr.pbf.digestdb.rocksdb;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

@Getter
public class UniprotXMLParser {


    @Data
    public abstract static class ProteinHandler {

        boolean stopped = false;

        int counter = 0;

        abstract void gotProtein(ProteinInfo p);
    }

    private int totalCount = 0;

    public void parseProteinsFromXMLstream(String filePath, ProteinHandler proteinHandler) {
        ProteinInfo proteinInfo = null;

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(filePath));

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if ("entry".equals(reader.getLocalName())) {
                            proteinInfo = new ProteinInfo();
                            if (proteinHandler.stopped) {
                                return;
                            }
//                            if (proteinList.size() > 100) {
//                                return proteinList;
//                            }
                        } else if (proteinInfo != null) {
                            switch (reader.getLocalName()) {
                                case "accession":
                                    proteinInfo.setAccession(reader.getElementText());
                                    break;
                                case "fullName":
                                    proteinInfo.setProteinName(reader.getElementText());
                                    break;
                                case "dbReference":
                                    if ("NCBI Taxonomy".equals(reader.getAttributeValue(null, "type"))) {
                                        try {
                                            proteinInfo.setTaxonomyId(Integer.parseInt(reader.getAttributeValue(null, "id")));
                                        } catch (NumberFormatException e) {
                                            proteinInfo.setTaxonomyId(-1);
                                        }
                                    }
                                    break;
                                case "name":
                                    if ("scientific".equals(reader.getAttributeValue(null, "type"))) {
                                        proteinInfo.setTaxonomyName(reader.getElementText());
                                    }
                                    break;
                                case "sequence":
                                    proteinInfo.setSequence(reader.getElementText().trim());
                                    break;
                            }
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if ("entry".equals(reader.getLocalName()) && proteinInfo != null) {
//                            proteinList.add(proteinInfo);
                            proteinHandler.counter++;
                            totalCount++;
                            proteinHandler.gotProtein(proteinInfo);
                        }
                        break;
                }
            }
        } catch (FileNotFoundException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }


}

@Data
class ProteinInfo {
    String accession;
    String proteinName;
    int taxonomyId;
    String taxonomyName;

    @ToString.Exclude
    String sequence;
}
