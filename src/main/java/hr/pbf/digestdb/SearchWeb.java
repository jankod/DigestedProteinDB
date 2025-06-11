
package hr.pbf.digestdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.pbf.digestdb.db.AccessionDbReader;
import hr.pbf.digestdb.db.MassRocksDbReader;
import hr.pbf.digestdb.util.AccTaxDB;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MyStopWatch;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;
import org.wildfly.common.annotation.NotNull;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Slf4j
@Data
public class SearchWeb {
    private String dbDirPath;
    private String dbAccTaxPath;
    private final int port;

    private String accDbPath;
    private Undertow server;
    private MassRocksDbReader massDb;
    private AccessionDbReader accDb;

    private AccTaxDB accTaxDb;

    public SearchWeb(String dbDirPath, int port) {
        this.dbDirPath = dbDirPath;
        this.port = port;
        setDbDirPath(dbDirPath + "/" + CreateDatabase.DEFAULT_ROCKSDB_MASS_DB_FILE_NAME);
        setAccDbPath(dbDirPath + "/" + CreateDatabase.DEFAULT_DB_FILE_NAME);
        setDbAccTaxPath(dbDirPath + "/" + CreateDatabase.DEFAULT_DB_ACC_TAX);
    }

    public void start() throws RocksDBException, IOException {

        log.debug("Start web on port: {} db dir: {}", port, dbDirPath);
        try {
            long startTime = System.currentTimeMillis();

            log.info("Initializing database...");

            massDb = new MassRocksDbReader(dbDirPath);
            accDb = new AccessionDbReader(accDbPath);
            log.info("Database initialized in {} ms", System.currentTimeMillis() - startTime);


            // Resource handler for static files
            ResourceHandler homeHtmlPageHandler = new ResourceHandler(
                    new ClassPathResourceManager(getClass().getClassLoader(), "web"))
                    .setWelcomeFiles("index.html");

            // Create paths
            PathHandler pathHandler = new PathHandler()
                    .addPrefixPath("/", homeHtmlPageHandler)
                    .addExactPath("/db-info", this::handleDbInfo)
                    .addExactPath("/search", this::handleSearch)
                    .addExactPath("/search-peptide", this::handleSearchByPeptide)
                    .addExactPath("/search-taxonomy", this::handleSearchTaxonomy);


            server = Undertow.builder()
                    .addHttpListener(port, "0.0.0.0")
                    .setHandler(pathHandler)
                    .build();

            server.start();
            log.info("Web started on {}", "http://localhost:" + port);

            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            // Keep the main thread alive until shutdown
            Thread.currentThread().join();

        } catch (Exception e) {
            log.error("Error starting web server", e);
            shutdown();
        }
    }

    private void handleSearchTaxonomy(HttpServerExchange http) {
        if (http.isInIoThread()) {
            http.dispatch(this::handleSearchTaxonomy);
            return;
        }

        Map<String, String> params = createParam(http);
        log.debug("search by taxonomy: {}", params);

        try {

            double mass1 = Double.parseDouble(params.getOrDefault("mass1", "0"));
            double mass2 = Double.parseDouble(params.getOrDefault("mass2", "0"));

            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "1000"));

            if (mass1 == 0 || mass2 == 0) {
                sendJsonResponse(http, StatusCodes.BAD_REQUEST,
                        "{\"error\": \"Mass1 and Mass2 are required as doubles.\"}");
                return;
            }


            MyStopWatch watch = new MyStopWatch();
            MassRocksDbReader.MassPageResult result = massDb.searchByMassPaginated(mass1, mass2, page, pageSize);
            long l = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            String memory = l / 1024 / 1024 + " MB";
            PageResult pageResult = new PageResult(result.getTotalCount(), toAccession(result.getResults()), memory, watch.getCurrentDuration(), page, pageSize);


            PageResultTax pageResultTax = toTaxonomy(pageResult);
            String jsonResult = toJson(pageResultTax);

            sendJsonResponse(http, StatusCodes.OK, jsonResult);


        } catch (Exception e) {
            log.error("Error searching taxonomy", e);
            sendJsonResponse(http, StatusCodes.INTERNAL_SERVER_ERROR,
                    "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private PageResultTax toTaxonomy(PageResult pageResult) {
        if (accTaxDb == null) {
            accTaxDb = new AccTaxDB();
            accTaxDb.readFromDisk(dbAccTaxPath);
            log.debug("Taxonomy read from disk: {}", accTaxDb);
        }

        PageResultTax pageResultTax = new PageResultTax();
        pageResultTax.setTotalResult(pageResult.getTotalResult());
        pageResultTax.setMemory(pageResult.getMemory());
        pageResultTax.setDuration(pageResult.getDuration());
        pageResultTax.setPage(pageResult.getPage());
        pageResultTax.setPageSize(pageResult.getPageSize());
        List<Map.Entry<Double, Set<PeptideAccTax>>> result = new ArrayList<>(pageResult.getResult().size());
        for (Map.Entry<Double, Set<PeptideAccText>> e : pageResult.getResult()) {
            Set<PeptideAccTax> peptides = new HashSet<>();
            for (PeptideAccText acc : e.getValue()) {
                PeptideAccTax accTax = new PeptideAccTax();
                accTax.setSeq(acc.getSeq());
                List<AccTaxs> accsTaxs = new ArrayList<>(acc.getAcc().length);
                for (String accText : acc.getAcc()) {
                    AccTaxs accTaxs = new AccTaxs();
                    accTaxs.setAcc(accText);
                    accTaxs.setTaxIds(accTaxDb.getTaxonomyIds(accText));
                    accsTaxs.add(accTaxs);
                }
                accTax.setAccsTax(accsTaxs);
                peptides.add(accTax);
            }
            result.add(new AbstractMap.SimpleEntry<>(e.getKey(), peptides));
        }
        pageResultTax.setResult(result);
        return pageResultTax;

    }

    private void handleSearchByPeptide(HttpServerExchange http) {
        if (http.isInIoThread()) {
            http.dispatch(this::handleSearchByPeptide);
            return;
        }


        Map<String, String> params = createParam(http);

        log.debug("search by peptide: {}", params);
        try {
            String peptide = params.getOrDefault("peptide", "");
            if (peptide.isEmpty()) {
                sendJsonResponse(http, StatusCodes.BAD_REQUEST,
                        "{\"error\": \"Peptide is required\"}");
                return;
            }
            double mass1 = BioUtil.calculateMassWidthH2O(peptide);
            double mass2 = mass1;
            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "1000"));

            searchByMass(http, mass1, mass2, page, pageSize);
        } catch (Exception e) {
            sendJsonResponse(http, StatusCodes.INTERNAL_SERVER_ERROR,
                    "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private static Map<String, String> createParam(HttpServerExchange http) {
        Map<String, String> params = new HashMap<>();
        http.getQueryParameters().forEach((key, value) -> {
            if (!value.isEmpty()) {
                params.put(key, value.getFirst());
            }
        });
        return params;
    }

    private void handleDbInfo(HttpServerExchange exchange) throws IOException {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this::handleDbInfo);
            return;
        }

        try {
            sendJsonResponse(exchange, StatusCodes.OK, toJson(getDbInfo()));
        } catch (Exception e) {
            sendJsonResponse(exchange, StatusCodes.INTERNAL_SERVER_ERROR,
                    "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleSearch(HttpServerExchange exchange) throws IOException {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this::handleSearch);
            return;
        }

        Map<String, String> params = createParam(exchange);

        try {
            double mass1 = Double.parseDouble(params.getOrDefault("mass1", "0"));
            double mass2 = Double.parseDouble(params.getOrDefault("mass2", "0"));

            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "1000"));

            if (mass1 == 0 || mass2 == 0) {
                sendJsonResponse(exchange, StatusCodes.BAD_REQUEST,
                        "{\"error\": \"Mass1 and Mass2 are required as doubles.\"}");
                return;
            }

            searchByMass(exchange, mass1, mass2, page, pageSize);

        } catch (NumberFormatException e) {
            sendJsonResponse(exchange, StatusCodes.BAD_REQUEST,
                    "{\"error\": \"Mass1 and Mass2 must be numbers.\"}");
        } catch (Exception e) {
            log.error("Error on search", e);
            sendJsonResponse(exchange, StatusCodes.INTERNAL_SERVER_ERROR,
                    "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    synchronized private void searchByMass(HttpServerExchange exchange, double mass1, double mass2, int page, int pageSize) {
        MyStopWatch watch = new MyStopWatch();
        MassRocksDbReader.MassPageResult result = massDb.searchByMassPaginated(mass1, mass2, page, pageSize);
        long l = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        String memory = l / 1024 / 1024 + " MB";
        PageResult pageResult = new PageResult(result.getTotalCount(), toAccession(result.getResults()), memory, watch.getCurrentDuration(), page, pageSize);
        String jsonResult = toJson(pageResult);
        sendJsonResponse(exchange, StatusCodes.OK, jsonResult);
    }

    private List<Map.Entry<Double, Set<PeptideAccText>>> toAccession(List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> results) {
        // TODO: use mapstruct
        List<Map.Entry<Double, Set<PeptideAccText>>> result = new ArrayList<>(results.size());
        for (Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>> e : results) {
            Set<PeptideAccText> peptides = new HashSet<>();
            for (BinaryPeptideDbUtil.PeptideAcc acc : e.getValue()) {
                PeptideAccText accText = new PeptideAccText();
                accText.setSeq(acc.getSeq());
                String[] accs = new String[acc.getAcc().length];
                for (int i = 0; i < acc.getAcc().length; i++) {
                    accs[i] = getAccDb().getAccession(acc.getAcc()[i]);
                }
                accText.setAcc(accs);
                peptides.add(accText);
            }
            result.add(new AbstractMap.SimpleEntry<>(e.getKey(), peptides));
        }
        return result;
    }

    public Properties getDbInfo() {
        Properties prop = new Properties();
        try (FileReader reader = new FileReader(dbDirPath + "/../db_info.properties")) {
            prop.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return prop;
    }

    @Data
    @RequiredArgsConstructor
    public static class PeptideAccTax {
        private String seq;
        private List<AccTaxs> accsTax;

    }

    @Data
    static class AccTaxs {
        String acc;
        List<Integer> taxIds = new ArrayList<>();
    }

    @Data
    public static class PeptideAccText implements Comparable<BinaryPeptideDbUtil.PeptideAcc> {
        private String seq;
        private String[] acc;

        @Override
        public int compareTo(@NotNull BinaryPeptideDbUtil.PeptideAcc o) {
            if (seq.equals(o.getSeq())) {
                return 0;
            }
            return seq.compareTo(o.getSeq());
        }

        @Override
        public String toString() {
            return seq + " " + Arrays.toString(acc);
        }


        public int hashCode() {
            return seq.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            return Objects.equals(seq, ((PeptideAccText) obj).seq);
        }
    }

    @Data
    static
    class PageResult {
        private final int totalResult;
        private final String memory;
        private final String duration;
        private final int page;
        private final int pageSize;
        private final List<Map.Entry<Double, Set<PeptideAccText>>> result;

        public PageResult(int totalResult, List<Map.Entry<Double, Set<PeptideAccText>>> result, String memory, String currentDuration, int page, int pageSize) {
            this.totalResult = totalResult;
            this.result = result;
            this.memory = memory;
            this.duration = currentDuration;
            this.page = page;
            this.pageSize = pageSize;
        }
    }

//    @Data
//    @RequiredArgsConstructor
//    class PageResult {
//        private final int totalResult;
//        private final String memory;
//        private final String duration;
//        private final int page;
//        private final int pageSize;
//        private final List<Map.Entry<Double, Set<PeptideAcc>>> result;
//    }

    @Data
    static
    class PageResultTax {
        private int totalResult;
        private String memory;
        private String duration;
        private int page;
        private int pageSize;
        private List<Map.Entry<Double, Set<PeptideAccTax>>> result;

    }

    private void sendJsonResponse(HttpServerExchange exchange, int statusCode, String response) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.setStatusCode(statusCode);
        exchange.getResponseSender().send(response);
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to json", e);
            return "{}";
        }
    }

    public void shutdown() {
        if (server != null) {
            server.stop();
        }
        if (massDb != null) {
            massDb.close();
        }
        log.info("Server stopped and resources released");
    }

}

