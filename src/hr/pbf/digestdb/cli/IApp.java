package hr.pbf.digestdb.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

/**
 * Konstruktor je najbolje prazan ostaviti jer se poziva uvjek bez obzira da li
 * se compressApp klasa koristi ili ne.
 * 
 * @author tag
 *
 */
public interface IApp {

	public void populateOption(Options o);

	public void start(CommandLine appCli);

	
	
}
