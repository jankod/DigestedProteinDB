package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.CustomAccessionDb;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.rendering.FileRenderer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Data
public class MainWeb {

    //    private String rocksDbPath = "";
//    private String accDbPath = "";
    private String dbDir;
    private int port = 7070;

    public MainWeb(int port, String dbDir) {
        this.port = port;
        this.dbDir = dbDir;
    }


    public static void main(String[] args) throws RocksDBException {
        MainWeb app = new MainWeb(7070, "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot");
//        app.rocksDbPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/rocksdb_mass.db";
//        app.accDbPath = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/custom_accession.db";
        app.startWeb();
    }


    public void startWeb() throws RocksDBException {
        MainMassRocksDb db = new MainMassRocksDb();

        db.setToDbPath(dbDir + "/rocksdb_mass.db");

        RocksDB massRocksDb = db.openReadDB();
        CustomAccessionDb accDb = new CustomAccessionDb();
        accDb.setToDbPath(dbDir + "/custom_accession.db");


//        accDb.setToDbPath(accDbPath);
        accDb.loadDb();

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
            Properties prop = new Properties();
            FileReader reader = new FileReader(dbDir + "/db-info_bacteria_trembl.properties");
            prop.load(reader);
            reader.close();
            ctx.json(prop);
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

            // log.debug("Search mass1: {} mass2: {}", mass1, mass2);
            List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> entries = db.searchByMass(massRocksDb, mass1, mass2);
//            for (Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>> entry : entries) {
//                System.out.println(entry.getKey());
//                for (BinaryPeptideDbUtil.PeptideAcc peptideAcc : entry.getValue()) {
//                  //  System.out.println(peptideAcc.getSeq());
//                }
//            }
            ctx.json(new MassResult(entries, accDb));


            //ctx.json("Evo rezultat "+ mass1 + " "+ mass2);
        });

        app.events(eventConfig -> {
            eventConfig.serverStopping(() -> {
                massRocksDb.close();
            });
        });
        log.info("Web started on {}", "http://localhost:" + port);
    }
}

@Data
class MassResult {
    int totalResult = 0;

    List<SeqAcc> results;

    public MassResult(List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> entries, CustomAccessionDb accDb) {
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
                    String accStr = accDb.getAcc(accNum);
                    sa.acc.add(accStr);
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
