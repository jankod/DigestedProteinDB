package hr.pbf.digestdb.test.probe;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxMass;
import hr.pbf.digestdb.uniprot.UniprotUtil;

public class NotWorkProbe {

	public static void main(String[] args) throws IOException {
		String path = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\ne radi\\1728.0.f2s";
		
		byte[] b = UniprotUtil.toByteArrayFast(path);
		b = UniprotUtil.uncompress(b);
		Map<String, List<PeptideAccTaxMass>> result = UniprotUtil.fromFormat2(b, true);
		result.forEach(new BiConsumer<String, List<PeptideAccTaxMass>>() {

			@Override
			public void accept(String peptide, List<PeptideAccTaxMass> u) {
				if("NAKIIFVRPLLGLFK".equals(peptide)) {
					for (PeptideAccTaxMass p : u) {
						System.out.println(p);
					}
				}
			}
		});
		
	}
}
