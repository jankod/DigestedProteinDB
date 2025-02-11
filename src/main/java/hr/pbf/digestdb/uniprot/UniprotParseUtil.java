package hr.pbf.digestdb.uniprot;

public class UniprotParseUtil {

	/**
	 * @formatter:off
	 * Parse: AC Q6GZW3; . 
	 * ili ako ima više:
	 * AC   Q92892; Q92893; Q92894; Q92895; Q93053; Q96KU9; Q96KV0; Q96KV1;
	 * 
	 * @formatter:on
	 * @param line
	 * @return prvi taxid samo
	 * 
	 */
	public static String parseFirstAccession(String line) {
		line = line.substring(5);
		int indexOf = line.indexOf(";");
		return line.substring(0, indexOf).trim();
	}

	/**
	 * Remove string like: https://web.expasy.org/docs/userman.html 2.4. Evidence
	 * attributions . {ECO:0000269|PubMed:10433554}
	 * 
	 * @param line
	 * @return
	 */
	public static String removeEvidenceAtributes(String line) {
		int ecoIndex = line.indexOf("{ECO:");
		if (ecoIndex == -1) {
		
			//log.warn("Not find '{ECO:', line:  '" + line + "'");
			return line;
		}
		String desc = line.substring(0, ecoIndex);
		return desc.trim();
	}
	
	/**
	 * @formatter:on
	 * Parse something like this: OX NCBI_TaxID=30343; Trembl have this: NCBI_TaxID=418404
	 * {ECO:0000313|EMBL:AHZ18584.1}; Must be like this: "NCBI_TaxID=3617
	 * {ECO:0000305};"
	 * 
	 * @formatter:off
	 * @param line
	 * @return
	 */
	public static int parseTaxLine(String line) {
			line = line.substring(16).trim();
			int pos = line.indexOf(';');
			if (line.contains("{")) {
				pos = line.indexOf(" ");
			}

			String striTaxID = line.substring(0, pos);
			return Integer.parseInt(striTaxID);
	}


}
