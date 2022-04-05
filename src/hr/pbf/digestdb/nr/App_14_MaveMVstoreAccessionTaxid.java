package hr.pbf.digestdb.nr;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MassCSV;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;

public class App_14_MaveMVstoreAccessionTaxid {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		MVStore s = new MVStore.Builder().cacheSize(1024)
				.fileName("/home/users/tag/nr_db/mvstore_prot.accession2taxid_all.db").compress().open();
		MVMap<Object, Object> map = s.openMap("a");

		addToDB("/home/users/tag/nr_db/accession2taxid/prot.accession2taxid", map, false);

		addToDB("/home/users/tag/nr_db/accession2taxid/dead_prot.accession2taxid", map, true);

	//	s.compact()
		s.close();
	}

	private static void addToDB(String string, MVMap<Object, Object> map, boolean isDead)
			throws FileNotFoundException, IOException {
		MutableString line = new MutableString(220);
		try (FastBufferedReader reader = new FastBufferedReader(new FileReader(string))) {
			reader.readLine(line); // header
			while ((reader.readLine(line)) != null) {
				String[] split = StringUtils.split(line.toString(), '\t');
				String acc =  split[0].trim();
				//String accVersion = split[1];
				//String acc = BioUtil.removeVersionFromAccession(accVersion);
				if (isDead) {
					map.put(acc, -2);
				} else {
					 int taxId = Integer.parseInt(split[2]);
					 map.put(acc, taxId);
				}

			}
		}

	}
}
