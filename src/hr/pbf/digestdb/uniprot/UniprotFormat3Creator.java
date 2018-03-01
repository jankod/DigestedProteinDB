package hr.pbf.digestdb.uniprot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import hr.pbf.digestdb.uniprot.UniprotFormat3.Format3Index;
import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
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

	public UniprotFormat3Creator(String pathDb, String pathIndex, int howUniqueMass) throws FileNotFoundException {
		index = new Format3Index(howUniqueMass);
		dbOut = BioUtil.newDataOutputStream(pathDb);

	}

	private float lastMass = -1;
	private long lastFilePosition = 0;

	public void putNext(float mass, String peptide, ArrayList<AccTax> accTaxList)
			throws UnsupportedEncodingException, IOException {
		if (mass <= lastMass) {
			throw new RuntimeException("last mass is less then mass. lastMass: " + lastMass + " mass: " + mass);
		}
		// index.createNewEntry()
		if (lastMass == -1 || lastMass == mass) {
			saveToMemory(peptide, accTaxList);
		} else {
			// unique mass
			counterUniqueMass++;

			byte[] compress = UniprotFormat3.compressFloatValue(pepatidesOfOneMass);
			
			dbOut.writeInt(pepatidesOfOneMass.size());
			dbOut.write(compress);
			
//			log.debug("put " + lastMass + " " + compress.length);
			// sink.put(lastMass, compress);
			// index.put(lastMass, lastIndexPos);
			// index.putNewValue(positionInArray, mass, positionInFile);

			// db.write(BiteUtil.toByte(countUniquePeptide));
			// db.write(compress);
			// lastIndexPos += compress.length + 4;
			pepatidesOfOneMass.clear();
		}
		lastMass = mass;
	}

	private HashMap<String, List<AccTax>> pepatidesOfOneMass = new HashMap<>();

	private void saveToMemory(String peptide, ArrayList<AccTax> accTaxList) {
		if (pepatidesOfOneMass.containsKey(peptide)) {
			throw new RuntimeException("Something wrong, contain peptide: " + peptide);
		}
		pepatidesOfOneMass.put(peptide, accTaxList);
	}

	// private void saveToMemory(String peptide, ArrayList<AccTax> accTaxList) {

	// valueCache.append(UniprotFormat3.formatValue(peptide,
	// accTaxList)).append("\n");
	// }

	public void finish() {
		if (dbOut != null) {
			try {
				dbOut.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

}
