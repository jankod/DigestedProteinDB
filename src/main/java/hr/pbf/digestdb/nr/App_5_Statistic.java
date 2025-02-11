package hr.pbf.digestdb.nr;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import hr.pbf.digestdb.cli.IApp;

public class App_5_Statistic implements IApp {

	@Override
	public void populateOption(Options o) {

	}

	private static final Logger log = LoggerFactory.getLogger(App_5_Statistic.class);

	@Override
	public void start(CommandLine appCli) {
		StopWatch s = new StopWatch();
		s.start();
		String folderPath = "/home/tag/nr_db/mass_small_store";
		File f = new File(folderPath);
		File[] listFiles = f.listFiles();
		long countUnique = 0;
		long countFiles = 0;
		long countuniqueSizeChar = 0;
		
		log.debug("start..");
		for (File file : listFiles) {
			if (Files.getFileExtension(file.getAbsolutePath()).equals("c")) {
				try {
					HashMap<String, List<Long>> db = App_4_CompressManyFilesSmall.decompress(file.getAbsolutePath());
					countUnique += db.size();
					countFiles++;
					Set<String> keySet = db.keySet();
					for (String peptideUnique : keySet) {
						countuniqueSizeChar += peptideUnique.length();
					}

					if (countFiles % 100 == 0) {
						System.out.print(countFiles + "\r");
					}
					
					
					insertIntoDatabase(db);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		s.stop();
		log.debug("Traje {}", DurationFormatUtils.formatDurationHMS(s.getTime()));
		log.debug("Count unique peptides: {}, Count files: {}", countUnique, countFiles);
		log.debug("Count unique peptide size: {}", countuniqueSizeChar);

	}

	private void insertIntoDatabase(HashMap<String, List<Long>> db) {
		
	}

}
