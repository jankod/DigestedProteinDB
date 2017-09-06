package hr.pbf.digestdb.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearspring.analytics.stream.membership.DataInputBuffer;

import hr.pbf.digestdb.app.CreateSmallMenyFilesDBapp;
import hr.pbf.digestdb.app.CreateSmallMenyFilesDBapp.Row;

public class TestCompression {
	private static final Logger log = LoggerFactory.getLogger(TestCompression.class);

	@Test
	public void test1() throws Exception {
		CreateSmallMenyFilesDBapp a = new CreateSmallMenyFilesDBapp();
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(data);
		double mass = 222.232134232;
		String peptide = "PEPTIDE";
		long accessionID = 33335423532L;
		a.writeRow(mass, peptide, accessionID, out);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data.toByteArray()));
		Row row = a.readRow(in);

		assertEquals(mass, row.mass, 0);

		assertEquals(peptide, row.peptide);

		assertEquals(accessionID, row.accessionID);

	}

	@AfterClass
	public void shutDown() {
		// TODO Auto-generated method stub

	}
}
