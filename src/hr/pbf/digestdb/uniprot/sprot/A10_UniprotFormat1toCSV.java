package hr.pbf.digestdb.uniprot.sprot;

import hr.pbf.digestdb.uniprot.UniprotModel;
import hr.pbf.digestdb.uniprot.UniprotUtil;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.UniprotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class A10_UniprotFormat1toCSV {
    private static final Logger log = LoggerFactory.getLogger(A10_UniprotFormat1toCSV.class);

    public A10_UniprotFormat1toCSV() {
    }

    static int division = 0;



    public static void main(String[] args) throws IOException {
        String baseDir = UniprotConfig.get(UniprotConfig.Name.BASE_DIR);
        String format1Dir = baseDir + "/uniprot_trembl_archaea.dat_format1";
        String pathOutputCSV = baseDir + "uniprot_trembl_archaea.csv";

        division = UniprotModel.DIVISION.Archaea.getIdDB();

        A10_UniprotFormat1toCSV a = new A10_UniprotFormat1toCSV();

        log.debug("Read: " + format1Dir);
        log.debug("OUT " + pathOutputCSV);

        a.start(format1Dir, pathOutputCSV);
    }

    public void start(String dirFormat1, String pathResultCsv) throws IOException {
        BufferedWriter out = BioUtil.newFileWiter(pathResultCsv, "ASCII");

        File[] files = new File(dirFormat1).listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String nameMass1 = o1.getName().replace(".format1", "");
                float m1 = Float.parseFloat(nameMass1);

                String nameMass2 = o2.getName().replace(".format1", "");
                float m2 = Float.parseFloat(nameMass2);
                return Float.compare(m1, m2);
            }

        });

        out.write("mass\tpeptide\tacc\ttax_id\tdiv\n");


        for (File f : files) {
            byte[] format1bytes = UniprotUtil.toByteArrayFast(f);

            ArrayList<UniprotModel.PeptideAccTax> pepList = UniprotUtil.fromFormat1(format1bytes);
            for (UniprotModel.PeptideAccTax p : pepList) {
                double mass = BioUtil.calculateMassWidthH2O(p.getPeptide());
                // TODO: zasto divizije nisu iste kao i u ncbi taxonomy
                out.write(mass + "\t" + p.getPeptide() + "\t" + p.getAcc() + "\t" + p.getTax() + "\t" + division+"\n");

            }
            log.debug(f.toString());
        }

    }

}
