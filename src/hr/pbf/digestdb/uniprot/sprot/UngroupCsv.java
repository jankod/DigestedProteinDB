package hr.pbf.digestdb.uniprot.sprot;

import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.CallbackMass;
import hr.pbf.digestdb.util.MassCSV;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;

public class UngroupCsv {
    private static final Logger log = LoggerFactory.getLogger(UngroupCsv.class);

    public static void main(String[] args) throws IOException {
        String pathCSV = "F:\\ProteinReader\\UniprotDBfiles\\trembl._10_000_lines.csv";
        String pathUngropuedCSV = pathCSV + ".ungroup.csv";



        if(SystemUtils.IS_OS_LINUX) {
            pathCSV = "/home/tag/uniprot/trembl.csv";
            pathUngropuedCSV = pathCSV + ".ungroup.csv";
        }
        log.debug(pathCSV);
        log.debug(pathUngropuedCSV);

        BufferedWriter out = BioUtil.newFileWiter(pathUngropuedCSV, "ASCII");
        MutableString line = new MutableString(220);
        try (FastBufferedReader reader = new FastBufferedReader(new FileReader(pathCSV))) {
            while ((reader.readLine(line)) != null) {

                String[] split = StringUtils.split(line.toString(), '\t');

                float mass = Float.parseFloat(split[0]);
                String peptide = split[1].trim();
                String accTaxRow = split[2].trim();
                String[] splitAccTax = StringUtils.split(accTaxRow, ",");
                for (String accTax : splitAccTax) {
                    String[] at = StringUtils.split(accTax, ":");
                    String acc = at[0];
                    int tax = Integer.parseInt(at[1]);
                    out.write(mass +"\t"+ peptide + "\t"+acc+"\t"+tax+"\n");
                }


            }
        }
        IOUtils.closeQuietly(out);
        log.debug("finish");
    }
}
