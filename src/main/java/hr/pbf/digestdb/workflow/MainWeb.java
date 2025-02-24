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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class MainWeb {
    public static void main(String[] args) throws RocksDBException {
        MainMassRocksDb db = new MainMassRocksDb();
        db.setToDbPath("/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/rocksdb_mass.db");
        RocksDB massRocksDb = db.openReadDB();
        CustomAccessionDb accDb = new CustomAccessionDb();
        accDb.setToDbPath("/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated_bacteria_uniprot/custom_accession.db");
        accDb.loadDb();

        var app = Javalin.create(config -> {
                  config.fileRenderer(new FileRenderer() {
                      @NotNull
                      @Override
                      public String render(@NotNull String filePath, @NotNull Map<String, ?> map, @NotNull Context context) {
                          log.debug("resnder " + filePath);
                          // return html page from src/main/resource/web
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
              .start(7070);

        app.get("/search", ctx -> {
            double mass1 = Double.parseDouble(ctx.queryParam("mass1"));
            double mass2 = Double.parseDouble(ctx.queryParam("mass2"));

            if (Math.abs(mass1 - mass2) > 0.3) {
                ctx.status(400);
                ctx.json("Mass1 and Mass2 must be close 0.3 Da");
                return;
            }

            log.debug("Search mass1: {} mass2: {}", mass1, mass2);
            List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> entries = db.searchByMass(massRocksDb, mass1, mass2);
            for (Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>> entry : entries) {
                System.out.println(entry.getKey());
                for (BinaryPeptideDbUtil.PeptideAcc peptideAcc : entry.getValue()) {
                    System.out.println(peptideAcc.getSeq());
                }
            }
            ctx.json(new MassResult(entries, accDb));


            //ctx.json("Evo rezultat "+ mass1 + " "+ mass2);
        });

        app.events(eventConfig -> {
            eventConfig.serverStopping(() -> {
                massRocksDb.close();
            });
        });
    }
}

@Data
class MassResult {
    int totalResult = 0;

    List<SeqAcc> seqAccs;

    public MassResult(List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAcc>>> entries, CustomAccessionDb accDb) {
        seqAccs = new ArrayList<>(entries.size());
        totalResult = entries.size();

        entries.forEach(e -> {
            Set<BinaryPeptideDbUtil.PeptideAcc> peptides = e.getValue();
            for (BinaryPeptideDbUtil.PeptideAcc peptide : peptides) {
                SeqAcc sa = new SeqAcc();
                sa.mass = e.getKey();
                int[] accessionsNum = peptide.getAccessions();
                sa.accessions = new ArrayList<>(accessionsNum.length);
                sa.seq = peptide.getSeq();
                for (int accNum : accessionsNum) {
                    String accStr = accDb.getAcc(accNum);
                    sa.accessions.add(accStr);
                }
                seqAccs.add(sa);
                sa = new SeqAcc();
            }

        });
    }

    @Data
    static class SeqAcc {
        double mass;
        String seq;
        List<String> accessions;
    }
}
