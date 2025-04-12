package hr.pbf.digestdb.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BioUtilTest {

    @Test
    void chymotrypsin11Mc() {
        String protein = "MTKHYRMIPRVRPEAGYREIFSALGTGHDPKFIAAFAEVLRSSLSLESAPILTPSGRAALYYLFLAVPLRRVYLPAYTCWVVAEAAELAGKKIHLLDIAYPGLHIKPSEIERIRFKPGIVVATHQFGYPEDAAAIRNILDETSFMIVEDCAGAMFSRYQNKPVGLTGDAAIFSFEEGKLWTLGRGGALAVKDKALRFQIQNIMKRTCTPGTSRINLFRLIKRRFLTHLFVYNLLLRMYLRFYPPTEGIHEFSRELTEDYTRTFSSYQARLGLIMEPRIKEIARRKRELFEYYYRATADIAGIRRVEALPQSEVCPMRFPILVPPENKLAIYSRMKSLGIDLGFSFSYFLGNEKQCPGAARMAKECLNLPVYSEINLPIARNIVNLMGSCMSDMS";
        int min = 7;
        int max = 30;
        List<String> result = BioUtil.chymotrypsin1mc(protein, min, max);
        System.out.println(result);

        // provjeri jel sadrzi u listi REIFSALGTGHDPKF
        assertTrue(result.contains("REIFSALGTGHDPKF"));
        assertTrue(result.contains("QNKPVGLTGDAAIFSF"));
        assertTrue(result.contains("QIQNIMKRTCTPGTSRINLFRLIKRRF"));
        assertTrue(result.contains("SRELTEDY"));
        assertTrue(result.contains("SFEEGKLW"));

        assertFalse(result.contains("TCWVVAEAAELAGKKIHLLDIAYPGLHIKPSEIERIRF"));
    }

    @Test
    void tripsyn1mc() {
    }
}