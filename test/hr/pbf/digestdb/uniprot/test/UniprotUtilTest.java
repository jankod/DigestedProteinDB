package hr.pbf.digestdb.uniprot.test;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hr.pbf.digestdb.uniprot.UniprotModel;
import hr.pbf.digestdb.uniprot.UniprotUtil;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import junit.framework.Assert;

class UniprotUtilTest {

	@Test
	void testReduceMasses() throws Exception {
		Map<String, Double> masses = new HashMap<>();

		masses.put("P1", 99.99d); // remove
		masses.put("P2", 100d);
		masses.put("P3", 499.999d);
		masses.put("P4", 500d);
		masses.put("P5", 500.0001d); // remove

		UniprotUtil.reduceMasses(masses, 100, 500);
		assertEquals(3, masses.size());
		assertTrue(masses.containsKey("P2"));
		assertTrue(masses.containsKey("P3"));
		assertTrue(masses.containsKey("P3"));

	}

	@Test
	void testWriteReadDataOutput() throws Exception {
		FastByteArrayOutputStream bout = new FastByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bout);
		PeptideAccTax p = new PeptideAccTax("PEPTIDE", "ACC222", 23232);
		UniprotUtil.writeOneFormat1(out, p.getPeptide(), p.getTax(), p.getAcc());
		PeptideAccTax res = UniprotUtil.readOneFormat1(new DataInputStream(new FastByteArrayInputStream(bout.array)));
		
		assertEquals(p, res);
		
	}

	@Test
	void testPeptideToDataAndBackStream() throws IOException {
		ArrayList<PeptideAccTax> pepList = new ArrayList<>();
		pepList.add(new PeptideAccTax("PEPTIDE1", "ACC11", 1111));
		pepList.add(new PeptideAccTax("PEPTIDE1", "ACC12", 1111));
		pepList.add(new PeptideAccTax("PEPTIDE1", "ACC13", 1113));
		pepList.add(new PeptideAccTax("PEPTIDE2", "ACC24", 1113));
		pepList.add(new PeptideAccTax("PEPTIDE2", "ACC25", 1112));

		Map<String, List<PeptideAccTax>> group = UniprotUtil.groupByPeptide(pepList);
		assertEquals(2, group.size());
		assertEquals(3, group.get("PEPTIDE1").size());
		assertEquals(2, group.get("PEPTIDE2").size());

		byte[] result = UniprotUtil.peptideToFormat2(group);

		Map<String, List<PeptideAccTax>> groupNew = UniprotUtil.format2ToPeptides(result);
		assertEquals(group, groupNew);

	}

	@Test
	void testDataToPeptides() {
	}

}
