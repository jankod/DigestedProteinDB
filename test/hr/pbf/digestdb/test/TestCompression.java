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
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearspring.analytics.stream.membership.DataInputBuffer;

import hr.pbf.digestdb.nr.App_3_CreateMenyFilesFromCSV;
import hr.pbf.digestdb.nr.App_4_CompressManyFilesSmall;
import hr.pbf.digestdb.nr.App_3_CreateMenyFilesFromCSV.Row;
import hr.pbf.digestdb.nr.App_4_CompressManyFilesSmall.PeptideMassIdRow;
import hr.pbf.digestdb.uniprot.MyDataOutputStream;
import hr.pbf.digestdb.util.BioUtil;

public class TestCompression {
	private static final Logger log = LoggerFactory.getLogger(TestCompression.class);

	double mass = 543522.223134232;
	String peptide = "PEPTIDE";
	String acc = "XP_642131.1";

	double mass2 = 22242.232134232;
	String peptide2 = "FERFGDSGFWEPEPTIDE";
	String acc2 = "WP_000184067.1";

	App_3_CreateMenyFilesFromCSV smallFilesApp = new App_3_CreateMenyFilesFromCSV();
	App_4_CompressManyFilesSmall compressApp = new App_4_CompressManyFilesSmall();

	@Test
	public void test1() throws Exception {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		MyDataOutputStream out = new MyDataOutputStream(data);

		smallFilesApp.writeRow(mass, peptide, acc, out);
		smallFilesApp.writeRow(mass2, peptide2, acc2, out);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data.toByteArray()));
		{
			Row row = smallFilesApp.readRow(in);

		//	assertEquals(mass, row.mass, 0);

			assertEquals(peptide, row.peptide);

			assertEquals(acc, row.accessionID);
		}

		{
			Row row = smallFilesApp.readRow(in);

		//	assertEquals(mass2, row.mass, 0);

			assertEquals(peptide2, row.peptide);

			assertEquals(acc2, row.accessionID);
		}
	}

	@Test
	public void test2() throws IOException {
		File tempDirectory = FileUtils.getTempDirectory();
		File file = new File(tempDirectory + "/test1compress.txt");

		assertTrue(file.exists());
		assertTrue(file.isFile());

		String path = file.getAbsolutePath();
		try (MyDataOutputStream out = BioUtil.newDataOutputStream(path)) {
			// one row
			smallFilesApp.writeRow(mass, peptide, acc, out);
			smallFilesApp.writeRow(mass2, peptide2, acc2, out);
		}

		String fileCompressed = path + "_compress";

		TreeSet<PeptideMassIdRow> rows = new TreeSet<>();

		// 1. READ
		try (DataInputStream in = BioUtil.newDataInputStream(path)) {
			while (in.available() > 0) {
				double mass = in.readDouble();
				long id = in.readLong();
				String peptide = in.readUTF();
				PeptideMassIdRow row = new PeptideMassIdRow(mass, id, peptide);
				rows.add(row);
			}
		}

		compressApp.compress(rows, fileCompressed);

		long length = new File(fileCompressed).length();
		assertTrue("Mora biti file veci od 1 length", length > 0);
		log.debug("File je {} bytes", length);

		HashMap<String, List<Long>> result = compressApp.decompress(fileCompressed);
		log.debug("result {}", result);
		assertEquals("Same ", 2, result.size());

		{
			List<Long> ids = result.get(peptide);
			assertEquals("Same ", 1, ids.size());
			assertEquals("SAme ids:", ids.get(0).longValue(), acc);
		}

		{
			List<Long> ids = result.get(peptide2);
			assertEquals("Same ", 1, ids.size());
			assertEquals("SAme ids:", ids.get(0).longValue(), acc2);
		}
		

	}

	@AfterClass
	public static void shutDown() {
		log.debug("shutdown");
	}
}
