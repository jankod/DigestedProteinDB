package hr.pbf.digestdb.experiments;

import hr.pbf.digestdb.exception.NcbiTaxonomyException;
import hr.pbf.digestdb.model.TaxonomyDivision;
import hr.pbf.digestdb.util.AccTaxDB;
import hr.pbf.digestdb.util.MyUtil;
import hr.pbf.digestdb.util.NcbiTaksonomyRelations;
import hr.pbf.digestdb.util.UniprotXMLParser;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UseOnlyBacteria {

    public static void main(String[] args) throws IOException {
          AccTaxDB accTaxDB = AccTaxDB.loadFromDiskCsv("/home/tag/IdeaProjects/DigestedProteinDB/misc/db/trembl/acc_taxid.csv");
    }

    public static void main2(String[] args) throws NcbiTaxonomyException, XMLStreamException, IOException {

        // read CSV 7896.1185,YYRYNHHYNYYYYHYYYYYYYYYYYYYYYYYYYYYFYYYYCYYYFYYYYY,A0A9D4F2M3
        //7932.0667,CYYYYYYYYYYYYYYYYYYYYYYYYYYYFYYYYYYYYYYYYYYYYYYYY,A0A315UQ47
        //7964.1195,YYDYYYYYYYYYYYYYYYYYFYYYHYYHYYYYYNYHYYYYYYYYYYFYYY,A0A9D4RK55
        //7970.0609,YYYYYYYYYYYCYYYYYYYYYYYYNYYFYYYFYYYYYYYYYYYYYYYCYY,A0A9D4BZQ7
        //7973.2054,LYVYIEYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY,A0A183BC07
        //7991.1614,MYVYIEYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY,A0A3P8IKL6
        //8019.0487,MYYYYYYYYYYYYYYYYYYYCCYYYYYYYYYYYYYYYYYYYYYYYYYYYY,A0A9D4CL15
        //8022.1141,DNYYYYYHYYYYYYYYYYYYYYYYYYYYYYYYHYYYYYYYYYYYYYYYYY,A0A959RPK1
        //8023.2029,MLYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYFYYYYYYYYYYYYYYL,A0A9D4MIE1
        //8532.7586,IRWPFIRWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWCWWWWW,A0A9D4IJ39


        NcbiTaksonomyRelations taxonomy = NcbiTaksonomyRelations.loadTaxonomyNodes("/home/tag/IdeaProjects/DigestedProteinDB/misc/ncbi/taxdump/nodes.dmp");

        AccTaxDB accTaxDB = AccTaxDB.loadFromDisk("/home/tag/IdeaProjects/DigestedProteinDB/misc/db/trembl/gen/acc_taxid.csv");

        boolean isFirst = true;
        try (BufferedWriter bacteriaCsv = Files.newBufferedWriter(Path.of("/home/tag/IdeaProjects/DigestedProteinDB/misc/db/trembl/mass_pep_acc_sorted_bacteria.csv"))) {

            try (BufferedReader file = Files.newBufferedReader(Path.of("/media/tag/D/digested-db/mass_pep_acc_sorted.csv"))) {
                String line;
                while ((line = file.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] parts = line.split(",");
                    String mass = parts[0].trim();
                    String peptide = parts[1].trim();
                    String acc = parts[2].trim();

                    int taxonomyId = accTaxDB.getTaxonomyId(acc);

                    if (taxonomy.isTaxIdInDivision(taxonomyId, TaxonomyDivision.BACTERIA)) {
                        if (!isFirst) {
                            bacteriaCsv.newLine();
                        } else {
                            isFirst = false;
                        }
                        bacteriaCsv.write(mass + "," + peptide + "," + acc + "," + taxonomyId);
                    }


                }

            }
        }


    }

    private static void createAccTaxIdMap() throws XMLStreamException, IOException {
        UniprotXMLParser parser = new UniprotXMLParser();

        String fromSwisprotPath = "/media/tag/D/digested-db/uniprot_trembl.xml.gz";

        AccTaxDB accTaxDB = AccTaxDB.createEmptyDb();
        parser.parseProteinsFromXMLstream(fromSwisprotPath, new UniprotXMLParser.ProteinHandler() {
            @Override
            public void gotProtein(UniprotXMLParser.ProteinInfo p) {
                String accession = p.getAccession();
                long accLong36 = MyUtil.toAccessionLong36(accession);
                accTaxDB.addAccessionTaxId(accLong36, p.getTaxonomyId());
            }
        });
        accTaxDB.writeToDiskCsv("/home/tag/IdeaProjects/DigestedProteinDB/misc/db/trembl/gen/acc_taxid.csv");
    }
}
