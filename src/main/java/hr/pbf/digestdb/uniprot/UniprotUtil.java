package hr.pbf.digestdb.uniprot;

import static java.util.stream.Collectors.groupingBy;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;

import java.util.Map.Entry;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxMass;
import hr.pbf.digestdb.util.BioUtil;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class UniprotUtil {
    private static final Logger log = LoggerFactory.getLogger(UniprotUtil.class);

    /**
     * Remove entry with mass less on fromMass or more than toMass.
     *
     * @param masses
     * @param fromMass
     * @param toMass
     */
    public final static void reduceMasses(Map<String, Double> masses, int fromMass, int toMass) {
        Iterator<String> it = masses.keySet().iterator();
        while (it.hasNext()) {
            Double m = masses.get(it.next());
            if (!(fromMass <= m && m <= toMass))
                it.remove();
        }
    }

    public final static void writeOneFormat1(DataOutput out, String peptide, int tax, String acc) throws IOException {
        out.writeUTF(peptide);
        out.writeInt(tax);
        out.writeUTF(acc);
    }

    public final static PeptideAccTax readOneFormat1(DataInput in) throws IOException {
        String peptide = in.readUTF();
        int tax = in.readInt();
        String acc = in.readUTF();
        return new PeptideAccTax(peptide, acc, tax);
    }


    public static Map<String, List<UniprotModel.PeptideAccTaxNames>> groupByPeptide(List<UniprotModel.PeptideAccTaxNames> data) {
        Map<String, List<UniprotModel.PeptideAccTaxNames>> grouped = data.stream().collect(groupingBy(o -> {
            return o.getPeptide();
        }));
        return grouped;
    }

    /**
     * Group by peptide and write to byte array.
     *
     * @param data
     * @return
     * @throws IOException
     */
    public final static Map<String, List<PeptideAccTax>> groupByPeptide(ArrayList<PeptideAccTax> data) {
        Map<String, List<PeptideAccTax>> grouped = data.stream().collect(groupingBy(o -> {
            return o.getPeptide();
        }));
        return grouped;
    }

    public final static byte[] toFormat2(Map<String, List<PeptideAccTax>> grouped) throws IOException {
        FastByteArrayOutputStream byteOut = new FastByteArrayOutputStream(grouped.size() * 18);
        // MyDataOutputStream out = new MyDataOutputStream(byteOut);
        FastOutput out = new FastOutput(byteOut);
        int countTotal = 0;
        int countUnique = 0;
        Set<Entry<String, List<PeptideAccTax>>> entrySet = grouped.entrySet();
        out.writeInt(entrySet.size());
        log.debug("WRITE SIzE " + entrySet.size());
        for (Entry<String, List<PeptideAccTax>> entry : entrySet) {
            String peptide = entry.getKey();
            List<PeptideAccTax> p = entry.getValue();
            out.writeAscii(peptide);
            countUnique++;
            out.writeInt(p.size());
            for (PeptideAccTax pAccTax : p) {
                out.writeAscii(pAccTax.getAcc());
                out.writeInt(pAccTax.getTax());
                countTotal++;
            }
        }
        out.close();
        log.debug("format2 zapisao unique  " + countUnique);
        log.debug("format2 zapisao total   " + countTotal);
        return byteOut.array;
    }

    public final static Map<String, List<PeptideAccTaxMass>> fromFormat2(byte[] format2, boolean sortByMass)
            throws IOException {
        return fromFormat2(format2, sortByMass, 0, 1000000);
    }

    public final static Map<String, List<PeptideAccTaxMass>> fromFormat2(byte[] format2, boolean sortByMass,
                                                                         float fromMass, float toMass) throws IOException {

        try (FastInput in = new FastInput(new FastByteArrayInputStream(format2))) {

            final int how = in.readInt();
            // log.debug("READ SIZE "+ how);
            Map<String, List<PeptideAccTaxMass>> result;

            final HashMap<String, Double> cache = new HashMap<>(200);

            if (!sortByMass) {
                result = new HashMap<String, List<PeptideAccTaxMass>>(how);
            } else {
                result = new TreeMap<String, List<PeptideAccTaxMass>>(new Comparator<String>() {

                    // private final HashMap<String, Double> cache = new HashMap<>(200);

                    @Override
                    public int compare(String p1, String p2) {

                        // return Double.compare(BioUtil.calculateMassWidthH2O(p1),
                        // BioUtil.calculateMassWidthH2O(p2));

                        double m1;
                        double m2;
                        if (cache.containsKey(p1)) {
                            m1 = cache.get(p1);
                        } else {
                            m1 = BioUtil.calculateMassWidthH2O(p1);
                        }
                        if (cache.containsKey(p2)) {
                            m2 = cache.get(p2);
                        } else {
                            m2 = BioUtil.calculateMassWidthH2O(p2);
                        }
                        if (m1 == m2) { // important! May be two peptide with equal mass, but diferent sequence, and map
                            // will then refuse samo peptide!
                            return p1.compareTo(p2);
                        }

                        return Double.compare(m1, m2);
                    }
                });
            }
            int countTotal = 0;
            int countUnique = 0;
            for (int i = 0; i < how; i++) {
                String peptide = in.readString();
                int howInList = in.readInt();

                double mass = BioUtil.calculateMassWidthH2O(peptide);
                cache.put(peptide, mass);

                boolean skipPeptide = false;
                if (mass < fromMass || toMass < mass) {
                    skipPeptide = true;
                    log.debug("Skip peptide " + peptide);
                }

                ArrayList<PeptideAccTaxMass> pepList = null;
                pepList = new ArrayList<PeptideAccTaxMass>(howInList);

                if (!skipPeptide) {
                    countUnique++;
                    if (result.containsKey(peptide)) {
                        // log.debug("Sadrzi vec peptide "+ peptide);
                    }
                    result.put(peptide, pepList);
                }
                for (int j = 0; j < howInList; j++) {
                    String acc = in.readString();
                    // if ("A0A1J4YX49".equals(acc)) {
                    // log.debug("nasao peptide: " + peptide + " skip: " + skipPeptide);
                    // }
                    int tax = in.readInt();
                    if (!skipPeptide) {
                        countTotal++;
                        pepList.add(new PeptideAccTaxMass(peptide, acc, tax, (float) mass));
                    }
                }
            }
            // log.debug("fromFormat2 nasao ih unique " + countUnique+ " "+ result.size());
            // log.debug("fromFormat2 nasao ih ukupno " + countTotal);
            return result;
        }
        // in.close();

    }

    public final static ArrayList<PeptideAccTax> format2ToPeptidesAndUnGroup(byte[] format2) throws IOException {
        MyDataInputStream in = new MyDataInputStream(new FastByteArrayInputStream(format2));
        final int how = in.readInt();
        ArrayList<PeptideAccTax> res = new ArrayList<>(how);
        for (int i = 0; i < how; i++) {
            final String peptide = in.readUTF();
            final int howAccTax = in.readInt();
            for (int j = 0; j < howAccTax; j++) {
                final PeptideAccTax p = new PeptideAccTax();
                p.setPeptide(peptide);
                p.setAcc(in.readUTF());
                p.setTax(in.readInt());
                res.add(p);
            }
        }
        in.close();
        return res;
    }

    public static String getDirectorySize(String pathDir) {
        return FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectory(new File(pathDir)));
    }

    public static byte[] toByteArrayFast(String path) throws IOException {
        return toByteArrayFast(new File(path));
    }

    public static byte[] toByteArrayFast(File f, long from, long to) throws IOException {
        if (to <= from) {
            throw new IOException("to is less then from. from: " + from + " to: " + to);
        }
        try (RandomAccessFile memoryFile = new RandomAccessFile(f.getPath(), "r")) {
            long length = f.length();
            if (length > Integer.MAX_VALUE) {
                throw new IOException("File length is more then integer: " + length);
            }
            MappedByteBuffer map = memoryFile.getChannel().map(FileChannel.MapMode.READ_ONLY, from, to - from);
            // mappedByteBuffer.array();

            byte[] all = new byte[(int) length];
            map.get(all);
            return all;
        }
    }

    public static byte[] toByteArrayFast(File f) throws IOException {
        try (RandomAccessFile memoryFile = new RandomAccessFile(f.getPath(), "r")) {
            long length = f.length();
            if (length > Integer.MAX_VALUE) {
                throw new IOException("File length is more then integer: " + length);
            }
            MappedByteBuffer mappedByteBuffer = memoryFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, length);
            // mappedByteBuffer.array();
            byte[] all = new byte[(int) length];
            mappedByteBuffer.get(all);
            return all;
        }
    }


    public static ArrayList<PeptideAccTax> fromFormat1(byte[] bytes) throws IOException {
        MyDataInputStream in = new MyDataInputStream(new FastByteArrayInputStream(bytes));

        ArrayList<PeptideAccTax> pepList = new ArrayList<>();
        while (in.available() != 0) {
            PeptideAccTax pep = UniprotUtil.readOneFormat1(in);
            pepList.add(pep);
        }
        in.close();
        return pepList;
    }

    public static byte[] uncompress(byte[] b) throws IOException {
        return Snappy.uncompress(b);
    }

    public static byte[] compress(byte[] b) throws IOException {
        return Snappy.compress(b);
    }

    public static void printDurration(String string, StopWatch s) {
        s.stop();
        log.debug(string + " " + DurationFormatUtils.formatDurationHMS(s.getTime()));
    }


}
