package hr.pbf.digestdb.test.probe;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.uniprot.UniprotUtil;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxMass;
import hr.pbf.digestdb.util.BioUtil;

public class ConvertToFormat2Probe {
private static final Logger log = LoggerFactory.getLogger(ConvertToFormat2Probe.class);

	public static void main(String[] args) throws IOException {
		
		
		
//		toCsv("C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\ne radi\\1728.0.db");
//		log.debug("csv");
//		if(true) {
//			return;
//		}
		String path = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\ne radi\\1728.0.f2s";
		byte[] b = UniprotUtil.toByteArrayFast(path);
//		b = UniprotUtil.uncompress(b);
		byte[] uncompress = UniprotUtil.uncompress(b);
		Files.write(Paths.get(path+".uncompress"), uncompress);
		
//		if(true) {
//			return;
//		}
		
		log.debug("start");
		Map<String, List<PeptideAccTaxMass>> data = UniprotUtil.fromFormat2(uncompress, true);
		Set<String> peptides = data.keySet();
		for (String pep : peptides) {
			if("NAKIIFVRPLLGLFK".equals(pep)) {
				writeAll(data.get(pep));
			}
//			System.out.println(pep);
		}
		
	}

	private static void toCsv(String path) throws IOException {
		ArrayList<PeptideAccTax> b = UniprotUtil.fromFormat1(UniprotUtil.toByteArrayFast(path));
		BufferedWriter out = BioUtil.newFileWiter(path+".csv", "ASCII");
		for (PeptideAccTax p: b) {
			out.write(p.getPeptide()+"\t"+p.getAcc()+"\t"+p.getTax()+"\n");
		}
		out.close();
		
	}

	private static void writeAll(List<PeptideAccTaxMass> list) {
		for (PeptideAccTaxMass p: list) {
			System.out.println(p.getMass() + "\t"+p.getPeptide() + "\t"+ p.getAcc());
		}
	}
}
