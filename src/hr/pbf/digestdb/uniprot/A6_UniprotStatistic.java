package hr.pbf.digestdb.uniprot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;

import hr.pbf.digestdb.util.BioUtil;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;

public class A6_UniprotStatistic {
	private static final Logger log = LoggerFactory.getLogger(A6_UniprotStatistic.class);

	public static void main(String[] args) throws IOException {

		countUniquFloatMassInCSV();

	}

	private static void countUniquFloatMassInCSV() throws IOException {
		String path = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_test.csv";
		
		if(SystemUtils.IS_OS_LINUX) {
			path = "/home/users/tag/uniprot/trembl.csv";  
					
		}
		
		BufferedReader in = BioUtil.newFileReader(path);
		HashMap<Float, Long> map = new HashMap<>();
		String line;
		float lastMass = -1;
		long sampleLong = 1;
		long countUniqueMass = 0;
		while ((line = in.readLine()) != null) {
			String[] split = StringUtils.split(line, "\t");
			float mass = Float.parseFloat(split[0]);
			if (mass != lastMass) {
				map.put(mass, sampleLong++);
				countUniqueMass++;
			}
			lastMass = mass;
		}
		log.debug("count unique mass "+ countUniqueMass );
		log.debug("count float mass  "+ map.size());
		FileOutputStream out = new FileOutputStream(path+".serialize_map"); // 234.016
		SerializationUtils.serialize(map, out);
		out.close();
		in.close();
	}
}
