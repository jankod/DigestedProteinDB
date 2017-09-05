package hr.pbf.digestdb.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.app.CreateSmallMenyFilesDBapp;

public class MainCli {
	private static final Logger log = LoggerFactory.getLogger(MainCli.class);

	
	public static void main(String[] args) throws ParseException {

		HashMap<String, IApp> apps = new HashMap<>();
		apps.put("small", new CreateSmallMenyFilesDBapp());

		// String[] demoArgs = { "-a", "small" };
		String[] demoArgs = { "-a" };

		demoArgs = args;
		log.debug("Upisao " + Arrays.toString(demoArgs));

		Options options = new Options();
		options.addOption(Option.builder("a").longOpt("app").hasArg()
				.desc("Start one of apps: " + apps.keySet().toString()).build());

		options.addOption(Option.builder("sa").longOpt("showApps").hasArg(false).desc("Show existing apps").build());
		options.addOption(Option.builder("h").argName("h").longOpt("help").build());

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cli = parser.parse(options, demoArgs);

			if (cli.hasOption("showApps")) {
				Set<Entry<String, IApp>> entrySet = apps.entrySet();
				for (Entry<String, IApp> entry : entrySet) {
					System.out.println("App '" + entry.getKey() + "'");
				}
				return;
			}

			if (cli.hasOption("help")) {
				printHelp(options);
				return;
			}

			if (cli.hasOption("a")) {
				String appName = cli.getOptionValue("a");
				log.debug("start app " + appName);
				IApp app = apps.get(appName);
				startApp(args, options, parser, app);
				return;
			}

			printHelp(options, "Not find app name!");

		} catch (UnrecognizedOptionException e) {
			System.out.println("Error " + e.getMessage());
			printHelp(options);
		} catch (Throwable t) {
			System.out.println("Error " + t.getMessage());
			t.printStackTrace();
			printHelp(options);
		}

	}

	private static void startApp(String[] args, Options options, CommandLineParser parser, IApp app)
			throws ParseException {
		List<String> argsList =  new ArrayList<>(Arrays.asList(args));
		if (argsList.size() >= 1)
			argsList.remove(0);
		if (argsList.size() >= 1)
			argsList.remove(0);
		String[] newArgs = argsList.toArray(new String[argsList.size()]);
		Options appOptions = new Options();
		try {
			app.populateOption(appOptions);
			CommandLine appCli = parser.parse(appOptions, newArgs);
			app.start(appCli);
		} catch (MissingOptionException e) {
			System.out.println("Error " + e.getMessage());
			printHelp(appOptions);
			e.printStackTrace();
		}

	}

	private static void printHelp(Options options) {
		printHelp(options, null);
	}

	private static void printHelp(Options options, String descAnnother) {
		HelpFormatter formatter = new HelpFormatter();
		if (descAnnother != null) {
			System.out.println(descAnnother);
		}
		formatter.printHelp("Startaj jedno od ovih aplikacija", "Komande su:", options, "");

	}

	private static void addApp(IApp demoApp) {
		Options o = new Options();
		demoApp.populateOption(o);
	}

}
