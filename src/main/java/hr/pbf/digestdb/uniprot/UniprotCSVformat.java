package hr.pbf.digestdb.uniprot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideMassAccTaxList;
import hr.pbf.digestdb.util.BioUtil;


/**
 * CSV format like this:
 * "500.20532	ACPNP	A0A2A9NT24:703135,A0A2A9NB66:703135"
 * @author tag
 *
 */
public class UniprotCSVformat {

	private String pathCsv;

	public UniprotCSVformat(String pathCsv) {
		this.pathCsv = pathCsv;
	}

	public void readLines(CallbackReadLine callback)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		try (BufferedReader reader = BioUtil.newFileReader(pathCsv, "ASCII")) {
			String line = null;
			while ((line = reader.readLine()) != null) {

				// 500.18015 AGGGCH A0A1F7PX67:1802111,A0A1F7NB42:1802102
				PeptideMassAccTaxList result = parseCSVLine(line);
				callback.readedOne(result);
			}
		}
	}

	public static PeptideMassAccTaxList parseCSVLine(String line) {
		String[] split = StringUtils.split(line, "\t");
		float mass = Float.parseFloat(split[0]);
		String peptide = split[1];
		String accTaxLines = split[2];
		String[] splitAccTax = StringUtils.split(accTaxLines, ",");
		ArrayList<AccTax> accTaxList = new ArrayList<>(splitAccTax.length);
		for (int i = 0; i < splitAccTax.length; i++) {
			String[] s = StringUtils.split(splitAccTax[i], ":");
			
			accTaxList.add(new AccTax(s[0], Integer.parseInt(s[1])));
		}
		return new PeptideMassAccTaxList(peptide, mass, accTaxList);

	}

	public static interface CallbackReadLine {

		public void readedOne(PeptideMassAccTaxList result);
	}
}
