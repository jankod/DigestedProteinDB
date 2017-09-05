package hr.pbf.digestdb.cli;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;

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
		DataInputStream in = BioUtil.newDataInputStream(file.getAbsolutePath());
		while (in.available() > 0) {
		}

	}

}
