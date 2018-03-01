package hr.pbf.digestdb.uniprot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.iq80.snappy.SnappyFramedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;
import org.xerial.snappy.SnappyInputStream;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxMass;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MassRangeMap;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class UniprotSearchFormat2 {

	enum Type {
		format2, format2snappy, format1,

	}


	public static void main(String[] args) throws IOException {
		// readF2();
		searchLinux(args);
	}



	public static void searchLinux(String[] args) throws IOException {
		String dir = "/home/users/tag/uniprot/trembl_format2s";

		{ // TESTING
//			dir = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\ne radi\\format2";
//			String[] myArgs = { "1728:1728.1", "debug" };
//			args = myArgs;
		}

		if (!new File(dir).isDirectory()) {
			throw new FileNotFoundException("Not a dir: " + dir);
		}
		boolean debug = false;
		if (args.length > 1 && args[1].equals("debug")) {
			debug = true;
			System.out.println("DEBUG MODE");
		}

		String param = args[0];
		if (param.contains(":")) {
			String[] split = param.split(":");
			float from = Float.parseFloat(split[0]);
			float to = Float.parseFloat(split[1]);
			System.out.println("from:to " + from + ":" + to);
			StopWatch s = new StopWatch();
			s.start();
			MassRangeMap map = new MassRangeMap(0.3f, 500, 6000);

			RangeMap<Float, String> massFileNames = map.getFileNames(from, to);
			Collection<String> fileNames = massFileNames.asMapOfRanges().values();
			int countTotalPeptides = 0;
			int countFiles = 0;
			for (String fileName : fileNames) {

				File f = new File(dir, fileName + ".f2s");
				if (!f.exists()) {
					if (debug)
						System.out.println("Not find: " + f.getAbsolutePath());
					continue;
				}

				countFiles++;

				byte[] bytes = UniprotUtil.toByteArrayFast(f);
				bytes = Snappy.uncompress(bytes);
				Map<String, List<PeptideAccTaxMass>> data = UniprotUtil.fromFormat2(bytes, true, from, to);

				// findAccession("A0A1J4YX49", data);
				Set<Entry<String, List<PeptideAccTaxMass>>> entrySet = data.entrySet();
				s.suspend();

				for (Entry<String, List<PeptideAccTaxMass>> entry : entrySet) {
					String peptide = entry.getKey();
					List<PeptideAccTaxMass> value = entry.getValue();
					countTotalPeptides += value.size();
					String accTax = formatAccessionsAndTax(value);
					// String accessions = getAllAccessions(value);
					// String taxs = getAllTax(value);

					System.out.println(value.get(0).getMass() + "\t" + peptide + "\t" + accTax);
				}
				s.resume();

			}
			if (debug) {
				System.out.println("Duration: " + DurationFormatUtils.formatDurationHMS(s.getTime()));
				System.out.println("Found peptides: " + countTotalPeptides);
				System.out.println("Count files: " + countFiles);
			}

		}

	}

	public final static String formatAccessionsAndTax(List<PeptideAccTaxMass> value) {
		StringBuilder b = new StringBuilder(value.size() * 15);

//		b.append("[");
		boolean first = true;
		for (PeptideAccTaxMass p : value) {
			if (!first) {
				b.append(",");
			}
			b.append(p.getAcc() + ":" + p.getTax());
			first = false;
		}
//		b.append("]");
		return b.toString();
	}

	private final static String getAllAccessions(final List<PeptideAccTaxMass> value) {
		final StringBuilder b = new StringBuilder(value.size() * 7);
		b.append("[");
		boolean first = true;
		for (PeptideAccTaxMass p : value) {
			if (!first) {
				b.append(',');
			}
			b.append(p.getAcc());
			first = false;

		}
		b.append("]");
		return b.toString();
	}

	private final static String getAllTax(final List<PeptideAccTaxMass> value) {
		final StringBuilder b = new StringBuilder(value.size() * 7);
		b.append("[");
		boolean first = true;
		for (PeptideAccTaxMass p : value) {
			if (!first) {
				b.append(',');
			}
			b.append(p.getTax());
			first = false;
		}

		b.append("]");
		return b.toString();
	}

}
