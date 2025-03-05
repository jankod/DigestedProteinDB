package hr.pbf.digestdb;

import hr.pbf.digestdb.db.AccessionDbReader;
import hr.pbf.digestdb.db.MassRocksDbReader;
import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.WorkflowConfig;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.rendering.FileRenderer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Data
public class AppWeb {

    private String dbDir;
    private int port;

    private String dbPath;
    private String accDbPath;

    public static class WebArgsParams {
        @CommandLine.Option(names = {"-p", "--port"}, description = "Port", required = true, defaultValue = "7070")
        int port;

        @CommandLine.Option(names = {"-d", "--db-dir"}, description = "Path to the directory with workflow.properties file", required = true)
        String dbDir;
    }

    public AppWeb(WebArgsParams params) {
        this.port = params.port;
        this.dbDir = params.dbDir;
        setDbPath(getDbDir() + "/" + AppWorkflow.DEFAULT_ROCKSDB_MASS_DB_FILE_NAME);
        setAccDbPath(getDbDir() + "/" + AppWorkflow.DEFAULT_DB_FILE_NAME);
    }


    public static void main(String[] args) throws RocksDBException {

        WebArgsParams params = new WebArgsParams();
        new CommandLine(params).parseArgs(args);

        AppWeb app = new AppWeb(params);

        log.debug("current dir: " + new File(".").getAbsoluteFile());
        app.startWeb();
    }


    public void startWeb() throws RocksDBException {
        MassRocksDbReader massDb = new MassRocksDbReader(dbPath);
        AccessionDbReader accDb = new AccessionDbReader(accDbPath);

        var app = Javalin.create(config -> {
                  config.fileRenderer(new FileRenderer() {
                      @NotNull
                      @Override
                      public String render(@NotNull String filePath, @NotNull Map<String, ?> map, @NotNull Context context) {
                          try (InputStream is = getClass().getResourceAsStream("/web/" + filePath)) {
                              if (is == null) {
                                  throw new IllegalArgumentException("File not found: /web/" + filePath);
                              }
                              return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                          } catch (IOException e) {
                              throw new RuntimeException(e);
                          }
                      }
                  });
              })
              .get("/", ctx -> {
                  ctx.render("index.html");
              })
              .start(port);

        app.get("/db-info", ctx -> {
            WorkflowConfig config = new WorkflowConfig(dbDir);
            ctx.json(config.getDbInfo());

        });
        app.get("/search", ctx -> {
            double mass1 = 0;
            double mass2 = 0;
            try {
                mass1 = ctx.queryParamAsClass("mass1", Double.class)
                      .getOrThrow(stringMap -> new Exception("Mass1 is required as double."));
                mass2 = ctx.queryParamAsClass("mass2", Double.class)
                      .getOrThrow(stringMap -> new Exception("Mass2 is required as double."));

                if (Math.abs(mass1 - mass2) > 1) {
                    ctx.status(400);
                    ctx.json("Mass1 and Mass2 must be close 1 Da");
                    return;
                }
            } catch (Exception e) {
                ctx.status(400);
                ctx.json("Mass1 and Mass2 must be numbers. " + e.getMessage());
                return;
            }

            //  List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> entries = db.searchByMass(massRocksDb, mass1, mass2);
            List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> result = massDb.searchByMass(mass1, mass2);
            ctx.json(new MassResult(result, accDb));

        });

        app.events(eventConfig -> {
            eventConfig.serverStopping(() -> {
                massDb.close();
                // massRocksDb.close();
            });
        });
        log.info("Web started on {}", "http://localhost:" + port);
    }
}


@Data
class MassResult {
    int totalResult = 0;

    List<SeqAcc> results;

    public MassResult(List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> entries, AccessionDbReader accDb) {
        results = new ArrayList<>(entries.size());
        totalResult = entries.size();

        entries.forEach(e -> {
            Set<BinaryPeptideDbUtil.PeptideAcc> peptides = e.getValue();
            for (BinaryPeptideDbUtil.PeptideAcc peptide : peptides) {
                SeqAcc sa = new SeqAcc();
                sa.mass = e.getKey();
                int[] accessionsNum = peptide.getAccessions();
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
