package hr.pbf.digestdb.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import ch.qos.logback.core.pattern.ConverterUtil;
import hr.pbf.digestdb.cli.IApp;

public class FindPeptidesByMassApp implements IApp{

	
	public static void main(String[] args) throws IOException {
		String pathDb = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\1086.4-1086.7.db.compress";
		App_4_CompressManyFilesSmall small = new App_4_CompressManyFilesSmall();
		StopWatch s = new StopWatch();
		s.start();
		HashMap<String, List<Long>> res = small.decompress(pathDb);
		
		System.out.println(DurationFormatUtils.formatDurationHMS(s.getTime()));
		
		System.out.println(res.size());
	}
	
	@Override
	public void populateOption(Options o) {
		o.addOption("m", "mass", true, "Koju masu da trazi");
	}

	@Override
	public void start(CommandLine appCli) {
		if(appCli.hasOption('m')) {
			String massString = appCli.getOptionValue('m');
			try {
				double mass = Double.parseDouble(massString);
				
				String path = "/home/tag/nr_db/mass_small_store";
				
			} catch (NumberFormatException e) {
				System.out.println(massString + " is not mass");
				e.printStackTrace();
			}
		}
	}

}
