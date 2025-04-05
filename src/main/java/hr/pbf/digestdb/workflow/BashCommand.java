package hr.pbf.digestdb.workflow;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Example usage:
 * sort -t',' -k1n peptide_mass.csv -o peptide_mass_sorted_console.csv
 */
@Data
@Slf4j
public class BashCommand {

	private String cmd;
	private File dir;

	public Integer start() throws Exception {
		ProcessBuilder builder = new ProcessBuilder();
		builder.command("bash", "-c", cmd);
		if(dir != null) {
			builder.directory(dir);
		}
		try {
			Process process = builder.start();
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			StringBuilder errorOutput = new StringBuilder();
			String line;
			while((line = errorReader.readLine()) != null) {
				errorOutput.append(line).append(System.lineSeparator());
			}
			int exitCode = process.waitFor();
			if(exitCode != 0) {
				log.error("Command '{}' failed with exit code {}: {}", cmd, exitCode, errorOutput);
				throw new RuntimeException("Command failed with exit code " + exitCode + ": " + errorOutput);
			}
			return exitCode;
		} catch(Exception e) {
			log.error("Error executing command: {}", cmd, e);
			throw new RuntimeException("Error executing command: " + cmd, e);
		}
	}

}
