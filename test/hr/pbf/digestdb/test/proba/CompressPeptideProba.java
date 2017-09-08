package hr.pbf.digestdb.test.proba;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharSet;

import hr.pbf.digestdb.util.BioUtil;

public class CompressPeptideProba {

	public static void main1(String[] args) {
		String peptide = "ELTPEAVDLLSK";
		
		Deflater d = new Deflater(Deflater.BEST_COMPRESSION);
		d.setInput(peptide.getBytes(StandardCharsets.US_ASCII));
		d.finish();
		byte[] result = new byte[50];
		int length = d.deflate(result);
		System.out.println("input "+ peptide.length() + " output "+ length);
		
		Smaz.main(args);
		
		
	}
	public static void main(String[] args) throws IOException {
		String path = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\small_store\\proba.bin";
		DataOutputStream out = BioUtil.newDataOutputStream(path);
		
		out.writeUTF("PEPTIDE");
		out.writeLong(232323);
		
		out.close();
		
		System.out.println(new File(path).length());
		
		
		
	}
	
}
