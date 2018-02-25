package hr.pbf.digestdb.uniprot.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import hr.pbf.digestdb.uniprot.A_X2_UniprotCompressSmallFilesLevelDb;
import hr.pbf.digestdb.uniprot.MyDataOutputStream;
import hr.pbf.digestdb.uniprot.UniprotModel;
import hr.pbf.digestdb.uniprot.UniprotUtil;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.util.BioUtil;
import static java.util.stream.Collectors.groupingBy;

public class SnappyTest {

	private static final Logger log = LoggerFactory.getLogger(SnappyTest.class);

	static String pathIn = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\615.2.db_grouped";
	static String pathOut = pathIn + ".zip";


	public static void main22(String[] args) throws IOException {
		// convert to structure grouped by peptide
		ArrayList<PeptideAccTax> data = A_X2_UniprotCompressSmallFilesLevelDb.parseDataFile(new File(pathIn));

		Map<String, List<PeptideAccTax>> grouped = data.stream().collect(groupingBy(o -> {
			return o.getPeptide();
		}));
		MyDataOutputStream out = BioUtil.newDataOutputStream(pathIn + "_grouped");
		Set<Entry<String, List<PeptideAccTax>>> entrySet = grouped.entrySet();
		out.writeInt(entrySet.size());
		for (Entry<String, List<PeptideAccTax>> entry : entrySet) {
			String peptide = entry.getKey();
			List<PeptideAccTax> p = entry.getValue();
			out.writeUTF(peptide);
			out.writeInt(p.size());
			for (PeptideAccTax pAccTax : p) {
				out.writeUTF(pAccTax.getAcc());
				out.writeInt(pAccTax.getTax());
			}
		}
		out.close();

	}

	public static void main11(String[] args) throws IOException {
		// sort one .db file
		ArrayList<PeptideAccTax> res = A_X2_UniprotCompressSmallFilesLevelDb.parseDataFile(new File(pathIn));
		// res.sort(new Comparator<PeptideAccTax>() {
		//
		// @Override
		// public int compare(PeptideAccTax o1, PeptideAccTax o2) {
		// return o1.getPeptide().compareTo(o2.getPeptide());
		// }
		// });
		res.sort(Comparator.comparing(PeptideAccTax::getPeptide));

		MyDataOutputStream out = BioUtil.newDataOutputStream(pathIn + "_sorted");
		for (PeptideAccTax p : res) {
			UniprotUtil.writeOneFormat1(out, p.getPeptide(), p.getTax(), p.getAcc());
		}
		out.close();
		log.debug("Finish");

	}

	/**
	 * Snappy sa 155 MB (163.351.767 bytes) na 85,3 MB (89.491.296 bytes)
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// compress with snappy

		FileInputStream fin = new FileInputStream(new File(pathIn + ""));
		byte[] byteArray = IOUtils.toByteArray(fin);
		StopWatch s = new StopWatch();
		s.start();
		byte[] compressed = Snappy.compress(byteArray);
		s.stop();
		fin.close();
		IOUtils.write(compressed, new FileOutputStream(pathIn + "_sorted.zip"));
		log.debug("time " + DurationFormatUtils.formatDurationHMS(s.getTime()));

	}
}
