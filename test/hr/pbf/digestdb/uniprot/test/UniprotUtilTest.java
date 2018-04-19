package hr.pbf.digestdb.uniprot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import hr.pbf.digestdb.uniprot.A7_UniprotProtNamesToLevelDB;
import hr.pbf.digestdb.uniprot.UniprotModel;
import hr.pbf.digestdb.uniprot.UniprotUtil;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxMass;
import hr.pbf.digestdb.uniprot.UniprotParseUtil;

class UniprotUtilTest {

	@Test
	void testRemoveLine() {
		String res = UniprotParseUtil
				.removeEvidenceAtributes("A0A109ZZL0      Methyl coenzyme-M reductase {ECO:0000313|EMBL:AMB19266.1} ");

		assertEquals("A0A109ZZL0      Methyl coenzyme-M reductase", res);

	}

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
		pepList.add(new PeptideAccTax("PEPTIDER", "ACC11", 1111));
		pepList.add(new PeptideAccTax("PEPTIDER", "ACC12", 1111));
		pepList.add(new PeptideAccTax("PEPTIDER", "ACC13", 1113));
		pepList.add(new PeptideAccTax("PEPTIDEK", "ACC24", 1113));
		pepList.add(new PeptideAccTax("PEPTIDEK", "ACC25", 1112));

		Map<String, List<PeptideAccTax>> group = UniprotUtil.groupByPeptide(pepList);
		assertEquals(2, group.size());
		assertEquals(3, group.get("PEPTIDER").size());
		assertEquals(2, group.get("PEPTIDEK").size());

		byte[] result = UniprotUtil.toFormat2(group);

		Map<String, List<PeptideAccTaxMass>> groupNew = UniprotUtil.fromFormat2(result, false);

		// TODO: check assert
		assertEquals(group.size(), groupNew.size());
		Set<String> keySet = group.keySet();
		for (String peptide : keySet) {
			List<PeptideAccTax> a1 = group.get(peptide);
			List<PeptideAccTaxMass> a2 = groupNew.get(peptide);
			assertEquals(a1.size(), a2.size());
			for (int i = 0; i < a1.size(); i++) {
				assertEquals(a1.get(i).getAcc(), a2.get(i).getAcc());
				assertEquals(a1.get(i).getPeptide(), a2.get(i).getPeptide());
				assertEquals(a1.get(i).getTax(), a2.get(i).getTax());
			}

		}

	}

	@Test
	void testDataToPeptides() {
	}

}
