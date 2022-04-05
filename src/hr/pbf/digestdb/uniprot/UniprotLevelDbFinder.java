package hr.pbf.digestdb.uniprot;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.ImmutableRangeMap.Builder;
import com.google.common.collect.Range;
import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxNames;
import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.LevelDButil;
import hr.pbf.digestdb.util.UniprotConfig;
import hr.pbf.digestdb.util.UniprotConfig.Name;
import hr.pbf.digestdb.web.ServletUtil;
import lombok.Data;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBComparator;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.table.MMapTable;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentNavigableMap;

public class UniprotLevelDbFinder implements Closeable {

    private DB db;
    private static final Logger log = LoggerFactory.getLogger(UniprotLevelDbFinder.class);
    private SortedTableMap<Float, Integer> mapIndex;
    private DB dbProtName;

    public static void main(String[] args) throws IOException {

        try (UniprotLevelDbFinder f = new UniprotLevelDbFinder(UniprotConfig.get(Name.PATH_TREMB_LEVELDB),
                UniprotConfig.get(Name.PATH_TREMBL_MASS_PEPTIDES_MAP),
                UniprotConfig.get(Name.PATH_TREMBL_PROT_NAMES_LEVELDB))) {

            float fromMass = 3731.7937F;
            float toMass = (float) (fromMass + 0.33421);


            fromMass = 400F;
            toMass = 500.15f;


            IndexResult result = f.searchIndex(fromMass, toMass);
            log.debug("Search {} : {}", fromMass, toMass);

            printIndex(result.map);

            DBIterator it = f.db.iterator();

            it.seek(BiteUtil.toBytes(fromMass));
            int countMass = 0;
            int countUniquePeptides = 0;

            while (it.hasNext()) {
                Map.Entry<byte[], byte[]> entry = (Map.Entry<byte[], byte[]>) it.next();
                float mass = BiteUtil.toFloat(entry.getKey());
                if (mass > toMass) {
                    break;
                }
                countMass++;
                TreeMap<String, List<AccTax>> v = UniprotFormat3.uncompressPeptidesJava(entry.getValue());
                countUniquePeptides += v.size();
                long totalPeptides = countTotalAccTax(v);
                log.debug(mass + "\t" + v.size() + "\t" + totalPeptides);

            }
            log.debug("total mass {}, peptides: {}", countMass, countUniquePeptides);
            it.close();
        }

    }

    public DB getDb() {
        return db;
    }

    private static void printIndex(SortedMap<Float, Integer> index) {
        for (Entry<Float, Integer> entry : index.entrySet()) {
            log.debug(entry.getKey() + "\t" + entry.getValue());
        }

    }

    private static long countTotalAccTax(Map<String, List<AccTax>> v) {
        long t = 0;
        for (Entry<String, List<AccTax>> entry : v.entrySet()) {
            t += entry.getValue().size();
        }
        return t;
    }

    public List<UniprotModel.PeptideAccTaxNames> findData2(HttpServletRequest req) {
        ArrayList<PeptideAccTaxNames> result = new ArrayList<>();
        try {
            double fromMass = ServletUtil.getDouble(req, "massFrom", 500d);
            double toMass = ServletUtil.getDouble(req, "massTo", 500 + 20d);
            int page = 1;
            int recordsPerPage = 10;
            if (req.getParameter("page") != null) {
                page = Integer.parseInt(req.getParameter("page"));
            }

            IndexResult index = searchIndex(fromMass, toMass);
            log.debug(index.toString());

            int start = (page - 1) * recordsPerPage;
            int end = start + recordsPerPage;
            log.debug("\nStart:stop %s: %s", start, end);
            ImmutableRangeMap<Integer, Float> range = index.toRangeMap(start, end);
            log.debug(range.toString());
            Float mass = range.get(start);
            int thisMassStartOf = range.getEntry(start).getKey().lowerEndpoint();
            ImmutableRangeMap<Integer, Float> subRangeMap = range.subRangeMap(Range.closedOpen(start, end));

            TwoMass low = getLowerUpperMass(subRangeMap);

            List<PeptideAccTaxNames> masses = searchMass(low.start, low.end, 100);
            int cc = 0;
            System.out.printf("\nthisMassStartOf %s ", thisMassStartOf);

            log.debug("\nMass %s \n", mass);
            for (PeptideAccTaxNames peptideAccTaxNames : masses) {
                log.debug(cc++ + " " + peptideAccTaxNames);
            }

            SearchOneMassResult searchMass = searchMass(mass);

            for (int i = 0; i < recordsPerPage; i++) {
                result.add(new PeptideAccTaxNames(i, "peptiode " + i, "ACC " + i, "peot name", "taxName", 3434));

            }
            page++;
            req.setAttribute("page", page);
        } catch (Throwable e) {
            log.debug("", e);
        }

        return result;
    }

    private TwoMass getLowerUpperMass(ImmutableRangeMap<Integer, Float> m) {
        TwoMass t = new TwoMass();

        ImmutableCollection<Float> values = m.asMapOfRanges().values();
        for (Float mass : values) {
            if (t.start == null) {
                t.start = mass;
            }
            if (t.end == null) {
                t.end = mass;
            }
            t.start = Math.min(t.start, mass);
            t.end = Math.max(t.end, mass);
        }
        return t;
    }

    class TwoMass {
        public Float start, end;

    }

    public UniprotLevelDbFinder() throws IOException {
        this(UniprotConfig.get(Name.PATH_TREMB_LEVELDB), UniprotConfig.get(Name.PATH_TREMBL_MASS_PEPTIDES_MAP),
                UniprotConfig.get(Name.PATH_TREMBL_PROT_NAMES_LEVELDB));
    }

    public UniprotLevelDbFinder(String levelDbPath, String indexSSTablePath, String protNameLevelDbPath)
            throws IOException {
        DBComparator comparator = LevelDButil.getFloatKeyComparator();
        Options opt = LevelDButil.getStandardOptions();
        opt.comparator(comparator);
        if (!new File(levelDbPath).isDirectory()) {
            throw new IOException("Not a  dir " + levelDbPath);
        }
        db = LevelDButil.open(levelDbPath, opt);
        String status = LevelDButil.getStatus(db);
        log.info("Leveldb status: " + status);


        Volume volume = MappedFileVol.FACTORY.makeVolume(indexSSTablePath, true);
        mapIndex = SortedTableMap.open(volume, Serializer.FLOAT, Serializer.INTEGER);

        String protNamePalPath = protNameLevelDbPath; // "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_prot_names.leveldb";
        dbProtName = LevelDButil.open(protNamePalPath, LevelDButil.getStandardOptions());

    }

    public String getProtName(String acc) {
        // return protNameReader.getString(acc);
        // return mapAccProtName.get(acc);
        DBIterator it = dbProtName.iterator();
        it.seek(acc.getBytes(StandardCharsets.US_ASCII));
        if (it.hasNext()) {
            Entry<byte[], byte[]> entry = it.next();
            String accFounded = new String(entry.getKey(), StandardCharsets.US_ASCII);
            if (!accFounded.equals(acc)) {
                log.warn("Not find: {} {}", acc, accFounded);
                return null;
            }
            String protName = new String(entry.getValue(), StandardCharsets.US_ASCII);
            return protName;
        }
        return null;
        // return new String(r);
    }

    public SortedTableMap<Float, Integer> getMapIndex() {
        return mapIndex;
    }

    @Override
    public void close() throws IOException {
        if (db != null) {
            db.close();
        }
        if (mapIndex != null) {
            mapIndex.close();
        }

        if (db != null) {
            db.close();
        }
        log.debug("CLOSE regular database");
    }

    public List<UniprotModel.PeptideAccTaxNames> searchMass(double mass, double to, int maxEntryReturn) throws IOException {
        DBIterator it = db.iterator();
        it.seek(BiteUtil.toBytes((float) mass));
        List<PeptideAccTaxNames> result = new ArrayList<>();
        int counter = 0;
        while (it.hasNext()) {
            Entry<byte[], byte[]> next = it.next();
            float massExact = BiteUtil.toFloat(next.getKey());
            if (massExact > to) {
                break;
            }
            MMapTable table;
            if(++counter > maxEntryReturn) {
                break;
            }

            TreeMap<String, List<AccTax>> v = UniprotFormat3.uncompressPeptidesJava(next.getValue());
            Set<Entry<String, List<AccTax>>> entrySet = v.entrySet();
            log.debug(massExact +" \t"+ countTotalAccTax(v));

            for (Entry<String, List<AccTax>> entry : entrySet) {
                List<AccTax> value = entry.getValue();
                String peptide = entry.getKey();
                for (AccTax accTax : value) {
                    String acc = accTax.getAcc();
                    int tax = accTax.getTax();
                    PeptideAccTaxNames p = new PeptideAccTaxNames();
                    p.setMass(massExact);
                    p.setAcc(acc);
                    p.setTax(tax);
                    p.setPeptide(peptide);
                    p.setProtName(getProtName(acc));
                    result.add(p);
                }
            }
        }
        it.close();
        return result;
    }

    @Data
    public static class SearchOneMassResult {
        private List<UniprotModel.PeptideAccTaxNames> result;
        private ArrayList<Entry<Float, Integer>> massesAround;
    }

    public SearchOneMassResult searchMassOne(float searchMass, float margin) throws IOException {
        SearchOneMassResult res = new SearchOneMassResult();

        if (margin < 0f || margin > 0.3001f) {
            log.warn("Margin is not in range " + margin);
            margin = 0.1f;
        }
        DBIterator it = db.iterator();
        it.seek(BiteUtil.toBytes(searchMass - margin));
        List<PeptideAccTaxNames> result = new ArrayList<>();
        res.setResult(result);

        if (!it.hasNext()) {
            log.warn("Find nothing for mass-margin: {} ", (searchMass - margin));
        } else {
            while (it.hasNext()) {
                Entry<byte[], byte[]> next = it.next();
                float foundMass = BiteUtil.toFloat(next.getKey());
                if (foundMass > searchMass + margin) {
                    break;
                }
                // res.setMassesAround(getMassesAround(foundMass));
                TreeMap<String, List<AccTax>> v = UniprotFormat3.uncompressPeptidesJava(next.getValue());
                Set<Entry<String, List<AccTax>>> entrySet = v.entrySet();

                populateResult(result, foundMass, entrySet);
                log.debug(foundMass + "");
            }
        }
        it.close();
        // log.warn("Nothig found for mass {} +- {} ", searchMass, margin);
        return res;
    }

    private void populateResult(List<PeptideAccTaxNames> result, float mass2,
                                Set<Entry<String, List<AccTax>>> entrySet) {
        for (Entry<String, List<AccTax>> entry : entrySet) {
            List<AccTax> value = entry.getValue();
            String peptide = entry.getKey();
            //	System.out.println(mass2 + "\t" + peptide);
            for (AccTax accTax : value) {
                String acc = accTax.getAcc();
                int tax = accTax.getTax();
                PeptideAccTaxNames p = new PeptideAccTaxNames();
                p.setAcc(acc);
                p.setTax(tax);
                p.setPeptide(peptide);
                p.setProtName(getProtName(acc));
                p.setMass(mass2);
                result.add(p);
                // System.out.println(p);
                // result.add(new PeptideAccTax(peptide, acc, tax));
            }
        }
    }

    public SearchOneMassResult searchMass(double mass) throws IOException {
        SearchOneMassResult r = new SearchOneMassResult();
        DBIterator it = db.iterator();
        it.seek(BiteUtil.toBytes((float) mass));
        if (it.hasNext()) {
            Entry<byte[], byte[]> next = it.next();
            float mass2 = BiteUtil.toFloat(next.getKey());
            r.setMassesAround(getMassesAround(mass2));
            // if (mass != mass2) {
            // log.warn("Not the some mass {}:{}", mass, mass2);
            // return Collections.emptyList();
            // }
            TreeMap<String, List<AccTax>> v = UniprotFormat3.uncompressPeptidesJava(next.getValue());
            Set<Entry<String, List<AccTax>>> entrySet = v.entrySet();

            List<PeptideAccTaxNames> result = new ArrayList<>();
            populateResult(result, mass2, entrySet);
            it.close();
            r.setResult(result);
            return r;
        }
        it.close();
        log.warn("Nothig found for mass: " + mass);
        return null;
    }

    private ArrayList<Entry<Float, Integer>> getMassesAround(float mass2) {
        ArrayList<Entry<Float, Integer>> masses = new ArrayList<Map.Entry<Float, Integer>>();

        Entry<Float, Integer> l1 = mapIndex.findLower(mass2, false);
        Entry<Float, Integer> l2 = mapIndex.findLower(l1.getKey(), false);
        masses.add(l2);
        masses.add(l1);
        masses.add(mapIndex.findLower(mass2, true));

        Entry<Float, Integer> h1 = mapIndex.findHigher(mass2, false);
        Entry<Float, Integer> h2 = mapIndex.findHigher(h1.getKey(), false);

        masses.add(h1);
        masses.add(h2);
        //System.out.println(masses);
        return masses;
        // System.out.println(l2.getKey() + " | "+ l1.getKey()+" | "+ mass2 +
        // " | "+ h1.getKey() + " | "+ h2.getKey());

    }

    public IndexResult searchIndex(double from, double to) {

        ConcurrentNavigableMap<Float, Integer> subMap = mapIndex.subMap((float) from, true, (float) to, true);

        // SortedMap<Float, Integer> map = indexMap.map((float) from, true,
        // (float) to, true);
        IndexResult result = new IndexResult();

        result.map = subMap;
        return result;
    }

    public static class IndexResult {

        public SortedMap<Float, Integer> map;

        public IndexResult() {
        }

        /**
         * Return mass float and position where to start with peptides in lelveldb database
         *
         * @param startPeptides
         * @return
         */
        public Pair<Float, Long> getStartMass(long startPeptides) {
            // f1 => 10
            // f2 => 10
            // f3 => 10

            // 8:24
            long fromPos = 0;
            Pair<Float, Long> startMass = null;
            for (Entry<Float, Integer> en : map.entrySet()) {
                long endPos = fromPos + en.getValue();

                if (startPeptides >= fromPos && startPeptides < endPos) {
                    long pos = startPeptides - fromPos;
                    startMass = new ImmutablePair<>(en.getKey(), pos);
                    return startMass;
                }
                fromPos += en.getValue();
            }

            return startMass;
        }

        public ImmutableRangeMap<Integer, Float> toRangeMap(long from, long to) {

            Builder<Integer, Float> builder = ImmutableRangeMap.<Integer, Float>builder();
            Set<Entry<Float, Integer>> entrySet = map.entrySet();
            int c = 0;
            for (Entry<Float, Integer> entry : entrySet) {
                int start = c;
                int stop = entry.getValue() + c;
                if (stop < from || start > to) {
                    c += entry.getValue();
                    continue;
                }
                //System.out.printf("\nRange %s:%s %s\n", start, stop, entry.getKey());
                builder.put(Range.open(start, stop), entry.getKey());
                c += entry.getValue();
            }
            ImmutableRangeMap<Integer, Float> r = builder.build();

            return r;
        }

        public int countMasses() {
            if (map == null) {
                return 0;
            }
            return map.size();
        }

        public long countTotalPeptides() {
            long c = 0;
            Set<Entry<Float, Integer>> entrySet = map.entrySet();
            for (Entry<Float, Integer> entry : entrySet) {
                c += entry.getValue();
            }
            return c;
        }

        @Override
        public String toString() {
            String res = "\nTotal mass: " + countMasses() + " Total peptides: " + countTotalPeptides();

            int c = 0;
            for (Entry<Float, Integer> set : map.entrySet()) {
                res += "\n" + set.getValue() + " " + set.getKey();
                if (c++ > 4) {
                    res += "\n..";
                    break;
                }
            }
            return res;

        }

        public String toStringHTML() {
            String res = "<br>Total mass: " + countMasses() + "<br>Total peptides: " + countTotalPeptides();

            for (Entry<Float, Integer> set : map.entrySet()) {
                res += "<br><a href='showMass.jsp?mass=" + set.getKey() + "'>" + set.getKey() + "</a> : "
                        + set.getValue();
            }
            return res;
        }

    }
}
