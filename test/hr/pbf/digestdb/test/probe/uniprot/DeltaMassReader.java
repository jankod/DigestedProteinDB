package hr.pbf.digestdb.test.probe.uniprot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MassRangeMap;

public class DeltaMassReader {
	private final static double DELTA = 0.3;
	final static int fromMass = 500;
	final static int toMass = 6000;
	static final MassRangeMap massPartsMap = new MassRangeMap(DELTA, fromMass, toMass);
	// private static HashMap<String, DataInputStream> massStreamMap = new
	// HashMap<>();

	private static String mainfolder = "F:\\Downloads\\uniprot";
	static String deltaDbPath = mainfolder + "/delta-db/";
	private static final Logger log = LoggerFactory.getLogger(DeltaMassReader.class);

	public static void main(String[] args) throws IOException {

		read(615.2F, 615.3f);
	}

	private static void read(float from, float to) throws IOException {
		String fileName = massPartsMap.getFileName(from);
		DataInputStream in = BioUtil.newDataInputStream(deltaDbPath + fileName + ".db");
	
		int c = 0;
		while (in.available() != 0) {
			String peptide = in.readUTF();
			int tax = in.readInt();
			int howAcc = in.readInt();
			ArrayList<String> accessions = new ArrayList<>(howAcc);
			for (int i = 0; i < howAcc; i++) {
				accessions.add(in.readUTF());
			}
			log.debug("{} {} {}", peptide, tax, accessions);
			c++;
		}
		log.debug("stavki " + c);
		log.debug(fileName);
		in.close();
	}

}
