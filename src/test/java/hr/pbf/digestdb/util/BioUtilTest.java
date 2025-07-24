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
        // Test basic functionality
        String protein = "MKRKVYK";
        List<String> result = BioUtil.tripsyn1mc(protein, 1, 20);

        // Expected peptides with 0 and 1 missed cleavage:
        // 0 MC: MK, R, KVYK
        // 1 MC: MKR, RKVYK
        assertTrue(result.contains("MK"));
        assertTrue(result.contains("R"));
        assertTrue(result.contains("KVYK"));
        assertTrue(result.contains("MKR"));
    }

    @Test
    void tripsyn1mcWithProline() {
        // Test that cleavage is blocked by proline
        String protein = "MKRPKVYK";
        List<String> result = BioUtil.tripsyn1mc(protein, 1, 20);



        assertTrue(result.contains("RPKVYK"));
        // R is followed by P, so no cleavage there
        // Only cleavage after K
        assertTrue(result.contains("MKRPK"));
        assertTrue(result.contains("VYK"));
        assertTrue(result.contains("RPK"));
        assertTrue(result.contains("MK"));
        assertFalse(result.contains("MKR")); // Should not cleave before P

    }

    @Test
    void tripsyn1mcNoCleavageSites() {
        // Test protein with no R or K
        String protein = "MLACDEFG";
        List<String> result = BioUtil.tripsyn1mc(protein, 1, 20);

        assertEquals(1, result.size());
        assertTrue(result.contains("MLACDEFG"));
    }

    @Test
    void tripsyn1mcLengthFiltering() {
        String protein = "MKRKVYK";

        // Test min length filtering
        List<String> result = BioUtil.tripsyn1mc(protein, 3, 20);
        assertFalse(result.contains("MK")); // Too short
        assertFalse(result.contains("R"));  // Too short
        assertTrue(result.contains("KVYK"));
        assertTrue(result.contains("MKR"));

        // Test max length filtering
        result = BioUtil.tripsyn1mc(protein, 1, 3);
        assertTrue(result.contains("MK"));
        assertTrue(result.contains("R"));
        assertFalse(result.contains("KVYK")); // Too long
        assertFalse(result.contains("RKVYK")); // Too long
    }

    @Test
    void tripsyn1mcSingleAminoAcid() {
        String protein = "K";
        List<String> result = BioUtil.tripsyn1mc(protein, 1, 10);
        assertTrue(result.contains("K"));
        assertEquals(1, result.size());
    }

    @Test
    void tripsyn1mcEmptyProtein() {
        String protein = "";
        List<String> result = BioUtil.tripsyn1mc(protein, 1, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void tripsyn1mcEndingWithCleavage() {
        // Test protein ending with cleavage site
        String protein = "MLACK";
        List<String> result = BioUtil.tripsyn1mc(protein, 1, 10);

        assertTrue(result.contains("MLACK"));
        assertEquals(1, result.size());
    }

    @Test
    void tripsyn1mcMultipleCleavages() {
        // Test with multiple cleavage sites
        String protein = "MKRACDEFKGHIJK";
        List<String> result = BioUtil.tripsyn1mc(protein, 1, 20);

        // 0 MC: MK, R, ACDEFK, GHIJK
        // 1 MC: MKR, RACDEFK, ACDEFKGHIJK
        assertTrue(result.contains("MK"));
        assertTrue(result.contains("R"));
        assertTrue(result.contains("ACDEFK"));
        assertTrue(result.contains("GHIJK"));
        assertTrue(result.contains("MKR"));
        assertTrue(result.contains("RACDEFK"));
        assertTrue(result.contains("ACDEFKGHIJK"));
    }

    @Test
    void tripsyn1mcConsecutiveCleavages() {
        // Test with consecutive R and K
        String protein = "MLARKDEFG";
        List<String> result = BioUtil.tripsyn1mc(protein, 1, 20);

        // 0 MC: MLAR, K, DEFG
        // 1 MC: MLARK, KDEFG
        assertTrue(result.contains("MLAR"));
        assertTrue(result.contains("K"));
        assertTrue(result.contains("DEFG"));
        assertTrue(result.contains("MLARK"));
        assertTrue(result.contains("KDEFG"));
    }


    @Test
    void tripsyn2mc() {
        // Test basic functionality
        String protein = "MKRKVYK";
        List<String> result = BioUtil.tripsyn2mc(protein, 1, 20);

        // Expected peptides with 0, 1, 2 missed cleavages:
        // 0 MC: MK, R, KVYK
        // 1 MC: MKR, RKVYK
        // 2 MC: MKRK, RKVYK (RKVYK already included)

        assertTrue(result.contains("MK"));
        assertTrue(result.contains("R"));
        assertTrue(result.contains("KVYK"));
        assertTrue(result.contains("MKR"));
        assertTrue(result.contains("RKVYK"));
        assertTrue(result.contains("MKRK"));
    }

    @Test
    void tripsyn2mcWithProline() {
        // Test that cleavage is blocked by proline
        String protein = "MKRPKVYK";
        List<String> result = BioUtil.tripsyn2mc(protein, 1, 20);

        // R is followed by P, so no cleavage there
        // Only cleavage after K
        assertTrue(result.contains("MKRPK"));
        assertTrue(result.contains("VYK"));
        assertFalse(result.contains("MKR")); // Should not cleave before P
    }

    @Test
    void tripsyn2mcNoCleavageSites() {
        // Test protein with no R or K
        String protein = "MLACDEFG";
        List<String> result = BioUtil.tripsyn2mc(protein, 1, 20);

        assertEquals(1, result.size());
        assertTrue(result.contains("MLACDEFG"));
    }

    @Test
    void tripsyn2mcLengthFiltering() {
        String protein = "MKRKVYK";

        // Test min length filtering
        List<String> result = BioUtil.tripsyn2mc(protein, 3, 20);
        assertFalse(result.contains("MK")); // Too short
        assertFalse(result.contains("R"));  // Too short
        assertTrue(result.contains("KVYK"));
        assertTrue(result.contains("MKR"));

        // Test max length filtering
        result = BioUtil.tripsyn2mc(protein, 1, 3);
        assertTrue(result.contains("MK"));
        assertTrue(result.contains("R"));
        assertFalse(result.contains("KVYK")); // Too long
        assertFalse(result.contains("RKVYK")); // Too long
    }

    @Test
    void tripsyn2mcComplex() {
        // Test with more complex sequence from your existing test
        String protein = "MTKHYRMIPRVRPEAGYREIFSALGTGHDPKFIAAFAEVLRSSLSLESAPILTPSGRAALYYLFLAVPLRRVYLPAYTCWVVAEAAELAGKKIHLLDIAYPGLHIKPSEIERIRFKPGIVVATHQFGYPEDAAAIRNILDETSFMIVEDCAGAMFSRYQNKPVGLTGDAAIFSFEEGKLWTLGRGGALAVKDKALRFQIQNIMKRTCTPGTSRINLFRLIKRRFLTHLFVYNLLLRMYLRFYPPTEGIHEFSRELTEDYTRTFSSYQARLGLIMEPRIKEIARRKRELFEYYYRATADIAGIRRVEALPQSEVCPMRFPILVPPENKLAIYSRMKSLGIDLGFSFSYFLGNEKQCPGAARMAKECLNLPVYSEINLPIARNIVNLMGSCMSDMS";

        List<String> result = BioUtil.tripsyn2mc(protein, 7, 30);

        // Should contain peptides with different missed cleavage levels
        assertTrue(result.size() > 0);

        // Verify some expected peptides exist
        boolean hasShortPeptides = result.stream().anyMatch(p -> p.length() <= 15);
        boolean hasLongPeptides = result.stream().anyMatch(p -> p.length() >= 20);

        assertTrue(hasShortPeptides, "Should have peptides with 0 missed cleavages");
        assertTrue(hasLongPeptides, "Should have peptides with 1-2 missed cleavages");
    }

    @Test
    void tripsyn2mcEmptyProtein() {
        String protein = "";
        List<String> result = BioUtil.tripsyn2mc(protein, 1, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void tripsyn2mcSingleAminoAcid() {
        String protein = "K";
        List<String> result = BioUtil.tripsyn2mc(protein, 1, 10);
        assertTrue(result.contains("K"));
        assertEquals(1, result.size());
    }
}
