
package hr.pbf.digestdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.pbf.digestdb.db.AccessionDbReader;
import hr.pbf.digestdb.db.MassRocksDbReader;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MyStopWatch;
import hr.pbf.digestdb.util.WorkflowConfig;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;
import picocli.CommandLine;

import java.io.IOException;
import java.util.*;

@Slf4j
@Data
public class SearchWebApp {

    private String dbDir;
    private int port;

    private String dbPath;
    private String accDbPath;
    private Undertow server;
    private MassRocksDbReader massDb;
    private AccessionDbReader accDb;

    private WorkflowConfig config;

    public static class WebArgsParams {
        @CommandLine.Option(names = {"-p", "--port"}, description = "Port", required = true, defaultValue = "7070")
        int port;

        @CommandLine.Option(names = {"-d", "--db-dir"}, description = "Path to the directory with workflow.properties file", required = true)
        String dbDir;
    }

    public SearchWebApp(WebArgsParams params) throws IOException {
        this.port = params.port;
        this.dbDir = params.dbDir;
        setDbPath(getDbDir() + "/" + CreateDatabaseApp.DEFAULT_ROCKSDB_MASS_DB_FILE_NAME);
        setAccDbPath(getDbDir() + "/" + CreateDatabaseApp.DEFAULT_DB_FILE_NAME);
        config = new WorkflowConfig(dbDir);
    }

    public static void main(String[] args) throws RocksDBException, IOException {
        WebArgsParams params = new WebArgsParams();
        new CommandLine(params).parseArgs(args);

        SearchWebApp app = new SearchWebApp(params);
        log.debug("Start web on port: " + app.getPort() + " db dir: " + app.getDbDir());
        app.startWeb();
    }

    public void startWeb() throws RocksDBException {
        try {
            long startTime = System.currentTimeMillis();

            log.info("Initializing database...");

            massDb = new MassRocksDbReader(dbPath);
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
                  .addExactPath("/search-peptide", this::handleBySearch);


            // Build and start server
            server = Undertow.builder()
                  .addHttpListener(port, "0.0.0.0")
                  .setHandler(pathHandler)
                  .build();

            server.start();
            log.info("Web started on {}", "http://localhost:" + port);

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        } catch (Exception e) {
            log.error("Error starting web server", e);
            shutdown();
        }
    }

    private void handleBySearch(HttpServerExchange http) {
        if (http.isInIoThread()) {
            http.dispatch(this::handleBySearch);
            return;
        }

        Map<String, String> params = createParam(http);

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

            sendJsonResponse(exchange, StatusCodes.OK, toJson(config.getDbInfo()));
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

//            if (Math.abs(mass1 - mass2) > 100) {
//                sendJsonResponse(exchange, StatusCodes.BAD_REQUEST,
//                      "{\"error\": \"Mass1 and Mass2 must be close 100 Da\"}");
//                return;
//            }
            searchByMass(exchange, mass1, mass2, page, pageSize);

        } catch (NumberFormatException e) {
            sendJsonResponse(exchange, StatusCodes.BAD_REQUEST,
                  "{\"error\": \"Mass1 and Mass2 must be numbers.\"}");
        } catch (Exception e) {
            sendJsonResponse(exchange, StatusCodes.INTERNAL_SERVER_ERROR,
                  "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void searchByMass(HttpServerExchange exchange, double mass1, double mass2, int page, int pageSize) {
        MyStopWatch watch = new MyStopWatch();
        MassRocksDbReader.MassPageResult result = massDb.searchByMassPaginated(mass1, mass2, page, pageSize);
        long l = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        String memory = l / 1024 / 1024 + " MB";
        PageResult pageResult = new PageResult(result.getTotalCount(), result.getResults(), memory, watch.getCurrentDuration(), page, pageSize);
        String jsonResult = toJson(pageResult);
        sendJsonResponse(exchange, StatusCodes.OK, jsonResult);
    }

    @Data
    static
    class PageResult {
        private final int totalResult;
        private final String memory;
        private final String duration;
        private final int page;
        private final int pageSize;
        private final List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> result;

        public PageResult(int totalResult, List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> result, String memory, String currentDuration, int page, int pageSize) {
            this.totalResult = totalResult;
            this.result = result;
            this.memory = memory;
            this.duration = currentDuration;
            this.page = page;
            this.pageSize = pageSize;
        }
    }

    private void sendJsonResponse(HttpServerExchange exchange, int statusCode, String response) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.setStatusCode(statusCode);
        exchange.getResponseSender().send(response);
    }

    ObjectMapper objectMapper = new ObjectMapper();

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


@Data
class MassResult {
    int totalResult = 0;
    String duration = "";

    List<SeqAcc> results;

    public MassResult(List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> entries, AccessionDbReader accDb, String currentDuration) {
        results = new ArrayList<>(entries.size());
        totalResult = entries.size();
        duration = currentDuration;

        entries.forEach(e -> {
            Set<BinaryPeptideDbUtil.PeptideAcc> peptides = e.getValue();
            for (BinaryPeptideDbUtil.PeptideAcc peptide : peptides) {
                SeqAcc sa = new SeqAcc();
                sa.mass = e.getKey();
                int[] accessionsNum = peptide.getAcc();
                sa.acc = new ArrayList<>(accessionsNum.length);
                sa.seq = peptide.getSeq();
                for (int accNum : accessionsNum) {
                    String accession = accDb.getAccession(accNum);
                    sa.acc.add(accession);
                }
                results.add(sa);
            }

        });
    }

    @Data
    static class SeqAcc {
        double mass;
        String seq;
        List<String> acc;
    }
}
