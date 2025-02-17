package hr.pbf.digestdb.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AminoAcidCoderTest {

    @Test
    void decodePeptideByteBuffer() {
        String sequence = "ACDEFGHIKLMNPQRSTVWY";
        byte[] encoded = AminoAcidCoder.encodePeptideByteBuffer(sequence);
        String decoded = AminoAcidCoder.decodePeptideByteBuffer(encoded, sequence.length());
        assertEquals(sequence, decoded);
    }

}
