package hr.pbf.digestdb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

import gnu.trove.map.TFloatIntMap;
import gnu.trove.map.hash.TFloatIntHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.google.common.primitives.Floats;

import hr.pbf.digestdb.uniprot.MassPeptideArrayIndex;
import hr.pbf.digestdb.uniprot.MassPeptideArrayIndex.SubMassPeptideIndex;

public class TestMassPeptideArrayIndex {

    @Test
    void test1() throws Exception {

        // float[] masses = new float[] { 500.34f, 600.34f, 700.34f, 2000, 6000 };
        // int[] peptides = new int[] { 1, 2, 3, 4, 5 };

        TreeMap<Float, Integer> map = new TreeMap<>();

        map.put(500.0000f, 5);
        map.put(700.3300f, 7);
        map.put(840.3240f, 34);
        map.put(840.3250f, 12332);
        map.put(1200.333f, 52323);
        map.put(1200.334f, 532423);
        map.put(4000.323f, 52232);
        map.put(4202f, 5232);
        map.put(4430f, 53223232);
        map.put(6000f, 52323);


        TFloatIntMap map1 = new TFloatIntHashMap();


        MassPeptideArrayIndex index = new MassPeptideArrayIndex(map.size());
        Set<Entry<Float, Integer>> entrySet = map.entrySet();
        int i = 0;
        for (Entry<Float, Integer> entry : entrySet) {
            index.put(i, entry.getKey(), entry.getValue());
            i++;
        }
        assertThrows(RuntimeException.class, () -> index.search(100, 50));


        {
            SubMassPeptideIndex res150_200 = index.search(150, 200);
            assertEquals(0, res150_200.masses.length);
            assertEquals(0, res150_200.peptides.length);
        }
        {

            SubMassPeptideIndex res = index.search(500, 6000);
            float[] array = Floats.toArray(map.keySet());
            assertArrayEquals(array, res.masses);
        }

    }
}
