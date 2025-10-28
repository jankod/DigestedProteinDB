package hr.pbf.digestdb.tools;

import hr.pbf.digestdb.SearchWeb;
import hr.pbf.digestdb.db.AccessionDbReader;
import hr.pbf.digestdb.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.RocksDB;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class CreatePeptideTaxCsv {
    static AccTaxDB accessionTaxDb;
    static AccessionDbReader accessionDbReader;

    public static void main(String[] args) throws XMLStreamException, IOException {
        String accTaxidCsv = "/disk4/janko/trembl_5_60_2_db/acc_taxid.csv";
        String massPep_accid_groupedCsv = "/disk4/janko/trembl_5_60_2_db/gen/mass_pep_accid_grouped.csv";
        String resultCsvPath = "/disk4/janko/trembl_5_60_2_db/peptide_taxids.csv";
        String customAccession = "/disk4/janko/trembl_5_60_2_db/custom_accession.db";


        // LOCAL
   //     accTaxidCsv = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/test_biotype/acc_taxid.csv";
     //   massPep_accid_groupedCsv = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/test_biotype/mass_pep_accid_grouped_100.csv";
      //  resultCsvPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/test_biotype/peptide_taxids.csv";
     //   customAccession = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/test_biotype/custom_accession.db";

        System.out.println("Loading accession-taxid db from " + accTaxidCsv);
        accessionTaxDb = AccTaxDB.loadFromDiskCsv(accTaxidCsv);
        System.out.println("Loaded accession-taxid db with " + accessionTaxDb.size() + " entries.");
        accessionDbReader = new AccessionDbReader(customAccession);


        // create CSV with columns:
        // peptide, taxids
        // AAAAAA,123;234;345
        // AAAAAAC,123;234;345


        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(Path.of(massPep_accid_groupedCsv)), 1024 * 1024 * 32);
             BufferedWriter writer = Files.newBufferedWriter(Path.of(resultCsvPath))) {
            int countMasses = 0;
            long lineCount = 0;

            String line;
            // line format:
            // # mass,[PEPTIDE1:acc_id1;acc_id2...-PEPTIDE:acc_id2...-PEPTIDE3:acc_id3....]
            // 360.1393,GGGGGG:0;33;132;264;165;363;330;364;265;263;232;67;100;394;426;416;417;413;415;418;424;419;420;421;423;422-GGGGAG:573;569;581;561;557;570;578;556;577;572;568;564;574;562;560;566;565;567;571;575;563;583;555;558;559;579;576;580;589;588;582;587;586;584;585-GAGGGG:470;474;478;466;453;454;449;450;473;475;479;477;468;464;476;456;452;455;451;472;471;465;469;457;467;460;461;458;459;462;463-GGAGGG:503;507;511;519;499;516;515;512;491;490;495;494;520;486;488;487;484;480;485;482;481;483;505;501;497;509;518;489;517;513;514;492;493;504;502;500;506;496;510;508;498-GGGGGA:668;602;643;676;594;635;659;660;618;619;642;601;611;644;629;626;627;630;631;672;639;664;647;606;614

            while ((line = reader.readLine()) != null) {
                // 503.234,SGAGAAA:15-SAAGGAA:14-TGAAAGG:16
                String[] parts = line.split(",", 2);

                if (parts.length < 2)
                    throw new IllegalArgumentException("Invalid input CSV format " + line);

                // double mass = Double.parseDouble(parts[0]);
                // int massInt = (int) Math.round(mass * 10_000);
                String seqAccs = parts[1];

                try {
                    List<SearchWeb.PeptideAccTax> peptideAccTaxes = readGroupedRow(seqAccs);
                    for (SearchWeb.PeptideAccTax peptideAccTax : peptideAccTaxes) {
                        String pep = peptideAccTax.getSeq();
                        String taxIds = toTaxList(peptideAccTax.getAccsTax());
                        writer.write(pep + "," + taxIds);
                        writer.newLine();
                    }

                    countMasses++;
                } catch (Exception e) {
                    log.error("Error on line: " + lineCount + ": " + StringUtils.truncate(line, 200), e);
                    throw new RuntimeException(e);
                }
                lineCount++;
            }
        }


    }

    public static String toTaxList(List<SearchWeb.AccTaxs> accsTax) {
        // retrn unique tax ids separated by ;
        return accsTax.stream().map(at -> Integer.toString(at.getTaxId())).distinct().reduce((a, b) -> a + ";" + b).orElse("");
    }

    private static List<SearchWeb.PeptideAccTax> readGroupedRow(String value) {
        List<SearchWeb.PeptideAccTax> result = new java.util.ArrayList<>();
        int start = 0;
        while (start < value.length()) {
            int colonIndex = value.indexOf(':', start);
            String seq = value.substring(start, colonIndex);
            int dashIndex = value.indexOf('-', colonIndex);
            if (dashIndex == -1)
                dashIndex = value.length();
            String[] accessions = value.substring(colonIndex + 1, dashIndex).split(";");

            List<SearchWeb.AccTaxs> accTaxs = new java.util.ArrayList<>();
            for (String accession : accessions) {
                //  String accString = MyUtil.fromAccessionLong36(Long.parseLong(accession));
                int accId = Integer.parseInt(accession);
                String accString = accessionDbReader.getAccession(accId);
                int taxId = accessionTaxDb.getTaxonomyId(accString);
                if (taxId == 0) {
                    System.err.println("Warning: No taxId for accession: " + accession);
                    log.warn("No taxId for accession: {}", accession);
                }
                accTaxs.add(new SearchWeb.AccTaxs(accession, taxId));
            }
            SearchWeb.PeptideAccTax e = new SearchWeb.PeptideAccTax();
            e.setSeq(seq);
            e.setAccsTax(accTaxs);
            result.add(e);

            start = dashIndex + 1;
        }
        return result;
    }
}
