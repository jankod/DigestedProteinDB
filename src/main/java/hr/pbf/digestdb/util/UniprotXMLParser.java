package hr.pbf.digestdb.util;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.zip.GZIPInputStream;

@Getter
public class UniprotXMLParser {


    @Data
    public abstract static class ProteinHandler {

        public boolean stopped = false;

        public long counter = 0;

        public abstract void gotProtein(ProteinInfo p) throws IOException;
    }

    private long totalCount = 0;

    public void parseProteinsFromXMLstream(String filePath, ProteinHandler proteinHandler) {
        ProteinInfo proteinInfo = null;

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, true);
            XMLStreamReader reader;

            if (filePath.endsWith(".gz")) {
                GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(filePath), 65536);
                reader = factory.createXMLStreamReader(gzipInputStream);
            } else {
                reader = factory.createXMLStreamReader(new FileInputStream(filePath));
            }
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

            reader.close();

        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }


    }

    static @Data
    public class ProteinInfo {
        String accession;
        String proteinName;
        int taxonomyId;
        String taxonomyName;

        @ToString.Exclude
        String sequence;
    }


}

