package hr.pbf.digestdb.uniprot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.util.BioUtil;

/**
 * Create float-byte[] sorted map
 * 
 * @author tag
 *
 */
public class UniprotFormat3Creator {
	private static final Logger log = LoggerFactory.getLogger(UniprotFormat3Creator.class);

	private Format3Index index;

	private int counterUniqueMass = 0;

	private MyDataOutputStream dbOut;

	private String pathDb;

	private String pathIndex;

	private int howUniqueMass;

	public UniprotFormat3Creator(String pathDb, String pathIndex, int howUniqueMass) throws FileNotFoundException {
		this.pathDb = pathDb;
		this.pathIndex = pathIndex;
		this.howUniqueMass = howUniqueMass;
		index = new Format3Index(howUniqueMass);
		dbOut = BioUtil.newDataOutputStream(pathDb);

	}

	private float lastMass = -1;
	private long lastFilePosition = 0;

	public void putNext(float mass, String peptide, List<AccTax> accTaxList)
			throws UnsupportedEncodingException, IOException {
		if (mass < lastMass) {
			throw new RuntimeException("last mass is less then mass. lastMass: " + lastMass + " mass: " + mass);
		}
		// index.createNewEntry()
		if (lastMass == -1 || lastMass == mass) {
			saveToMemory(peptide, accTaxList);
		} else {
			// unique mass

			byte[] compressPeptidesTaxAccs = UniprotFormat3.compressPeptides(pepatidesOfOneMass);

			dbOut.writeInt(pepatidesOfOneMass.size());
			dbOut.write(compressPeptidesTaxAccs);
			index.newEntry(counterUniqueMass, lastMass, lastFilePosition);
			lastFilePosition += compressPeptidesTaxAccs.length + 4;
			counterUniqueMass++;
			pepatidesOfOneMass.clear();
		}
		lastMass = mass;
	}

	private HashMap<String, List<AccTax>> pepatidesOfOneMass = new HashMap<>();

	private void saveToMemory(String peptide, List<AccTax> accTaxList) {
		if (pepatidesOfOneMass.containsKey(peptide)) {
			throw new RuntimeException("Something wrong, contain peptide: " + peptide);
		}
		pepatidesOfOneMass.put(peptide, accTaxList);
	}

	public void finish() throws IOException {
		// Last entryes to write
		if (!pepatidesOfOneMass.isEmpty()) {
			byte[] compressPeptidesTaxAccs = UniprotFormat3.compressPeptides(pepatidesOfOneMass);
			dbOut.writeInt(pepatidesOfOneMass.size());
			dbOut.write(compressPeptidesTaxAccs);
			index.newEntry(counterUniqueMass, lastMass, lastFilePosition);
			lastFilePosition += compressPeptidesTaxAccs.length + 4;
			pepatidesOfOneMass.clear();
		}
		

		

		if (dbOut != null) {
			dbOut.close();
		}
		index.setLastPostitionFileLength(new File(pathDb).length());
		writeIndexToDisk();

	}

	public void writeIndexToDisk() throws IOException {
		FileOutputStream outputStream = new FileOutputStream(new File(pathIndex));
		MyDataOutputStream out = new MyDataOutputStream(outputStream);
		// FastOutput o = new FastOutput(outputStream);
		index.writeToDataOutput(out);
		outputStream.close();
//		log.debug("index saved to: " + pathIndex);
//		log.debug("index size " + index.length());
	}

}
