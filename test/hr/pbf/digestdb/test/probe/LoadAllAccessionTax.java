package hr.pbf.digestdb.test.probe;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.esotericsoftware.kryo.io.UnsafeInput;
import com.univocity.parsers.common.Context;
import com.univocity.parsers.common.processor.core.Processor;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import hr.pbf.digestdb.util.BioUtil;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.h2.mvstore.*;

public class LoadAllAccessionTax {

	private static final class ProcessorImplementation implements Processor<Context> {

		int count = 0;
		private MVStore s;
		// TObjectIntHashMap<String> map = new TObjectIntHashMap<>(463_523_590/2);
		private MVMap<String, Integer> map;

		public ProcessorImplementation(String storeFileDb) {
			s = new MVStore.Builder().cacheSize(512).fileName(storeFileDb).compress().open();
			map = s.openMap("acc_taxid");
			
		}

		public int getCount() {
			return count;
		}

		@Override
		public void processStarted(Context context) {

		}

		@Override
		public void rowProcessed(String[] row, Context context) {
			// System.out.println(Arrays.toString(row));
			count++;
			String accVersion = row[1];
			int taxId = Integer.parseInt(row[2]);
			// map.put(accVersion, taxId);
			map.put(accVersion, taxId);
		}

		@Override
		public void processEnded(Context context) {
			System.out.println("Finishcompact...");
			s.commit();
			s.compactMoveChunks();
			// s.compactRewriteFully();
			System.out.println("Finish compact");
			s.close();

		}
	}

	public static void main2(String[] args) throws IOException {

		UnsafeInput in = new UnsafeInput(new FileInputStream(new File("pero")));
		BufferedReader bin = new BufferedReader(new InputStreamReader(in, Charset.forName("ASCII")));
		TObjectIntHashMap<String> map = new TObjectIntHashMap<>(463_523_590);
		// TIntIntHashMap map = new TIntIntHashMap();

		String line = bin.readLine();
		while (line != null) {

			String[] split = BioUtil.fastSplit(line, '\t');
			String accession = split[1];
			int taxid = Integer.parseInt(split[2]);
			// map.put(accession, taxid);

			line = bin.readLine();
		}

	}

	static String storeFileDb = "F:\\tmp\\h2store.db";

	public static void main1(String[] args) {
		StopWatch stop = new StopWatch();
		stop.start();
		File accesionCSVFile = new File(
				"C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\900_000prot.accession2taxid.txt");
		if (SystemUtils.IS_OS_LINUX) {
			storeFileDb = "/home/users/tag/nr_db/mvstore.db";
			accesionCSVFile = new File("/home/users/tag/nr_db/prot.accession2taxid");
		}
		TsvParserSettings s = new TsvParserSettings();
		s.setHeaderExtractionEnabled(true);
		ProcessorImplementation proc = new ProcessorImplementation(storeFileDb);
		s.setProcessor(proc);
		TsvParser parser = new TsvParser(s);

		parser.parse(accesionCSVFile);
		System.out.println("finish");
		System.out.println(proc.getCount());
		// System.out.println("Map size " + proc.getMap().size());
		System.out.println(DurationFormatUtils.formatDurationHMS(stop.getTime()));
	}

	public static void main(String[] args) throws FileNotFoundException {
		storeFileDb = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\mvstore_accession_taxid.db";
		if (SystemUtils.IS_OS_LINUX) {
			storeFileDb = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\mvstore.db";
		}
		
		if(!Paths.get(storeFileDb).toFile().exists()) {
			throw new FileNotFoundException(storeFileDb);
		}
		
		MVStore s = new MVStore.Builder().cacheSize(512).fileName(storeFileDb).compress().open();
		MVMap<Object, Object> map = s.openMap("acc_taxid");
		
		
		
		System.out.println("Size: "+ map.size());
		System.out.println("Accession APZ74649.1 : "+ map.get("APZ74649.1"));
		Object firstKey = map.firstKey();
		System.out.println(firstKey);
		s.close();
	}
}
