package hr.pbf.digestdb.test.experiments;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import hr.pbf.digestdb.nr.App_3_CreateMenyFilesFromCSV;
import hr.pbf.digestdb.nr.App_4_CompressManyFilesSmall;
import hr.pbf.digestdb.nr.App_4_CompressManyFilesSmall.PeptideMassIdRow;
import hr.pbf.digestdb.uniprot.MyDataOutputStream;

public class ProbeStep4CompressManyFiles {

	public static void main222(String[] args) throws FileNotFoundException, IOException {
		// NE RADI
		String file = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\4904.3.db";
		file = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\500.3.db";
		TreeSet<PeptideMassIdRow> result = App_4_CompressManyFilesSmall.readSmallDataFile(new File(file));
		for (PeptideMassIdRow p : result) {
			System.out.println(p.mass + " " + p.id + " " + p.peptide);

		}
	}
	
	public static void main(String[] args) {
//		App_3_CreateMenyFilesFromCSV c = new App_3_CreateMenyFilesFromCSV();
//		c.start(null);
		
		App_4_CompressManyFilesSmall read = new App_4_CompressManyFilesSmall();
		read.start(null);
		
	}

	public static void main2(String[] args) throws IOException {
		// RADI
		App_3_CreateMenyFilesFromCSV c = new App_3_CreateMenyFilesFromCSV();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		MyDataOutputStream dout = new MyDataOutputStream(out);
		c.writeRow(1230D, "PEPTIDE", "WP_000184067.1", dout);
		c.writeRow(1230D, "PEPTIDESFDSDFSDFREWRWEFCSEFWS", "XP_642131.1", dout);
		dout.flush();

		File tempFile = new File(FileUtils.getTempDirectory(), "proba.txt");
		{
			FileOutputStream ooo = new FileOutputStream(tempFile);
			IOUtils.write(out.toByteArray(), ooo);
			ooo.close();
		}
		{
			TreeSet<PeptideMassIdRow> res = App_4_CompressManyFilesSmall.readSmallDataFile(tempFile);
			for (PeptideMassIdRow p : res) {
				System.out.println(p.mass + " " + p.id + " " + p.peptide);

			}
		}

	}
}
