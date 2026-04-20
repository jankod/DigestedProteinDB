package hr.pbf.digestdb.tools;

import hr.pbf.digestdb.db.AccessionDbReader;
import hr.pbf.digestdb.db.MassRocksDbReader;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.MyUtil;
import org.apache.commons.lang3.RandomUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import java.io.PrintWriter;
import java.util.*;
import java.util.Locale;

import static org.apache.commons.math3.stat.StatUtils.mean;

public class BenchmarkRunner {

    static final int BATCH_SIZE = 10_000;
    static final double TOLERANCE_DA = 0.1; // ±0.1 Da mass window
    private static double MIN_MASS;
    private static double MAX_MASS;

    public static void main(String[] args) throws Exception {
        String dbDirPath = "./rocksdb_mass.db";
        String accDbPath = "./custom_accession.db";
        if (args.length > 0) {
            dbDirPath = args[0];
            accDbPath = args[1];
        }
        dbDirPath = "/disk-2tb/digested-db/db_trembl_trypsin/rocksdb_mass.db";
        accDbPath = "/disk-2tb/digested-db/db_trembl_trypsin/custom_accession.db";
        MassRocksDbReader massDb = new MassRocksDbReader(dbDirPath);
        AccessionDbReader accDb = new AccessionDbReader(accDbPath);

        detectMinAndMax(massDb);
        System.out.println("Min mass: " + MIN_MASS + ", max mass: " + MAX_MASS);


        List<Double> queryMasses = generateRealisticMasses();

        // JIT warmup: run a few hundred queries before the timed benchmark
        // to ensure HotSpot has compiled the hot methods and results are not
        // skewed by interpreter overhead in the first iterations.
        int WARMUP = 1000;
        for (int i = 0; i < WARMUP; i++) {
            double mass = queryMasses.get(i % queryMasses.size());
            massDb.searchByMass(mass - TOLERANCE_DA, mass + TOLERANCE_DA, 1, Integer.MAX_VALUE);
        }
        System.out.println("Warmup done. Starting benchmark...");

        long[] perQueryNanos = new long[BATCH_SIZE];
        long totalSink = 0; // accumulated value consumed at the end to prevent JIT dead-code elimination
        long totalStart = System.nanoTime();

        PrintWriter pw = new PrintWriter("benchmark_distribution.csv");
        pw.println("mass,hit_count,latency_ms");

        for (int i = 0; i < BATCH_SIZE; i++) {
            double mass = queryMasses.get(i);
            double lo = mass - TOLERANCE_DA;
            double hi = mass + TOLERANCE_DA;

            long t0 = System.nanoTime();

            // Step 1: key–value range scan in RocksDB
            // Step 2: deserialization of peptide sequences (5-bit encoding, varint parsing)
            // Step 3: mapping of internal integer IDs to UniProt accession strings
            MassRocksDbReader.MassPageResult results = massDb.searchByMass(lo, hi, 1, Integer.MAX_VALUE);

            int hitCount = 0;
            for (int i1 = 0; i1 < results.getResults().size(); i1++) {
                Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>> entry = results.getResults().get(i1);
                Set<BinaryPeptideDbUtil.PeptideAccids> peptideAccids = entry.getValue();
                for (BinaryPeptideDbUtil.PeptideAccids peptideAccid : peptideAccids) {
                    hitCount++;
                    // Resolve ALL accession IDs for this peptide (not just the first one)
                    for (int iAcc = 0; iAcc < peptideAccid.getAccids().length; iAcc++) {
                        String accession = accDb.getAccession(peptideAccid.getAccids()[iAcc]);
                        totalSink += accession.length(); // consume result to prevent JIT elimination
                    }
                }
            }

            perQueryNanos[i] = System.nanoTime() - t0;
            pw.printf(Locale.US, "%.4f,%d,%.4f%n", mass, hitCount, perQueryNanos[i] / 1_000_000.0);
        }

        pw.close();
        System.out.println("Distribution data saved to benchmark_distribution.csv");

        long totalNanos = System.nanoTime() - totalStart;

        // Print sink so the JIT cannot remove any of the above code as dead code
        System.out.println("(sink=" + totalSink + ", ignore this value)");

        // --- Latency statistics ---
        double[] ms = Arrays.stream(perQueryNanos)
                .mapToDouble(n -> n / 1_000_000.0)
                .toArray();
        Arrays.sort(ms);

        System.out.printf("Total (10k queries): %.2f s%n", totalNanos / 1e9);
        System.out.printf("Mean per query:      %.2f ms%n", mean(ms));
        System.out.printf("Median (P50):        %.2f ms%n", ms[BATCH_SIZE / 2]);
        // P95: 95% of queries finished faster than this value; only 5% were slower.
        System.out.printf("P95:                 %.2f ms%n", ms[(int) (BATCH_SIZE * 0.95)]);
        // P99: 99% of queries finished faster than this value; only 1% were slower (tail latency).
        System.out.printf("P99:                 %.2f ms%n", ms[(int) (BATCH_SIZE * 0.99)]);
        // Max: the single slowest query in the entire batch (worst-case latency).
        System.out.printf("Max:                 %.2f ms%n", ms[BATCH_SIZE - 1]);
    }

    private static void detectMinAndMax(MassRocksDbReader massDb) {
        RocksDB db = massDb.getDb();
        try (RocksIterator iter = db.newIterator()) {
            // Smallest key
            iter.seekToFirst();
            if (iter.isValid()) {
                byte[] minKey = iter.key();
                //System.out.println("Min key: " + decodeMassKey(minKey));
                MIN_MASS = decodeMassKey(minKey);
            }

            // Largest key
            iter.seekToLast();
            if (iter.isValid()) {
                byte[] maxKey = iter.key();
                //System.out.println("Max key: " + decodeMassKey(maxKey));
                MAX_MASS = decodeMassKey(maxKey);
            }
        }
    }

    private static double decodeMassKey(byte[] key) {
        if (key == null || key.length != Integer.BYTES) {
            throw new IllegalArgumentException("Unexpected key length: " + (key == null ? -1 : key.length));
        }
        int massInt = MyUtil.byteArrayToInt(key);
        return massInt / 10_000.0;
    }

    private static List<Double> generateRealisticMasses() {
        List<Double> masses = new ArrayList<>(BenchmarkRunner.BATCH_SIZE);
        for (int i = 0; i < BenchmarkRunner.BATCH_SIZE; i++) {
            masses.add(RandomUtils.nextDouble(MIN_MASS, MAX_MASS));

        }
        return masses;
    }
}

// Suggested paper text:
// "To characterize end-to-end query performance, a batch benchmark of 10,000 mass-range queries
// (±0.1 Da tolerance, masses sampled uniformly from 360.1394–8532.7585 Da) was executed against the full
// TrEMBL-trypsin database instance. Measured latency encompassed the complete query path, including
// RocksDB range scan, peptide deserialization (5-bit decoding, varint parsing), and resolution of
// internal accession identifiers to UniProt accession strings. The total wall-clock time for 10,000
// queries was X s (mean: Y ms per query, median: Z ms, P95: W ms, P99: V ms)."
