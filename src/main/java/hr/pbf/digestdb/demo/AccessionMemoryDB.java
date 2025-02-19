package hr.pbf.digestdb.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;

import hr.pbf.digestdb.util.BioUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.SerializationUtils;

import gnu.trove.map.hash.TObjectLongHashMap;

public class AccessionMemoryDB implements Serializable {

	private static final long serialVersionUID = 1L;

	// private HashMap<String, Long> map;
	TObjectLongHashMap<String> map;

	private static final String pathSerialize = "/home/tag/AccessionMemoryDB.ser";

	public static AccessionMemoryDB tryDeserializeSelf() throws FileNotFoundException {
		File fileSerialize = new File(pathSerialize);
		if (fileSerialize.exists()) {
			System.out.println("Pouksavam deserializirati ");
			FileInputStream in = new FileInputStream(fileSerialize);
			AccessionMemoryDB self = SerializationUtils.deserialize(in);
			IOUtils.closeQuietly(in);
			System.out.println("Uspio!!!");
			return self;
		}
		return null;

	}

	public AccessionMemoryDB(String path) throws IOException {
		File fileSerialize = new File(pathSerialize);

		FileInputStream in = new FileInputStream(new File(path));
		LineIterator it = IOUtils.lineIterator(in, StandardCharsets.US_ASCII);
		// map = new HashMap<>(lines.size());
		int maxLines = 413_086_738;
		map = new TObjectLongHashMap<>(maxLines);
		int c = 0;
		int petStoTisuca = 900_000;
		while (it.hasNext()) {
			c++;

			if (c % petStoTisuca == 0) {
				BioUtil.printMemoryUsage("Sada ");
				System.out.println("Citam " + NumberFormat.getInstance().format(c) + " linija... "
						+ NumberFormat.getInstance().format(((double) c) / (double) maxLines * 100D) + " %");
			}
			String line = it.next();
			int index = line.indexOf(',');
			// String[] split = StringUtils.split(line, ',');
			String accession = line.substring(0, index);
			Long acc = Long.valueOf(line.substring(index + 1));
			map.put(accession, acc);

		}

		FileOutputStream outputStream = new FileOutputStream(fileSerialize);
		SerializationUtils.serialize(this, outputStream);
		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(outputStream);
		System.out.println("Serializiorao bazu na: " + fileSerialize);
	}

	public Long getAccessionNum(String accession) {
		return map.get(accession);
	}
}
