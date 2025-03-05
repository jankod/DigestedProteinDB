package hr.pbf.digestdb.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AminoAcid5bitCoderTest {

    @Test
    void decodePeptide() {
        String sequence = "ACDEFGHIKLMNPQRSTVWY";
        byte[] encoded = AminoAcid5bitCoder.encodePeptide(sequence);
        String decoded = AminoAcid5bitCoder.decodePeptide(encoded, sequence.length());
        assertEquals(sequence, decoded);
    }

    @Test
    void testEmpty() {
        String sequence = "";
        byte[] encoded = AminoAcid5bitCoder.encodePeptide(sequence);
        String decoded = AminoAcid5bitCoder.decodePeptide(encoded, sequence.length());
        assertEquals(sequence, decoded);
    }

    void testAllAminoAcids() {
        String sequence = "ACDEFGHIKLMNPQRSTVWY";
        byte[] encoded = AminoAcid5bitCoder.encodePeptide(sequence);
        String decoded = AminoAcid5bitCoder.decodePeptide(encoded, sequence.length());
        assertEquals(sequence, decoded);
    }

}
