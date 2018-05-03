package hr.pbf.digestdb.uniprot.sprot;

import java.io.*;

import org.apache.commons.lang3.StringUtils;

import hr.pbf.digestdb.util.BioUtil;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;

public class PrepareTaxonomy {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        //writeNodes();

        writeNames();
    }

    private static void writeNames() throws IOException {
        int count = 0;
        String path = "F:\\ProteinReader\\UniprotDBfiles\\new_tax_dump\\names.dmp";
        BufferedWriter outNodes = BioUtil.newFileWiter(path + ".correct.csv",
                "UTF-8");

        try (FastBufferedReader reader = new FastBufferedReader(
                new FileReader(path))) {

            MutableString line = new MutableString();
            while ((reader.readLine(line)) != null) {
                count++;

                if (line.toString().contains("scientific name")) {

                    String newLine = StringUtils.replace(line.toString(), "\t|\t", "\t");
                    String[] split = StringUtils.split(newLine, "\t");
                    int tax_id = Integer.parseInt(split[0]);
                    String taxName = split[1];
                 //   String unique_name = split[2];
                   // String name_class = split[3];

                    outNodes.write(tax_id + "\t" + taxName + "\n");
                }
            }
            System.out.println("Count " + count);
            outNodes.close();
        }
    }

    private static void writeNodes() throws IOException {
        int count = 0;

        BufferedWriter outNodes = BioUtil.newFileWiter("F:\\ProteinReader\\UniprotDBfiles\\new_tax_dump\\nodes.dmp.csv",
                "UTF-8");

        try (FastBufferedReader reader = new FastBufferedReader(
                new FileReader("F:\\ProteinReader\\UniprotDBfiles\\new_tax_dump\\nodes.dmp"))) {

            MutableString line = new MutableString();
            while ((reader.readLine(line)) != null) {
                count++;
                String[] split = StringUtils.splitByWholeSeparator(line.toString(), "\t|\t");
                int taxId = Integer.parseInt(split[0]);
                int parrentTaxId = Integer.parseInt(split[1]);
                int divisionId = Integer.parseInt(split[4]);
                String rank = split[5];

                outNodes.write(taxId + "\t-\t" + parrentTaxId + "\t" + divisionId + "\t" + rank + "\n");


                // batch.put(BiteUtil.toByte(taxId), value)
            }
            System.out.println("Count " + count);
            outNodes.close();
        }
    }
}
