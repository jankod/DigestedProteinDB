package hr.pbf.digestdb.db;

import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MassRocksDbTest {


    @Test
    public void testCsvCreateDelete() throws IOException, RocksDBException {
        String csv = """
              503.234,SGAGAAA:2-SAAGGAA:1
              505.2132,AGGASSG:3
              513.2547,AGAAPAG:4
              516.2405,GGGGGGR:8;7;6;5
              516.2656,AAGGGGK:11;10;9
              517.2496,GGSALGG:13-AAAGASA:12
              529.2496,SGAAGPA:14
              529.286,LAGAAAG:18-AAAGAAV:15-ALGGAAA:16-GLLGGGG:17
              530.2449,AGQGAAG:19
              530.2561,GGGGGAR:23-AGGGGGR:21;20-GGAGGGR:22
              530.2813,GGAAAGK:25-GAAGGAK:24
              531.2289,GSGGGTP:26
              531.2653,AAAASAA:27
              541.286,AGGAVPA:28
              542.2813,AGGGGPK:29
              543.2653,TAAGGAP:30
              543.301649,AAIAAGA:32;31
              544.2717,GAGAGGR:34-AAGGGGR:33-RAGGGAG:35
              544.2718,GGAGAGR:39;38;37;36-GGAGGAR:40-GGGGAAR:41""";
        File csvFile = null;
        String dbDirPath = "";
        try {


            csvFile = File.createTempFile("mass_db_test", ".csv");
            csvFile.delete();
            dbDirPath = csvFile.getAbsolutePath() + ".db";
            FileUtils.writeStringToFile(csvFile, csv, StandardCharsets.UTF_8);

            MassRocksDbCreator creator = new MassRocksDbCreator(csvFile.getAbsolutePath(), dbDirPath);
            MassRocksDbCreator.DbInfo dbInfo = creator.startCreate();

            assertEquals(19, dbInfo.countMasses);
            assertTrue(new File(dbDirPath).exists());

            log.debug("db created: " + dbDirPath);


            try (MassRocksDbReader reader = new MassRocksDbReader(dbDirPath)) {

                {
                    List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> emptyResult = reader.searchByMassPaginated(400, 503);
                    assertEquals(0, emptyResult.size());
                }
                {
                    List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> resultLeft8 = reader.searchByMassPaginated(503, 530);
                    assertEquals(8, resultLeft8.size());
                }
                {
                    List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> result3 = reader.searchByMassPaginated(542, 544);
                    assertEquals(3, result3.size());
                    Double key542_2813 = result3.getFirst().getKey(); // 542.2813,AGGGGPK:29
                    Set<BinaryPeptideDbUtil.PeptideAccids> valuePeptide = result3.getFirst().getValue();
                    assertEquals(1, valuePeptide.size());
                    assertEquals("AGGGGPK", valuePeptide.iterator().next().getSeq());
                    assertArrayEquals(new int[]{29}, valuePeptide.iterator().next().getAccids());
                    assertEquals(542.2813, key542_2813, 0.0001);
                }
                {
                    List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> resultRight9 = reader.searchByMassPaginated(530, 544);
                    assertEquals(9, resultRight9.size());
                }
                {
                    List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> result2 = reader.searchByMassPaginated(544, 545);
                    assertEquals(2, result2.size());
                }
                {
                    List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> result2 = reader.searchByMassPaginated(544, 545.2657);
                    assertEquals(2, result2.size());


                    // 544.2717,GAGAGGR:34-AAGGGGR:33-RAGGGAG:35
                    assertEquals(544.2717, result2.getFirst().getKey(), 0.0001);
                    Set<BinaryPeptideDbUtil.PeptideAccids> pepAccFirstSet = result2.getFirst().getValue();
                    assertEquals(3, pepAccFirstSet.size());


                    pepAccFirstSet.forEach(pepAcc -> {
                        log.debug("seq: " + pepAcc.getSeq() + " acc: " + Arrays.toString(pepAcc.getAccids()));

                        switch (pepAcc.getSeq()) {
                            case "GAGAGGR" -> assertArrayEquals(new int[]{34}, pepAcc.getAccids());
                            case "AAGGGGR" -> assertArrayEquals(new int[]{33}, pepAcc.getAccids());
                            case "RAGGGAG" -> assertArrayEquals(new int[]{35}, pepAcc.getAccids());
                            default -> fail("Unexpected peptide sequence: " + pepAcc.getSeq());
                        }
                    });

                    Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>> pepAccSecondSet = result2.get(1);
                    // 544.2718,GGAGAGR:39;38;37;36-GGAGGAR:40-GGGGAAR:41
                    assertEquals(544.2718, pepAccSecondSet.getKey(), 0.0001);
                    assertEquals(3, pepAccSecondSet.getValue().size());
                    pepAccSecondSet.getValue().forEach(pepAcc -> {

                        switch (pepAcc.getSeq()) {
                            case "GGAGAGR" -> assertArrayEquals(new int[]{39, 38, 37, 36}, pepAcc.getAccids());
                            case "GGAGGAR" -> assertArrayEquals(new int[]{40}, pepAcc.getAccids());
                            case "GGGGAAR" -> assertArrayEquals(new int[]{41}, pepAcc.getAccids());
                            default -> fail("Unexpected peptide sequence: " + pepAcc.getSeq());
                        }
                    });

                    { // non existing rigth
                        List<Map.Entry<Double, Set<BinaryPeptideDbUtil.PeptideAccids>>> result2NonExisting = reader.searchByMassPaginated(544.3, 545);
                        assertEquals(0, result2NonExisting.size());

                    }
                }
            }


        } finally {
            FileUtils.deleteQuietly(csvFile);
            FileUtils.deleteQuietly(new File(dbDirPath));

        }

    }

}
