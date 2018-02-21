package hr.pbf.digestdb.uniprot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;

import hr.pbf.digestdb.util.BioUtil;

public class UniprotXmlCleanProbe {

	public static void main(String[] args) throws IOException {
		BufferedReader in = BioUtil.newFileReader("F:\\Downloads\\uniprot\\uniprot_sprot.xml", "US-ASCII");
		BufferedWriter out = BioUtil.newFileWiter("F:\\Downloads\\uniprot\\uniprot_sprot2.xml", "US-ASCII");
		String line = in.readLine();
		boolean write = true;
		while(line != null)  {
			
			line = line.trim();
			write = isWrite(line);
			
			line = in.readLine();
			
		}
		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);
	}

	private static boolean isWrite(String line) {
		if(line.startsWith("<reference")) {
			return false;
		}
		
		if(line.startsWith("</reference>")) {
			return true;
		}
		
		return true;
	}
}
