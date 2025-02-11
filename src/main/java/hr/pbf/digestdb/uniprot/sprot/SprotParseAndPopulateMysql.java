package hr.pbf.digestdb.uniprot.sprot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;

import hr.pbf.digestdb.uniprot.A1_UniprotToFormat1;
import hr.pbf.digestdb.uniprot.UniprotUtil;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.UniprotConfig;
import hr.pbf.digestdb.util.UniprotConfig.Name;
import hr.pbf.digestdb.uniprot.UniprotModel.CallbackUniprotReader;
import hr.pbf.digestdb.uniprot.UniprotModel.EntryUniprot;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;

public class SprotParseAndPopulateMysql {

	private static final Logger	log	= LoggerFactory.getLogger(SprotParseAndPopulateMysql.class);
	private BufferedWriter		outPeptides;
	private BufferedWriter		outPeptidesProtein;

	public static void main(String[] args) throws IOException {

		EntryUniprot u = new EntryUniprot();

		// String pathSprot = "F:\\ProteinReader\\UniprotDBfiles\\uniprot_sprot.dat";
		// A1_UniprotToFormat1.readUniprotTextLarge(pathSprot, new
		// CallbackUniprotReader() {
		//
		// @Override
		// public void readEntry(EntryUniprot e) {
		// log.debug(e.toString());
		// }
		// }, 20);

		SprotParseAndPopulateMysql i = new SprotParseAndPopulateMysql();
		i.outPeptides = BioUtil.newFileWiter(UniprotConfig.get(Name.BASE_DIR) +  "peptides.csv",
				"ASCII");
		i.outPeptidesProtein = BioUtil
				.newFileWiter(UniprotConfig.get(Name.BASE_DIR) +  "peptides_protein_part.csv", "ASCII");

		i.frormat1toCSVtables(UniprotConfig.get(Name.BASE_DIR) +  "uniprot_trembl_part.dat_format1");

		i.closeAll();
	}

	private void closeAll() {
		IOUtils.closeQuietly(outPeptides);
		IOUtils.closeQuietly(outPeptidesProtein);
	}

	private void frormat1toCSVtables(String dirFormat1) throws IOException {
		log.debug("parse format 1: {}", dirFormat1);

		int peptideIdAutoincrement = 1;
		File[] listFiles = new File(dirFormat1).listFiles();
		for (File f : listFiles) {
			//log.debug("work " + f.getName());
			byte[] format1bytes = UniprotUtil.toByteArrayFast(f);

			ArrayList<PeptideAccTax> pepList = UniprotUtil.fromFormat1(format1bytes);

			Map<String, List<PeptideAccTax>> group = UniprotUtil.groupByPeptide(pepList);

			Set<Entry<String, List<PeptideAccTax>>> entrySet = group.entrySet();
			for (Entry<String, List<PeptideAccTax>> entry : entrySet) {
				String peptide = entry.getKey();
				writePeptideTable(peptideIdAutoincrement, peptide, BioUtil.calculateMassWidthH2O(peptide));

				for (PeptideAccTax accTax : entry.getValue()) {
					writePeptideProteinTable(peptideIdAutoincrement, accTax.getAcc());
				}
				peptideIdAutoincrement++;
			}
			
			
			//StoreReader reader = PalDB.createReader(new File("store.paldb"));

//			if (peptideIdAutoincrement > 324233) {
//				log.debug("finis");
//
//				return;
//			}
		} // end for
	}

	private void writePeptideProteinTable(int peptideIdAutoincrement, String acc) throws IOException {
		outPeptidesProtein.write(acc + "\t" + peptideIdAutoincrement + "\n");
	}

	private void writePeptideTable(int peptideIdAutoincrement, String pepetide, double mass) throws IOException {
		outPeptides.write(peptideIdAutoincrement + "\t" + pepetide + "\t" + (float) mass + "\n");
	}
}
