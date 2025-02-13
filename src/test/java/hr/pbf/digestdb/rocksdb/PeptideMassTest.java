package hr.pbf.digestdb.rocksdb;


import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PeptideMassTest {
    @Test
    public void test1() {
        PeptideByMass p = new PeptideByMass();
        p.setPeptide(p.randomPeptides(15));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        SerializationUtils.serialize(p, outputStream);

        PeptideByMass result = SerializationUtils.deserialize(outputStream.toByteArray());

        assertEquals(p.getPeptide(), result.getPeptide());
        assertEquals(p.getAccessions(), result.getAccessions());
        assertEquals(p.getTaxonomyIds(), result.getTaxonomyIds());


    }

}
