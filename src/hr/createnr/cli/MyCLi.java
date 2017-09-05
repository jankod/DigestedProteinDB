package hr.createnr.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Prvi parametar main args je ime applikacije
 * 
 * @author tag
 *
 */
public class MyCLi {

	public MyCLi(String[] args) throws ParseException {

		HashMap<String, IApp> apps = new HashMap<>();
		apps.put("demo1", new Demo1App());
		apps.put("demo2", new Demo2App());

		String appName = args[0];
		Options options = new Options();

		IApp currentApp = null;
		currentApp.populateOption(options);
		
		
		
		CommandLineParser parser = new DefaultParser();

		List<String> listArgs = Arrays.asList(args);
		listArgs.remove(0);
		CommandLine cli = parser.parse(options, listArgs.toArray(new String[listArgs.size()]));

	}

	private void printHelp() {
		System.out.println("Prvo parametar je ime aplikacije.");

	}
}
