package hr.pbf.digestdb.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.WriteBatch;

import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;

public class MassCSV {

	private String path;

	public MassCSV(String path) {
		this.path = path;
	}

	
	private boolean stop = false;
	public void startIterate(CallbackMass callbackMass) throws FileNotFoundException, IOException {

		MutableString line = new MutableString(220);
		try (FastBufferedReader reader = new FastBufferedReader(new FileReader(path))) {
			while ((reader.readLine(line)) != null) {
				if(stop) {
					callbackMass.finish();
					break;
				}
				
				String[] split = StringUtils.split(line.toString(), '\t');

				double mass = Double.parseDouble(split[0]);
				String peptide = split[1].trim();
				String accVersion = split[2].trim();
				callbackMass.row(mass, accVersion, peptide);

			}
		}
		callbackMass.finish();
	}

	public void stop() {
		stop = true;
	}

}
