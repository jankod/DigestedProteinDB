package hr.pbf.digestdb.util;

import com.fasterxml.jackson.core.Base64Variant;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Slf4j
@Getter
public class UniprotXMLParser {

    @Data
    public abstract static class ProteinHandler {

        public boolean stopped = false;

        public long counter = 0;

        public abstract void gotProtein(ProteinInfo p) throws IOException;
    }

    @Getter
    public enum Division {
        BACTERIA(0, "Bacteria"),
        INVERTEBRATES(1, "Invertebrates"),
        MAMMALS(2, "Mammals"),
        PHAGES(3, "Phages"),
        PLANTS_AND_FUNGI(4, "Plants and Fungi"),
        PRIMATES(5, "Primates"),
        RODENTS(6, "Rodents"),
        SYNTHETIC_AND_CHIMERIC(7, "Synthetic and Chimeric"),
        UNASSIGNED(8, "Unassigned"),
        VIRUSES(9, "Viruses"),
        VERTEBRATES(10, "Vertebrates"),
        ENVIRONMENTAL_SAMPLES(11, "Environmental samples");

        private final int id;
        private final String name;

        Division(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static Division fromTaxon(String taxon) {
            if (taxon == null)
                return UNASSIGNED;

            return switch (taxon.trim().toLowerCase()) {
                case "bacteria" -> BACTERIA;
                case "viruses" -> VIRUSES;
                case "primates" -> PRIMATES;
                case "rodents" -> RODENTS;
                case "mammals" -> MAMMALS;
                case "vertebrates" -> VERTEBRATES;
                case "invertebrates" -> INVERTEBRATES;
                case "plants", "fungi" -> PLANTS_AND_FUNGI;
                case "phages" -> PHAGES;
                case "environmental samples" -> ENVIRONMENTAL_SAMPLES;
                case "synthetic", "chimeric" -> SYNTHETIC_AND_CHIMERIC;
                default -> UNASSIGNED;
            };
        }

        public static Division fromId(int divisionId) {
            for (Division division : Division.values()) {
                if (division.getId() == divisionId) {
                    return division;
                }
            }
            return UNASSIGNED;

        }
    }

    private long totalCount = 0;

    public void parseProteinsFromXMLstream(String filePath, ProteinHandler proteinHandler) {
        String limit = String.valueOf(Integer.MAX_VALUE);
        System.setProperty("jdk.xml.maxGeneralEntitySizeLimit", limit);
        System.setProperty("jdk.xml.totalEntitySizeLimit", limit);
        System.setProperty("jdk.xml.entityExpansionLimit", limit);

        ProteinInfo proteinInfo = null;
        boolean inLineage = false;

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, true);
            XMLStreamReader reader;

            if (filePath.endsWith(".gz")) {
                GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(filePath), 65536 * 2);
                reader = factory.createXMLStreamReader(gzipInputStream);
            } else {
                reader = factory.createXMLStreamReader(new FileInputStream(filePath));
            }
            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if ("entry".equals(reader.getLocalName())) {
//                            System.out.println("entry");
                            proteinInfo = new ProteinInfo();
                            if (proteinHandler.stopped) {
                                return;
                            }
                        } else if (proteinInfo != null) {
                            switch (reader.getLocalName()) {
                                case "accession":
                                    // primary accession is first in the list
                                    if (proteinInfo.getAccession() == null) {
                                        String elementText = reader.getElementText();
//                                        System.out.println("accession " + elementText);
                                        proteinInfo.setAccession(elementText);
                                    }
                                    break;
                                case "fullName":
                                    String fullName = reader.getElementText();
//                                    System.out.println("fullName " + fullName);
                                    proteinInfo.setProteinName(fullName);
                                    break;
                                case "dbReference":
                                    if ("NCBI Taxonomy".equals(reader.getAttributeValue(null, "type"))) {
                                        try {
                                            String taxId = reader.getAttributeValue(null, "id");
//                                            System.out.println("taxonomyId " + taxId);
                                            // proteinInfo.setTaxonomyId(Integer.parseInt(reader.getAttributeValue(null, "id")));
                                            proteinInfo.addTaxonomyId(Integer.parseInt(taxId));
                                        } catch (NumberFormatException e) {
                                            //proteinInfo.setTaxonomyId(-1);
                                            log.error("Error parsing taxonomyId", e);
                                        }
                                    }
                                    break;
                              /*  case "name":
                                    if ("scientific".equals(reader.getAttributeValue(null, "type"))) {
                                        String elementText = reader.getElementText();
                                        System.out.println("taxonomyName " + elementText);
                                        proteinInfo.setTaxonomyName(elementText);
                                    }
                                    break;*/
                                case "lineage":
                                    inLineage = true;
                                    break;
                                case "taxon":
                                    if (inLineage && proteinInfo.getDivisionId() == -1) {
                                        String taxon = reader.getElementText();
                                        Division division = Division.fromTaxon(taxon);
//                                        System.out.println("division " + taxon);
                                        proteinInfo.setDivisionId(division.getId());
                                    }
                                    break;
                                case "sequence":
                                    String seq = reader.getElementText();
//                                    System.out.println("sequence " + StringUtils.truncate(seq, 10)+"...");
                                    proteinInfo.setSequence(seq.trim());
                                    break;
                            }
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if ("entry".equals(reader.getLocalName()) && proteinInfo != null) {
//                            System.out.println("entry end");
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
        //  int taxonomyId;
        List<Integer> taxonomyIds = new ArrayList<>(6);
     //   String taxonomyName;
        int divisionId = -1;

        @ToString.Exclude
        String sequence;

        public void addTaxonomyId(int taxId) {
            taxonomyIds.add(taxId);
        }
    }

}

