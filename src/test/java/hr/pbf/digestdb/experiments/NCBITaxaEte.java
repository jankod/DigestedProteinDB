package hr.pbf.digestdb.experiments;

import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;


/**
 * This is port of python ete3 NCBITaxa to Java.
 */
public class NCBITaxaEte implements AutoCloseable {
    private final Connection conn;

    //DEFAULT_TAXADB = os.path.join(os.environ.get('HOME', '/'), '.etetoolkit', 'taxa.sqlite')
    public final static String DEFAULT_TAXADB = System.getProperty("user.home") + "/.etetoolkit/taxa.sqlite";

    public NCBITaxaEte() {
        this(DEFAULT_TAXADB);
    }

    public NCBITaxaEte(String sqlitePath) {
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.setReadOnly(true); // mora prije connection-a
            this.conn = DriverManager.getConnection(
                  "jdbc:sqlite:" + sqlitePath,
                  config.toProperties()
            );
        } catch (SQLException e) {
            throw new RuntimeException("Cannot open SQLite: " + sqlitePath, e);
        }
    }

    public int dbVersion() {
        String sql = "SELECT version FROM stats";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* ------------------------- helpers ------------------------- */

    /**
     * Ako je taxid prebačen u merged tablici, vrati novi taxid; inače isti.
     */
    public int translateMerged(int taxid) {
        String sql = "SELECT taxid_new FROM merged WHERE taxid_old = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taxid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : taxid;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Grupna varijanta: vrati mapu old->new samo za one koji su merged.
     */
    public Map<Integer, Integer> translateMerged(Set<Integer> taxids) {
        if (taxids.isEmpty()) return Map.of();
        String in = taxids.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = "SELECT taxid_old, taxid_new FROM merged WHERE taxid_old IN (" + in + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Integer t : taxids) ps.setInt(i++, t);
            try (ResultSet rs = ps.executeQuery()) {
                Map<Integer, Integer> m = new HashMap<>();
                while (rs.next()) m.put(rs.getInt(1), rs.getInt(2));
                return m;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* ------------------------- queries ------------------------- */

    /**
     * taxid -> rank
     */
    public Map<Integer, String> getRank(Collection<Integer> taxids) {
        Set<Integer> ids = cleanIds(taxids);
        if (ids.isEmpty()) return Map.of();
        String in = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = "SELECT taxid, rank FROM species WHERE taxid IN (" + in + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Integer t : ids) ps.setInt(i++, t);
            try (ResultSet rs = ps.executeQuery()) {
                Map<Integer, String> m = new HashMap<>();
                while (rs.next()) m.put(rs.getInt(1), rs.getString(2));
                return m;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * taxid -> scientific name (pokušava i preko merged mape, vraća stare ključeve)
     */
    public Map<Integer, String> getTaxidTranslator(Collection<Integer> taxids, boolean trySynonyms) {
        Set<Integer> ids = cleanIds(taxids);
        if (ids.isEmpty()) return Map.of();

        Map<Integer, String> result = fetchSpnames(ids);

        if (trySynonyms && result.size() != ids.size()) {
            Set<Integer> missing = new HashSet<>(ids);
            missing.removeAll(result.keySet());
            Map<Integer, Integer> old2new = translateMerged(missing);
            if (!old2new.isEmpty()) {
                Set<Integer> newIds = new HashSet<>(old2new.values());
                Map<Integer, String> newSp = fetchSpnames(newIds);
                for (var e : newSp.entrySet()) {
                    // mapiraj na originalne (stare) ključeve
                    for (var oldEntry : old2new.entrySet()) {
                        if (Objects.equals(oldEntry.getValue(), e.getKey())) {
                            result.put(oldEntry.getKey(), e.getValue());
                        }
                    }
                }
            }
        }
        return result;
    }

    private Map<Integer, String> fetchSpnames(Set<Integer> ids) {
        String in = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = "SELECT taxid, spname FROM species WHERE taxid IN (" + in + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Integer t : ids) ps.setInt(i++, t);
            try (ResultSet rs = ps.executeQuery()) {
                Map<Integer, String> m = new HashMap<>();
                while (rs.next()) m.put(rs.getInt(1), rs.getString(2));
                return m;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * taxid -> common name (ako postoji)
     */
    public Map<Integer, String> getCommonNames(Collection<Integer> taxids) {
        Set<Integer> ids = cleanIds(taxids);
        if (ids.isEmpty()) return Map.of();
        String in = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = "SELECT taxid, common FROM species WHERE taxid IN (" + in + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Integer t : ids) ps.setInt(i++, t);
            Map<Integer, String> m = new HashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String c = rs.getString(2);
                    if (c != null && !c.isBlank()) m.put(rs.getInt(1), c);
                }
            }
            return m;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * taxid -> lineage (lista od root→…→taxid)
     */
    public Map<Integer, List<Integer>> getLineageTranslator(Collection<Integer> taxids) {
        Set<Integer> ids = cleanIds(taxids);
        if (ids.isEmpty()) return Map.of();
        String in = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = "SELECT taxid, track FROM species WHERE taxid IN (" + in + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Integer t : ids) ps.setInt(i++, t);
            Map<Integer, List<Integer>> m = new HashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int tax = rs.getInt(1);
                    String track = rs.getString(2);
                    List<Integer> lineage = parseTrack(track);    // DB ima track kao "node, ..., root" (obrnuto)
                    Collections.reverse(lineage);                 // root → … → node
                    m.put(tax, lineage);
                }
            }
            return m;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Jedan taxid → lineage (root→…→taxid). Uvažava merged.
     */
    public List<Integer> getLineage(int taxid) {
        int t = translateMerged(taxid);
        String sql = "SELECT track FROM species WHERE taxid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("taxid not found: " + taxid);
                List<Integer> lin = parseTrack(rs.getString(1));
                Collections.reverse(lin);
                return lin;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * točna pretraga imena (species + synonym), case-insensitive; vraća originalni ključ -> lista taxidova
     */
    public Map<String, List<Integer>> getNameTranslator(Collection<String> names) {
        if (names == null || names.isEmpty()) return Map.of();

        // mapiraj original -> lower (radi vraćanja originalnog ključa)
        Map<String, String> origByLower = new HashMap<>();
        for (String n : names) if (n != null) origByLower.put(n.toLowerCase(Locale.ROOT), n);

        Set<String> lowers = origByLower.keySet();

        Map<String, List<Integer>> out = new LinkedHashMap<>();
        out.putAll(fetchNameToIds("species", lowers, origByLower));
        // koji nedostaju → probaj u synonym
        Set<String> missing = new HashSet<>(lowers);
        missing.removeAll(out.keySet().stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet()));
        if (!missing.isEmpty()) {
            Map<String, List<Integer>> syn = fetchNameToIds("synonym", missing, origByLower);
            // spoji pod originalnim ključevima
            for (var e : syn.entrySet()) {
                out.merge(e.getKey(), e.getValue(), (a, b) -> {
                    var l = new ArrayList<>(a);
                    l.addAll(b);
                    return l;
                });
            }
        }
        return out;
    }

    private Map<String, List<Integer>> fetchNameToIds(String table, Set<String> lowers, Map<String, String> origByLower) {
        if (lowers.isEmpty()) return Map.of();
        String in = lowers.stream().map(x -> "?").collect(Collectors.joining(","));
        String sql = "SELECT spname, taxid FROM " + table + " WHERE spname IN (" + in + ") COLLATE NOCASE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (String s : lowers) ps.setString(i++, s);
            Map<String, List<Integer>> m = new LinkedHashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sp = rs.getString(1);
                    int tax = rs.getInt(2);
                    String orig = origByLower.get(sp.toLowerCase(Locale.ROOT));
                    m.computeIfAbsent(orig, k -> new ArrayList<>()).add(tax);
                }
            }
            return m;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Pretvori listu taxidova → listu imena (ako nema imena, vrati broj kao string).
     */
    public List<String> translateToNames(List<Integer> taxids) {
        Map<Integer, String> map = getTaxidTranslator(taxids, true);
        List<String> out = new ArrayList<>(taxids.size());
        for (Integer t : taxids) {
            out.add(map.getOrDefault(t, String.valueOf(t)));
        }
        return out;
    }

    /**
     * Svi potomci (descendants) nekog taxID-a. Koristi SQLite RECURSIVE CTE, bez .pkl cachea.
     */
    public List<Integer> getDescendantTaxa(int parentTaxid, boolean includeIntermediate) {
        int root = translateMerged(parentTaxid);
        String cte =
              "WITH RECURSIVE sub(taxid) AS (\n" +
              "  SELECT taxid FROM species WHERE taxid = ?\n" +
              "  UNION ALL\n" +
              "  SELECT s.taxid FROM species s JOIN sub ON s.parent = sub.taxid\n" +
              ")\n";
        String leavesOnly =
              "SELECT s.taxid FROM sub s LEFT JOIN species ch ON ch.parent = s.taxid " +
              "WHERE s.taxid <> ? AND ch.taxid IS NULL";
        String allNodes =
              "SELECT taxid FROM sub WHERE taxid <> ?";
        String sql = cte + (includeIntermediate ? allNodes : leavesOnly);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, root);
            ps.setInt(2, root);
            List<Integer> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getInt(1));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* ------------------------- utils ------------------------- */

    private static Set<Integer> cleanIds(Collection<Integer> ids) {
        Set<Integer> set = new LinkedHashSet<>();
        if (ids == null) return set;
        for (Integer i : ids) if (i != null) set.add(i);
        return set;
    }

    private static List<Integer> parseTrack(String track) {
        if (track == null || track.isBlank()) return List.of();
        String[] parts = track.split(",");
        List<Integer> r = new ArrayList<>(parts.length);
        for (String p : parts) {
            String s = p.trim();
            if (!s.isEmpty()) r.add(Integer.parseInt(s));
        }
        return r;
    }

    @Override
    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignored) {
        }
    }

    public static void main(String[] args) {
        try (NCBITaxaEte dao = new NCBITaxaEte()) {
            System.out.println("DB version: " + dao.dbVersion());
            System.out.println("Taxid 9606 rank: " + dao.getRank(Set.of(9606)));
            System.out.println("Taxid 9606 lineage: " + dao.getLineage(9606));
            System.out.println("Taxid 9606 name: " + dao.getTaxidTranslator(Set.of(9606), true));
            System.out.println("Descendants of 9606: " + dao.getDescendantTaxa(9606, false));
        }
    }
}
