package hr.pbf.digestdb.uniprot.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxMass;

public class TestModel {

	
	@Test
	void testModel1() throws Exception {
		PeptideAccTaxMass p = new PeptideAccTaxMass("PEPTIDE", "ACC22", 234, 1000.3f);
		assertEquals("PEPTIDE", p.getPeptide());
		assertEquals("ACC22", p.getAcc());
		assertEquals(234, p.getTax());
		assertEquals(1000.3f, p.getMass());
		
	}
}
