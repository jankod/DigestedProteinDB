package hr.pbf.digestdb.test.proba;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.CharSet;

import hr.pbf.digestdb.test.Smaz;

public class CompressPeptideProba {

	public static void main(String[] args) {
		String peptide = "ELTPEAVDLLSK";
		
		Deflater d = new Deflater(Deflater.BEST_COMPRESSION);
		d.setInput(peptide.getBytes(StandardCharsets.US_ASCII));
		d.finish();
		byte[] result = new byte[50];
		int length = d.deflate(result);
		System.out.println("input "+ peptide.length() + " output "+ length);
		
		Smaz.main(args);
		
		
	}
	
}
