package hr.pbf.digestdb.uniprot;

import static hr.pbf.digestdb.util.BioUtil.calculateMassWidthH2O;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxMass;
import hr.pbf.digestdb.util.LevelDButil;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;

public class A3_UniprotFormat2ToOther {
	private static final Logger log = LoggerFactory.getLogger(A3_UniprotFormat2ToOther.class);

	public static void main(String[] args) throws IOException {
		
		
		String dir = "/home/users/tag/uniprot/trembl_format2s";
		String outResultPath = "/home/users/tag/uniprot/trembl.csv";

		
//		dir = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\nemoze citati";
//		outResultPath = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\result.csv";
		
		log.debug("Write result " + outResultPath);
		File fileResult = new File(outResultPath);
		fileResult.delete();
		FastBufferedOutputStream out = new FastBufferedOutputStream(new FileOutputStream(fileResult), 8 * 1024 * 4);
		int count = 0;
		File[] listFiles = new File(dir).listFiles();
		ArrayList<File> files = new ArrayList<>(Arrays.asList(listFiles));
		files.sort(new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				float f1 = Float.parseFloat(FilenameUtils.getBaseName(o1.getName()));
				float f2 = Float.parseFloat(FilenameUtils.getBaseName(o2.getName()));
				return Float.compare(f1, f2);
			}
		});
//		LevelDButil levelDB = new LevelDButil("");
		FINISH: for (File file : files) {
			log.debug(file.getName());
			byte[] bytes = UniprotUtil.toByteArrayFast(file);
			bytes = Snappy.uncompress(bytes);
			Map<String, List<PeptideAccTaxMass>> data = UniprotUtil.fromFormat2(bytes, true);
//			log.debug("Unique peptides "+ data.size());
			Set<Entry<String, List<PeptideAccTaxMass>>> entrySet = data.entrySet();
			for (Entry<String, List<PeptideAccTaxMass>> entry : entrySet) {
				String peptide = entry.getKey();
				List<PeptideAccTaxMass> value = entry.getValue();
				String valuesFormated = UniprotSearchFormat2.formatAccessionsAndTax(value);
				String d = ((float) calculateMassWidthH2O(peptide)) + "\t" + peptide + "\t" + valuesFormated + "\n";
				
				out.write(d.getBytes(StandardCharsets.US_ASCII));
				count++;
//				if (count > 1_000_000 * 4) {
//					break FINISH;
//				}
			}
		}

		log.debug("Finish, count peptides: " + count);

		out.close();

	}
}
