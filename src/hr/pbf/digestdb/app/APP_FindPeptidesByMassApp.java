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

public class APP_FindPeptidesByMassApp implements IApp{

	
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
		o.addOption("f", "mass", true, "Koju masu od da trazi");
		o.addOption("t", "mass2", true, "Koju masu do da trazi");
	}

	@Override
	public void start(CommandLine appCli) {
		if(appCli.hasOption("f")) {
			try {
				double mass1 = Double.parseDouble(appCli.getOptionValue("f"));
				double mass2 = Double.parseDouble(appCli.getOptionValue("t"));
				
				String path = "/home/tag/nr_db/mass_small_store";
				
				
				
				
			} catch (NumberFormatException e) {
				System.out.println(appCli.getOptionValue("f") + " is not mass");
				e.printStackTrace();
			}
		}
	}

}
