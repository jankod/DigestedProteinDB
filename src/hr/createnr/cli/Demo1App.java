package hr.createnr.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Demo1App implements IApp {

	private Options options;

	public Demo1App() {
	}

	@Override
	public void populateOption(Options options) {
		this.options = options;

		options.addOption(Option.builder("c").hasArg().required().build());
	}

	@Override
	public void start(CommandLine cli) {
		if (cli.hasOption('c')) {
			String optionC = cli.getOptionValue('c');
			System.out.println("Start demo1 app: c=" + optionC);

		}

	}
}
