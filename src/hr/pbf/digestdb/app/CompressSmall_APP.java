package hr.pbf.digestdb.app;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.iq80.snappy.SnappyFramedOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.BitShuffle;
import org.xerial.snappy.BitShuffleNative;

import hr.pbf.digestdb.cli.IApp;
import hr.pbf.digestdb.util.BioUtil;

/**
 * Kompresira file gdje je slozeno po masama sa
 * {@link CreateSmallMenyFilesDBapp}.
 * 
 * @author tag
 *
 */
public class CompressSmall_APP implements IApp {
	private static final Logger log = LoggerFactory.getLogger(CompressSmall_APP.class);

	@Override
	public void populateOption(Options o) {

	}

	@Override
	public void start(CommandLine appCli) {
		String folderPath = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\small_store";

		File f = new File(folderPath);
		File[] listFiles = f.listFiles();
		for (File file : listFiles) {
			try {
				compress(file.getAbsolutePath(), file.getAbsoluteFile() + ".compress");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) {
		CompressSmall_APP app = new CompressSmall_APP();
//		app.start(null);
		
		
		
	}

	public HashMap<String, List<Long>> uncompress(String fileIn) throws IOException {
		HashMap<String, List<Long>> result = new HashMap<>();
		try (DataInputStream in = BioUtil.newDataInputStreamCompressed(fileIn)) {
			while (in.available() > 0) {
				String peptide = in.readUTF();
				// in.read()

			}

		}

		return null;

	}

	public TreeSet<PeptideMassIdRow> compress(String fileIn, String fileOut) throws IOException {
		TreeSet<PeptideMassIdRow> rows = new TreeSet<>();

		try (DataInputStream in = BioUtil.newDataInputStream(fileIn)) {
			while (in.available() > 0) {
				double mass = in.readDouble();
				long id = in.readLong();
				String peptide = in.readUTF();
				PeptideMassIdRow row = new PeptideMassIdRow(mass, id, peptide);
				rows.add(row);
			}
		}

		try (DataOutputStream out = BioUtil.newDataOutputStreamCompresed(fileOut)) {

			HashMap<String, List<PeptideMassIdRow>> map = new HashMap<>();

			for (PeptideMassIdRow row : rows) {
				List<PeptideMassIdRow> listRows = map.get(row.peptide);
				if (listRows == null) {
					listRows = new ArrayList<>();
					map.put(row.peptide, listRows);
				}
				listRows.add(row);
			}

			// Zapis: UTF:peptide, INT:kolko ID ima: LONG:ID-evi....
			Set<Entry<String, List<PeptideMassIdRow>>> entrySet = map.entrySet();
			for (Entry<String, List<PeptideMassIdRow>> entry : entrySet) {
				String peptide = entry.getKey();
				out.writeUTF(peptide);
				List<PeptideMassIdRow> rowsByPeptide = entry.getValue();
				// long[] arrayID = new long[rowsByPeptide.size()];

				// int i = 0;
				out.writeShort(rowsByPeptide.size());
				for (PeptideMassIdRow row : rowsByPeptide) {
					// arrayID[i] = row.id;
					out.writeLong(row.id);
				}
				// byte[] shuffle = BitShuffle.shuffle(arrayID);
				// out.write(shuffle);
			}
		}

		return rows;

	}

	public static class PeptideMassIdRow implements Comparable<PeptideMassIdRow> {
		String peptide;
		double mass;
		long id;

		public PeptideMassIdRow(double mass, long id, String peptide) {
			this.mass = mass;
			this.id = id;
			this.peptide = peptide;
		}

		@Override
		public int compareTo(PeptideMassIdRow o) {
			return this.peptide.compareTo(o.peptide);
		}

	}
}
