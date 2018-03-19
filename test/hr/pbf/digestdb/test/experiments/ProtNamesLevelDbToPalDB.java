package hr.pbf.digestdb.test.experiments;

import java.io.BufferedInputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.io.KryoDataInput;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;
import com.linkedin.paldb.api.StoreWriter;

import hr.pbf.digestdb.MyUtil;
import hr.pbf.digestdb.uniprot.MassIndex;
import hr.pbf.digestdb.uniprot.UniprotUtil;
import hr.pbf.digestdb.util.LevelDButil;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;

public class ProtNamesLevelDbToPalDB {

	private static final Logger log = LoggerFactory.getLogger(ProtNamesLevelDbToPalDB.class);

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		// convertLevelDbToPalDB();

		StoreReader r = PalDB.createReader(new File(pathDb + ".paldb"));
		Iterable<Entry<byte[], byte[]>> it = r.iterable();
		int c = 0;
		for (Entry<byte[], byte[]> entry : it) {
			//System.out.println(new String(entry.getKey()) + " " + new String(entry.getValue()));
			System.out.println(entry);
			if (c++ > 100) {
				break;
			}
		}
		r.close();

	}

	static String pathDb = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_prot_names.leveldb";

	private static void convertLevelDbToPalDB() throws IOException {
		DB db = LevelDButil.open(pathDb, new Options());
		DBIterator it = db.iterator();
		it.seekToFirst();

		StoreWriter writer = PalDB.createWriter(new File(pathDb + ".paldb"));
		writer.getConfiguration().set("compression.enabled", "true");

		while (it.hasNext()) {
			Map.Entry<byte[], byte[]> entry = (Map.Entry<byte[], byte[]>) it.next();
			// String acc = new String(entry.getKey(), StandardCharsets.US_ASCII);
			// String protName = new String(entry.getValue(), StandardCharsets.US_ASCII);
			writer.put(entry.getKey(), entry.getValue());
		}
		writer.close();

		db.close();
		log.debug("FINISH");
	}

}
