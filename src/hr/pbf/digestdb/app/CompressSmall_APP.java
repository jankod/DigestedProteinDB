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

import hr.pbf.digestdb.cli.IApp;
import hr.pbf.digestdb.util.BioUtil;

public class CompressSmall_APP implements IApp {

	@Override
	public void populateOption(Options o) {

	}

	@Override
	public void start(CommandLine appCli) {
		String folderPath = "";

		File f = new File(folderPath);
		File[] listFiles = f.listFiles();
		for (File file : listFiles) {
			try {
				compress(file);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void compress(File file) throws IOException {
		TreeSet<Row> rows = new TreeSet<>();

		DataInputStream in = BioUtil.newDataInputStream(file.getAbsolutePath());
		while (in.available() > 0) {
			double mass = in.readDouble();
			long id = in.readLong();
			String peptide = in.readUTF();
			Row row = new Row(mass, id, peptide);
			rows.add(row);
		}
		DataOutputStream out = BioUtil.newDataOutputStream(file.getAbsolutePath() + ".compress");

		HashMap<String, List<Row>> map = new HashMap<>();

		for (Row row : rows) {
			List<Row> listRows = map.get(row.peptide);
			if (listRows == null) {
				listRows = new ArrayList<>();
				map.put(row.peptide, listRows);
			}
			listRows.add(row);
		}

		Set<Entry<String, List<Row>>> entrySet = map.entrySet();
		for (Entry<String, List<Row>> entry : entrySet) {
			String peptide = entry.getKey();
			
		}

	}

}

class Row implements Comparable<Row> {
	String peptide;
	double mass;
	long id;

	public Row(double mass, long id, String peptide) {
		this.mass = mass;
		this.id = id;
		this.peptide = peptide;
	}

	@Override
	public int compareTo(Row o) {
		return this.peptide.compareTo(o.peptide);
	}

}