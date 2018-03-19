package hr.pbf.digestdb.test.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.xerial.snappy.Snappy;

import hr.pbf.digestdb.uniprot.UniprotUtil;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxMass;

public class CanotReadBug {

	public static void main(String[] args) throws IOException {
		String path = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\nemoze citati\\2801.3.db";
		byte[] bytes = UniprotUtil.toByteArrayFast(path);
		ArrayList<PeptideAccTax> res = UniprotUtil.fromFormat1(bytes);
		byte[] format2 = UniprotUtil.toFormat2(UniprotUtil.groupByPeptide(res));
		format2 = Snappy.compress(format2);
		FileOutputStream output = new FileOutputStream(new File(path+".f2s.new"));
		IOUtils.write(format2, output);
		output.close();
		System.out.println("result"+ res.size());
		
	}
	public static void main22(String[] args) throws IOException {
		String path = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\nemoze citati\\2801.3.db.f2s.new";
		byte[] bytes = UniprotUtil.toByteArrayFast(path);
		bytes = Snappy.uncompress(bytes);
		
//		FileOutputStream output = new FileOutputStream(new File(path+".uncompresses.txt"));
//		IOUtils.write(bytes, output);
//		output.close();
		Map<String, List<PeptideAccTaxMass>> data = UniprotUtil.fromFormat2(bytes, true);
		
		System.out.println(data.size());
		
		
	}
}
