package hr.pbf.digestdb.uniprot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.collections.impl.list.mutable.primitive.FloatArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.UniprotConfig;
import hr.pbf.digestdb.util.UniprotConfig.Name;
import me.lemire.integercompression.differential.IntegratedIntCompressor;

/**
 * Compress 13.353.898 => 850.924 sorted masses to int
 *
 * @author tag
 */
public class MassPeptideArrayIndex {
    private static final Logger log = LoggerFactory.getLogger(MassPeptideArrayIndex.class);

    public static final int massesNum = 13_353_898;
    public static final int massesNumCompressed = 850_924;

    private float[] masses;
    private int[] peptides;

    public static void main(String[] args) throws IOException {
        // testSave();
        testRead();

    }

    private static void testRead() throws IOException {
        String pathCsv = UniprotConfig.get(Name.PATH_TREMB_LEVELDB_INDEX_CSV);
        StopWatch s = new StopWatch();
        s.start();
        MassPeptideArrayIndex index = MassPeptideArrayIndex.loadFromFile(pathCsv + ".masses", pathCsv + ".peptides",
                -1);
        UniprotUtil.printDurration("Load index from file ", s);
        log.debug("m len {}, poss lenn {}", index.masses.length, index.peptides.length);

        SubMassPeptideIndex res = index.search(1000, 1100);
        log.debug("Result rearch " + res);

    }

    private static void testSave() throws IOException {
        String pathCsv = UniprotConfig.get(Name.PATH_TREMB_LEVELDB_INDEX_CSV);
        StopWatch time = new StopWatch();
        time.start();
        MassPeptideArrayIndex index = MassPeptideArrayIndex.buildFromCSV(pathCsv, 13_353_898);
        time.stop();
        log.debug("loading from csv " + DurationFormatUtils.formatDurationHMS(time.getTime()));
        log.debug("index countaint " + index.getLowestMass() + " " + index.getHigestMass());
        log.debug("length   " + index.masses.length);

        NumberFormat numFormat = DecimalFormat.getIntegerInstance();
        {// save peptides to disk
            time = new StopWatch();
            time.start();
            // int[] compress = iic.compress(index.peptides);
            store(new File(pathCsv + ".peptides"), index.peptides);
            time.stop();

            log.debug("Durration save peptides " + DurationFormatUtils.formatDurationHMS(time.getTime()));
        }

        {// save masses
            time = new StopWatch();
            time.start();
            IntegratedIntCompressor iic = new IntegratedIntCompressor();
            int[] massesAsInt = MassPeptideArrayIndex.getMassesAsIntArray(index.masses);
            int[] compress = iic.compress(massesAsInt);
            store(new File(pathCsv + ".masses"), compress);
            time.stop();
            log.debug("Compress {} => {}", numFormat.format(index.peptides.length), numFormat.format(compress.length));
            log.debug("Durration save masses" + DurationFormatUtils.formatDurationHMS(time.getTime()));
        }
    }

    public static float[] toFloatFromInt(int[] intArray) {
        float[] f = new float[intArray.length];
        int c = 0;
        for (int i : intArray) {
            f[c++] = Float.intBitsToFloat(i);
        }
        return f;
    }

    public static int[] getMassesAsIntArray(float[] arrFloat) {
        int[] r = new int[arrFloat.length];
        int c = 0;
        for (float m : arrFloat) {
            r[c++] = Float.floatToIntBits(m);
        }
        return r;
    }

    public MassPeptideArrayIndex(int size) {
        masses = new float[size];
        peptides = new int[size];
    }

    public MassPeptideArrayIndex(float[] masses, int[] peptides) {
        if (masses.length != peptides.length) {
            throw new RuntimeException("Not same length");
        }
        this.masses = masses;
        this.peptides = peptides;
    }

    public static MassPeptideArrayIndex loadFromFile(String pathMasses, String pathPeptides, int lengthArray)
            throws IOException {
        int[] massesIntCompresed = load(new File(pathMasses), -1);
        IntegratedIntCompressor iic = new IntegratedIntCompressor();
        int[] massesInt = iic.uncompress(massesIntCompresed);
        float[] massesArray = toFloatFromInt(massesInt);

        int[] peptidesArray = load(new File(pathPeptides), -1);
        MassPeptideArrayIndex index = new MassPeptideArrayIndex(massesArray, peptidesArray);
        return index;
    }

    public static MassPeptideArrayIndex buildFromCSV(String pathCsv, int lengthArray) throws IOException {
        BufferedReader reader = BioUtil.newFileReader(pathCsv);
        // Format is:
        // mass unique_peptides peptides
        // 500.18015 49 133

        StringTokenizer tokenizer = new StringTokenizer("");
        String line = reader.readLine(); // skip header

        MassPeptideArrayIndex result = new MassPeptideArrayIndex(lengthArray);

        int c = 0;
        // int lines = 0;
        while ((line = reader.readLine()) != null) {
            // lines++;
            tokenizer = new StringTokenizer(line, "\t");
            float mass = Float.parseFloat(tokenizer.nextToken());
            int uniquePeptidesNum = Integer.parseInt(tokenizer.nextToken());
            int peptidesNum = Integer.parseInt(tokenizer.nextToken());
            result.masses[c] = mass;
            result.peptides[c] = peptidesNum;
            c++;
        }
        // log.debug("lines " + lines);
        return result;

    }

    public void put(int index, float mass, int howPeptides) {
        masses[index] = mass;
        peptides[index] = howPeptides;
    }

    public SubMassPeptideIndex search(float from, float to) {
        if (from > to) {
            throw new RuntimeException("From is more than to");
        }

        int indexLeft = Math.abs(Arrays.binarySearch(masses, from));
        int indexRight = Math.abs(Arrays.binarySearch(masses, to));

        log.debug(from + ":" + to + " index= " + indexLeft + ":" + indexRight);

        if (indexLeft == -1 || indexRight == -1 || indexLeft > indexRight) {
            return new SubMassPeptideIndex(new float[0], new int[0]);
        }

        float[] subMass = ArrayUtils.subarray(masses, indexLeft, indexRight);
        int[] subPeptides = ArrayUtils.subarray(peptides, indexLeft, indexRight);

        return new SubMassPeptideIndex(subMass, subPeptides);
    }

    private float getHigestMass() {
        if (masses == null || masses.length == 0) {
            return 6000f;
        }
        return masses[masses.length - 1];
    }

    private float getLowestMass() {
        if (masses == null || masses.length == 0) {
            return 0;
        }
        return masses[0];
    }

    public static int[] loadByNIO(int argNumPrimes, String fname) {
        int numPrimes = Math.min(argNumPrimes, 50_000_000);
        int[] primes = new int[numPrimes];
        try (FileChannel fc = FileChannel.open(Paths.get(fname))) {
            MappedByteBuffer mbb = fc.map(MapMode.READ_ONLY, 0, numPrimes * 4l);
            for (int i = 0; i < numPrimes; i++) {
                primes[i] = mbb.getInt();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return primes;
    }

    static int[] load(final File file, int arrayLiength) throws IOException {

        if (arrayLiength == -1) {
            arrayLiength = (int) (file.length() / 4);
        }
        try (final FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            final MappedByteBuffer mapping = channel.map(FileChannel.MapMode.READ_ONLY, 0, // offset
                    arrayLiength * Integer.BYTES // length
            );
            mapping.order(ByteOrder.nativeOrder());
            final IntBuffer integers = mapping.asIntBuffer();
            final int[] array = new int[arrayLiength];
            integers.get(array);
            return array;
        }
    }

    public static void store(final File file, final int[] array) throws IOException {
        try (final FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ,
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            final MappedByteBuffer mapping = channel.map(FileChannel.MapMode.READ_WRITE, 0, // offset
                    array.length * Integer.BYTES // length
            );
            mapping.order(ByteOrder.nativeOrder());
            final IntBuffer integers = mapping.asIntBuffer();
            integers.put(array);
        }
    }

    public static class SubMassPeptideIndex {
        // TODO: not copy array, use reference instead like Map
        public SubMassPeptideIndex(float[] subMass, int[] subPeptides) {
            masses = subMass;
            peptides = subPeptides;
        }

        public float[] masses;
        public int[] peptides;

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            if (masses != null) {
                int c = 0;
                b.append("Length " + masses.length).append("\n");
                for (float f : masses) {
                    b.append(f).append("\t").append(peptides[c++]).append("\n");
                    if (c > 30) {
                        b.append("...to more " + DecimalFormat.getInstance().format(masses.length));
                        this.addLast10(b);
                        break;
                    }
                }
            } else {
                b.append("Null masses ");
            }
            return b.toString();
        }

        private void addLast10(StringBuilder b) {
            for (int i = masses.length - 10; i < masses.length; i++) {
                b.append(masses[i]).append("\t").append(peptides[i]).append("\n");
            }
        }
    }

}
