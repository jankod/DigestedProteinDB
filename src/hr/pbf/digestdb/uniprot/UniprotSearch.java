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

public class UniprotSearch {

	enum Type {
		format2, format2snappy, format1,

	}

	private static final Logger log = LoggerFactory.getLogger(UniprotSearch.class);

	public static void main111(String[] args) throws IOException {
		String p = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl\\1831.8.f1";

		// "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl\\1831.8.f2s";

		byte[] f1 = UniprotUtil.toByteArrayFast(p);
		ArrayList<PeptideAccTax> peptides = UniprotUtil.fromFormat1(f1);
		Map<String, List<PeptideAccTax>> group = UniprotUtil.groupByPeptide(peptides);
		byte[] f2 = UniprotUtil.toFormat2(group);

		f2 = UniprotUtil.compress(f2);
		Path path = Paths.get(p + ".new");

		Files.write(path, f2);
		log.debug("saveo novi");

	}

	public static void main(String[] args) throws IOException {
		// readF2();
		searchLinux(args);
	}

	private static void readF2() throws IOException {
		String p = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\1831.8.f2s";
		p = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl\\demo-format2\\500.0.f2s";
		p = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl\\linux ponovo\\2182.9.f2s";
		byte[] bytes = UniprotUtil.toByteArrayFast(p);
		// bytes = Snappy.uncompress(bytes);
		bytes = UniprotUtil.uncompress(bytes);
		int countTotalPeptides = 0;

		Map<String, List<PeptideAccTaxMass>> data = UniprotUtil.fromFormat2(bytes, true);

		Set<Entry<String, List<PeptideAccTaxMass>>> entrySet = data.entrySet();
		for (Entry<String, List<PeptideAccTaxMass>> entry : entrySet) {
			List<PeptideAccTaxMass> value = entry.getValue();
			for (PeptideAccTaxMass pep : value) {
				float mass = (float) BioUtil.calculateMassWidthH2O(pep.getPeptide());
				System.out.println(mass + "\t " + pep.getPeptide() + "\t" + pep.getAcc() + "\t " + pep.getTax());
			}
			countTotalPeptides = value.size();
		}
		log.debug("Founded peptides " + countTotalPeptides);
	}

	public static void searchLinux(String[] args) throws IOException {
		String dir = "/home/users/tag/uniprot/trembl_format2s";

		if (!new File(dir).isDirectory()) {
			throw new FileNotFoundException("Not a dir: " + dir);
		}

		String param = args[0];
		if (param.contains(":")) {
			String[] split = param.split(":");
			float from = Float.parseFloat(split[0]);
			float to = Float.parseFloat(split[1]);
			log.debug("from:to {}:{}", from, to);
			StopWatch s = new StopWatch();
			s.start();
			MassRangeMap map = new MassRangeMap(0.3f, 500, 6000);

			RangeMap<Float, String> names = map.getFileNames(from, to);
			Collection<String> values = names.asMapOfRanges().values();
			int countTotalPeptides = 0;
			int countFiles = 0;
			for (String fileName : values) {

				File f = new File(dir, fileName + ".f2s");
				if (!f.exists()) {
//					log.debug("Not find: " + f.getAbsolutePath());
					continue;
				}

				countFiles++;

				byte[] bytes = UniprotUtil.toByteArrayFast(f);
				bytes = Snappy.uncompress(bytes);
				Map<String, List<PeptideAccTaxMass>> data = UniprotUtil.fromFormat2(bytes, true, from, to);
				Set<Entry<String, List<PeptideAccTaxMass>>> entrySet = data.entrySet();
				s.suspend();

				for (Entry<String, List<PeptideAccTaxMass>> entry : entrySet) {
					List<PeptideAccTaxMass> value = entry.getValue();
					String peptide = entry.getKey();

					String accessions = getAllAccessions(value);
					String taxs = getAllTax(value);
					System.out.println(value.get(0).getMass() + "\t" + peptide + "\t" + accessions + "\t" + taxs);
				}
				s.resume();

			}
			log.debug("Duration: " + DurationFormatUtils.formatDurationHMS(s.getTime()));
			log.debug("Found peptides: " + countTotalPeptides);
			log.debug("Count files: " + countFiles);

		}

	}

	private final static String getAllAccessions(final List<PeptideAccTaxMass> value) {
		final StringBuilder b = new StringBuilder(value.size() * 10);
		b.append("[");
		for (PeptideAccTaxMass p : value) {
			b.append(p.getAcc()).append(",");
		}
		b.deleteCharAt(b.length() - 1);
		b.append("]");
		return b.toString();
	}

	private final static String getAllTax(final List<PeptideAccTaxMass> value) {
		final StringBuilder b = new StringBuilder(value.size() * 7);
		b.append("[");
		for (PeptideAccTaxMass p : value) {
			b.append(p.getTax()).append(",");
		}
		b.deleteCharAt(b.length() - 1);
		b.append("]");
		return b.toString();
	}

	public static byte[] decompress(byte[] contents) throws IOException {
		SnappyFramedInputStream is = new SnappyFramedInputStream(new FastByteArrayInputStream(contents), true);
		FastByteArrayOutputStream os = new FastByteArrayOutputStream(Snappy.uncompressedLength(contents));
		IOUtils.copy(is, os);
		os.close();
		return os.array;
	}

	private static void testProba1() throws IOException {
		String dir = "F:\\Downloads\\uniprot\\format2";

		String dirSnappy = "F:\\Downloads\\uniprot\\format2snappy";

		float m1 = 1955.5f;
		float m2 = m1 + 98.3f;

		MassRangeMap map = new MassRangeMap(0.3f, 500, 6000);
		RangeMap<Float, String> names = map.getFileNames(m1, m2);
		Collection<String> values = names.asMapOfRanges().values();

		doIt(dirSnappy, values, Type.format2snappy);
		doIt(dir, values, Type.format2);
		doIt("F:\\Downloads\\uniprot\\uniprot_sprot.dat_format1", values, Type.format1);
	}

	private static void doIt(String dir, Collection<String> values, Type type) throws IOException {

		StopWatch s = new StopWatch();
		s.start();
		int c = 0;
		for (String fileName : values) {

			if (type == Type.format1) {
				File f = new File(dir, fileName + ".format1");
				if (!f.exists())
					continue;

				byte[] bytes = UniprotUtil.toByteArrayFast(f);

				List<PeptideAccTax> result = UniprotUtil.fromFormat1(bytes);
				c += result.size();
			}
			if (type == Type.format2) {
				File f = new File(dir, fileName + ".format2");
				if (!f.exists())
					continue;
				byte[] bytes = UniprotUtil.toByteArrayFast(f);

				Map<String, List<PeptideAccTaxMass>> data = UniprotUtil.fromFormat2(bytes, true);
				c += data.size();
			}
			if (type == Type.format2snappy) {
				File f = new File(dir, fileName + ".format2");
				if (!f.exists())
					continue;
				byte[] bytes = UniprotUtil.toByteArrayFast(f);
				bytes = Snappy.uncompress(bytes);
				Map<String, List<PeptideAccTaxMass>> data = UniprotUtil.fromFormat2(bytes, true);
				c += data.size();
			}

		}
		s.stop();
		String time = DurationFormatUtils.formatDurationHMS(s.getTime());
		log.debug("Type: {}  {}", type, time);
		log.debug("c {}", c);
	}
}
