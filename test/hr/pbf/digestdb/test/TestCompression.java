package hr.pbf.digestdb.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearspring.analytics.stream.membership.DataInputBuffer;

import hr.pbf.digestdb.app.CompressSmall_APP;
import hr.pbf.digestdb.app.CreateSmallMenyFilesDBapp;
import hr.pbf.digestdb.app.CreateSmallMenyFilesDBapp.Row;
import hr.pbf.digestdb.util.BioUtil;

public class TestCompression {
	private static final Logger log = LoggerFactory.getLogger(TestCompression.class);

	double mass = 543522.223134232;
	String peptide = "PEPTIDE";
	long acc = 23432435423532L;

	double mass2 = 22242.232134232;
	String peptide2 = "FERFGDSGFWEPEPTIDE";
	long acc2 = 43333979732L;

	CreateSmallMenyFilesDBapp smallFilesApp = new CreateSmallMenyFilesDBapp();
	CompressSmall_APP compressApp = new CompressSmall_APP();

	@Test
	public void test1() throws Exception {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(data);

		smallFilesApp.writeRow(mass, peptide, acc, out);
		smallFilesApp.writeRow(mass2, peptide2, acc2, out);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data.toByteArray()));
		{
			Row row = smallFilesApp.readRow(in);

			assertEquals(mass, row.mass, 0);

			assertEquals(peptide, row.peptide);

			assertEquals(acc, row.accessionID);
		}

		{
			Row row = smallFilesApp.readRow(in);

			assertEquals(mass2, row.mass, 0);

			assertEquals(peptide2, row.peptide);

			assertEquals(acc2, row.accessionID);
		}
	}

	@Test
	public void test2() throws IOException {
		File tempDirectory = FileUtils.getTempDirectory();
		String path = new File(tempDirectory + "/test1compress.txt").getAbsolutePath();
		try (DataOutputStream out = BioUtil.newDataOutputStream(path)) {

			smallFilesApp.writeRow(mass, peptide, acc, out);
		}

		String fileCompressed = path + "_compress";
		compressApp.compress(path, fileCompressed);

		HashMap<String, List<Long>> result = compressApp.decompress(fileCompressed);
		assertEquals("Same ", 1, result.size());

		List<Long> ids = result.get(peptide);
		assertEquals("Same ", ids.size(), 1);

		assertEquals("SAme ids:", ids.get(0).longValue(), acc);

	}

	@AfterClass
	public static void shutDown() {
		log.debug("shutdown");
	}
}
