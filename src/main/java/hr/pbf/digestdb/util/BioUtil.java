package hr.pbf.digestdb.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import hr.pbf.digestdb.model.FastaSeq;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

@Slf4j
@UtilityClass
public class BioUtil {

    public static final double PROTON_MASS = 1.007825;

    public static final double H2O = 18.0105646D;

    public BufferedReader newFileReader(String path, String charset, int bufSize)
          throws UnsupportedEncodingException, FileNotFoundException {
        if (charset == null) {
            charset = "ASCII";
        }
        return new BufferedReader(new InputStreamReader(new FileInputStream(path), charset),
              bufSize);
    }

    public BufferedReader newFileReader(String path, String charset)
          throws UnsupportedEncodingException, FileNotFoundException {
        return newFileReader(path, charset, 8192);
    }

    public BufferedWriter newFileWiter(String path, String charset)
          throws UnsupportedEncodingException, FileNotFoundException {

        if (charset == null) {
            charset = "ASCII";
        }
        int BUFFER = 1024 * 1024 * 12 * 4; // 12 * 4 MB


        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), charset),
              BUFFER);
    }

    public DataOutputStream newDataOutputStreamCompresed(String path) throws IOException {

        return new DataOutputStream(
              new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(path))));
    }

    public DataInputStream newDataInputStreamCompressed(String path) throws IOException {
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(path));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IOUtils.copy(in, bytes);
        return new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
    }


    public DataInputStream newDataInputStream(String path) throws FileNotFoundException {
        return new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
    }

    public double roundToDecimals(double num, int dec) {
        return Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
    }

    public float roundToDecimals(float num, int dec) {
        return (float) (Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec));
    }

    public Map<String, Double> getMassesDigest(String seq, int minAa, int maxAa, double maxMass) {
        final List<String> peptides = tripsyn1mc(seq, minAa, maxAa);
        // 57 ~ 110 aminoki kis.

        Map<String, Double> cepaniceMasa = new HashMap<String, Double>(peptides.size());
        for (String cepa : peptides) {
            if (StringUtils.containsAny(cepa, 'X', 'B', 'Z', 'J', 'O')) {
                continue;
            }

            final double mass = calculateMassWidthH2O(cepa);
            if (mass < maxMass) // ESI maxAa 6000Da
                cepaniceMasa.put(cepa, mass);
        }
        return cepaniceMasa;
    }

    /**
     * Read fasta file and call callback for each fasta sequence.
     */
    public void readLargeFasta(String path, Callback callback, long howMuchRead) throws IOException {

        BufferedReader reader = null;
        try {
            long count = 1;
            reader = newFileReader(path, StandardCharsets.US_ASCII.name());
            String line = null;
            StringBuilder seq = new StringBuilder(12345);
            String header = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(">")) {
                    if (!seq.isEmpty()) {
                        FastaSeq fs = new FastaSeq(header, seq.toString().toUpperCase().trim());
                        callback.readFasta(fs);
                        if (count == howMuchRead) {
                            log.debug("Finish reading fasta file: " + path);
                            return;
                        }
                        // seq = new StringBuilder();
                        seq.setLength(0);
                        count++;

                    }
                    header = line;
                } else {
                    seq.append(line);
                }

            }
            if (!seq.isEmpty()) {
                FastaSeq fs = new FastaSeq(header, seq.toString().toUpperCase());
                callback.readFasta(fs);
            }

        } finally {
            IOUtils.closeQuietly(reader);
        }

    }

    public interface Callback {
        void readFasta(FastaSeq seq) throws IOException;
    }

    public Set<FastaSeq> readFasta(String path, int num) throws IOException {
        int count = 0;
        FileInputStream in = new FileInputStream(new File(path));
        Set<FastaSeq> res = new HashSet<FastaSeq>();
        try {
            LineIterator lines = IOUtils.lineIterator(in, Charset.defaultCharset().name());
            StringBuilder seq = new StringBuilder();
            String header = null;
            while (lines.hasNext()) {
                // for (String line : lines) {
                String line = lines.next();
                if (line.startsWith(">")) {
                    if (count++ > num) {
                        break;
                    }
                    if (!seq.isEmpty()) {
                        FastaSeq fs = new FastaSeq(header, seq.toString());
                        res.add(fs);
                        seq = new StringBuilder();
                    }
                    header = line;
                } else {
                    seq.append(line);

                }
            }
            if (!seq.isEmpty()) {
                FastaSeq fs = new FastaSeq(header, seq.toString().toUpperCase());
                res.add(fs);
            }

            return res;

        } finally {
            IOUtils.closeQuietly(in);
        }
    }


    /**
     * (C-term to F/Y/W, not before P).
     * 1 missed cleavage only. Cleaves after F, W, or Y if next is not P. All chars must be upper case!
     */
    public List<String> chymotrypsin1mc(String prot, int min, int max) {

        // sites is the list of positions where the cleavage occurs
        List<Integer> sites = new ArrayList<>();
        for (int i = 0; i < prot.length() - 1; i++) {
            char current = prot.charAt(i);
            char next = prot.charAt(i + 1);
            if ((current == 'F' || current == 'W' || current == 'Y') && next != 'P') {
                sites.add(i + 1);
            }
        }
        List<String> peptides = new ArrayList<>();
        int n = prot.length();

        List<Integer> extendedSites = new ArrayList<>(sites.size() + 2);
        extendedSites.add(0);
        extendedSites.addAll(sites);
        extendedSites.add(n);

        for (int i = 0; i < extendedSites.size() - 1; i++) {
            int start = extendedSites.get(i);
            int end = extendedSites.get(i + 1);
            String peptide = prot.substring(start, end);
            if (peptide.length() >= min && peptide.length() <= max) {
                peptides.add(peptide);
            }
        }

        for (int i = 0; i < extendedSites.size() - 2; i++) {
            int start = extendedSites.get(i);
            int end = extendedSites.get(i + 2);
            String peptide = prot.substring(start, end);
            if (peptide.length() >= min && peptide.length() <= max) {
                peptides.add(peptide);
            }
        }
        return peptides;

    }

    /**
     * Fast method for trypsin cleaving with 2 miss cleavage. Cleaves after R or K if next is not P. All chars must be upper case!
     *
     * @param prot protein sequence
     * @param min  minimal length of peptide
     * @param max  maximal length of peptide
     * @return list of peptides
     */
    public List<String> tripsyn2mc(String prot, int min, int max) {
        if (prot.isEmpty()) {
            return new ArrayList<>();
        }

        final int numberOfRandK = StringUtils.countMatches(prot, "R") + StringUtils.countMatches(prot, "K");
        if (numberOfRandK == 0) {
            List<String> result = new ArrayList<>(1);
            if (prot.length() >= min && prot.length() <= max)
                result.add(prot);
            return result;
        }

        // Pre-calculate cleavage sites
        List<Integer> sites = new ArrayList<>(numberOfRandK + 2);
        sites.add(0); // Start position

        for (int i = 0; i < prot.length(); i++) {
            final char charAt = prot.charAt(i);
            if (charAt == 'R' || charAt == 'K') {
                if (i + 1 < prot.length() && prot.charAt(i + 1) == 'P') {
                    continue; // Skip if next amino acid is P
                }
                sites.add(i + 1); // Position after cleavage site
            }
        }

        // Only add end position if it's different from the last cleavage site
        if (sites.get(sites.size() - 1) != prot.length()) {
            sites.add(prot.length()); // End position
        }

        List<String> result = new ArrayList<>(numberOfRandK * 6); // Estimate capacity

        // Generate peptides with 0, 1, and 2 missed cleavages
        for (int mc = 0; mc <= 2; mc++) {
            for (int i = 0; i < sites.size() - 1 - mc; i++) {
                int start = sites.get(i);
                int end = sites.get(i + 1 + mc);

                if (end > start && end - start >= min && end - start <= max) {
                    result.add(prot.substring(start, end));
                }
            }
        }

        return result;
    }

    /**
     * Fast method for trypsin cleaving with 1 miss cleavage. Cleaves after R or K if next is not P. All chars must be upper case!
     *
     * @param prot protein sequence
     * @param min  minimal length of peptide
     * @param max  maximal length of peptide
     * @return list of peptides
     */
    public List<String> tripsyn1mc(String prot, int min, int max) {
        if (prot.isEmpty()) {
            return new ArrayList<>();
        }

        final int numberOfRandK = StringUtils.countMatches(prot, "R") + StringUtils.countMatches(prot, "K");
        if (numberOfRandK == 0) {
            List<String> result = new ArrayList<>(1);
            if (prot.length() >= min && prot.length() <= max)
                result.add(prot);
            return result;
        }

        // Pre-calculate cleavage sites
        List<Integer> sites = new ArrayList<>(numberOfRandK + 2);
        sites.add(0); // Start position

        for (int i = 0; i < prot.length(); i++) {
            final char charAt = prot.charAt(i);
            if (charAt == 'R' || charAt == 'K') {
                if (i + 1 < prot.length() && prot.charAt(i + 1) == 'P') {
                    continue; // Skip if next amino acid is P
                }
                sites.add(i + 1); // Position after cleavage site
            }
        }

        // Only add end position if it's different from the last cleavage site
        if (sites.get(sites.size() - 1) != prot.length()) {
            sites.add(prot.length()); // End position
        }

        List<String> result = new ArrayList<>(numberOfRandK * 4); // Estimate capacity

        // Generate peptides with 0 and 1 missed cleavages
        for (int mc = 0; mc <= 1; mc++) {
            for (int i = 0; i < sites.size() - 1 - mc; i++) {
                int start = sites.get(i);
                int end = sites.get(i + 1 + mc);

                if (end > start && end - start >= min && end - start <= max) {
                    result.add(prot.substring(start, end));
                }
            }
        }

        return result;
    }

    /**
     * This is mass that is in the database! This mass takes as it usually calculates
     * peptide mass other libs. New fast calculation of mass + water 18.01.
     */
    public double calculateMassWidthH2O(final String peptide) {
        double h = 0;
        try {
            for (int i = 0; i < peptide.length(); i++) {
                h += getMassFromAAfast(peptide.charAt(i));
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage() + " peptide: " + peptide, e);
        }
        return h + H2O;
    }

    public double getMassFromAAfast(final char aa) {
        return switch (aa) {
            case 'G' -> 57.021463724D;
            case 'A' -> 71.03711379D;
            case 'S' -> 87.03202841D;
            case 'P' -> 97.05276385D;
            case 'V' -> 99.06841392D;
            case 'T' -> 101.04767847D;
            case 'C' -> 103.00918448D;
            case 'I', 'J', 'L' -> 113.08406398D;
            case 'N' -> 114.04292745D;
            case 'D' -> 115.02694303D;
            case 'Q' -> 128.05857751D;
            case 'K' -> 128.09496302D;
            case 'E' -> 129.0425931D;
            case 'M' -> 131.04048461D;
            case 'H' -> 137.05891186D;
            case 'F' -> 147.0684139162D;
            case 'R' -> 156.10111103D;
            case 'Y' -> 163.06332854D;
            case 'W' -> 186.07931295D;
            case 'U' -> 150.95363559D;
            case 'O' -> 114.0793129535D;

            default -> throw new RuntimeException("Wrong AA '" + aa + "' ");
        };
    }

    public void printMemoryUsage(String msg) {
        int gb = 1024 * 1024 * 1024;
        long mem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / gb;

        System.out.println(msg + ". Memory:  " + mem + " GB");
    }

    public void printTimeDurration(StopWatch stopWatch) {
        System.out.println("Time duration: " + DurationFormatUtils.formatDurationHMS(stopWatch.getTime()));
    }

    public String[] fastSplit(String string, char delimiter) {
        /*
         * fastpath of String.split()
         *
         * [NOTE] it will remove empty token in the end it will not remove in-between
         * empty tokens the same behavior as String.split(String regex)
         *
         * [EXAMPLE] string = "boo\tboo\tboo\t\t\tboo\t\t\t\t\t"; strings =
         * fastSplit(string, '\t') -> [boo, boo, boo, , , boo]
         */
        int off = 0;
        int next = 0;
        ArrayList<String> list = new ArrayList<>();
        while ((next = string.indexOf(delimiter, off)) != -1) {
            list.add(string.substring(off, next));
            off = next + 1;
        }
        // If no match was found, return this
        if (off == 0)
            return new String[]{string};

        // Add remaining segment
        list.add(string.substring(off));

        // Construct result
        int resultSize = list.size();
        while (resultSize > 0 && list.get(resultSize - 1).isEmpty())
            resultSize--;
        String[] result = new String[resultSize];
        return list.subList(0, resultSize).toArray(result);
    }


}
