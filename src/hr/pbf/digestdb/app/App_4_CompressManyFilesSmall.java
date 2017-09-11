package hr.pbf.digestdb.app;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import hr.pbf.digestdb.cli.IApp;
import hr.pbf.digestdb.util.BioUtil;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

/**
 * Kompresira file gdje je slozeno po masama sa
 * {@link App_3_CreateMenyFilesFromCSV}.
 * 
 * @author tag
 *
 */
public class App_4_CompressManyFilesSmall implements IApp {
	private static final Logger log = LoggerFactory.getLogger(App_4_CompressManyFilesSmall.class);

	@Override
	public void populateOption(Options o) {

	}

	@Override
	public void start(CommandLine appCli) {
		StopWatch s = new StopWatch();
		s.start();
		String folderPath = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\small_store";
		folderPath = "/home/tag/nr_db/mass_small_store";
		File f = new File(folderPath);
		File[] listFiles = f.listFiles();

		int threads = 22;
		ExecutorService threadPool = Executors.newFixedThreadPool(threads);
		Semaphore semaphore = new Semaphore(threads);
		AtomicLong counter = new AtomicLong(0);

		for (File file : listFiles) {

			try {
				semaphore.acquire();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			threadPool.submit(new Callable<String>() {

				@Override
				public String call() throws Exception {
					try {
						TreeSet<PeptideMassIdRow> rows = new TreeSet<>();

						// 1. READ
						try (DataInputStream in = BioUtil.newDataInputStream(file.getAbsolutePath())) {
							while (in.available() > 0) {
								double mass = in.readDouble();
								long id = in.readLong();
								String peptide = in.readUTF();
								PeptideMassIdRow row = new PeptideMassIdRow(mass, id, peptide);
								rows.add(row);
							}
						}

						compress(rows, changeExtension(file, "c").getAbsolutePath());
						semaphore.release();
						// file.delete(); // NE SADA JOS
						long c = counter.getAndIncrement();
						if (c % 1000 == 0) {
							log.debug("Obratio filova {} od {} time:"
									+ DurationFormatUtils.formatDurationHMS(s.getTime()), c, listFiles.length);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					return "";
				}
			});
		}

		try {
			threadPool.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		s.stop();
		System.out.println("Finish " + DurationFormatUtils.formatDurationHMS(s.getTime()));
	}

	public static File changeExtension(File file, String extension) {
		String path = Files.getNameWithoutExtension(file.getAbsolutePath());
		return new File(file.getParentFile(), path + "." + extension);
	}

	public static void main(String[] args) {
		// App_4_CompressManyFilesSmall app = new App_4_CompressManyFilesSmall();
		// compressApp.start(null);
		File newFile = App_4_CompressManyFilesSmall.changeExtension(new File("/home/poss.db"), "c");
		System.out.println(newFile);
	}

	public static HashMap<String, List<Long>> decompress(String fileIn) throws IOException {
		HashMap<String, List<Long>> result = new HashMap<>();
		LZ4FastDecompressor fastDecompressor = LZ4Factory.fastestInstance().fastDecompressor();
		File file = new File(fileIn);
		// log.debug("Citam file velicine {} ", (file.length()));
		FileInputStream fim = new FileInputStream(file);
		// BufferedInputStream bfim = new BufferedInputStream(fim, 512);
		LZ4BlockInputStream lz = new LZ4BlockInputStream(fim, fastDecompressor);

		try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(IOUtils.toByteArray(lz)))) {
			while (in.available() > 0) {
				String peptide = in.readUTF();
				short howManyIds = in.readShort();
				List<Long> ids = new ArrayList<>(howManyIds);
				for (int i = 0; i < howManyIds; i++) {
					ids.add(in.readLong());
				}
				result.put(peptide, ids);
			}
		}

		return result;

	}

	public static void compress(TreeSet<PeptideMassIdRow> rows, String fileOut) throws IOException {
		//
		HashMap<String, List<PeptideMassIdRow>> map = new HashMap<>();

		// 2. CONVERT
		for (PeptideMassIdRow row : rows) {
			List<PeptideMassIdRow> listRows = map.get(row.peptide);
			if (listRows == null) {
				listRows = new ArrayList<>();
				map.put(row.peptide, listRows);
			}
			listRows.add(row);
		}

		// 3. COMPRESS
		LZ4Compressor compressor = LZ4Factory.nativeInstance().highCompressor();
		// ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		BufferedOutputStream buf = new BufferedOutputStream(
				new LZ4BlockOutputStream(new FileOutputStream(new File(fileOut)), 1 << 16, compressor));
		DataOutputStream out = new DataOutputStream(buf);

		for (Entry<String, List<PeptideMassIdRow>> entry : map.entrySet()) {
			String peptide = entry.getKey();
			out.writeUTF(peptide);
			List<PeptideMassIdRow> rowsByPeptide = entry.getValue();
			out.writeShort(rowsByPeptide.size());

			for (PeptideMassIdRow row : rowsByPeptide) {
				out.writeLong(row.id);
			}
		}
		buf.flush();

		// byte[] compressedByte = compressor.compress(byteOut.toByteArray());
		// FileOutputStream fOut = new FileOutputStream(new File(fileOut));
		// IOUtils.write(compressedByte, fOut);
		IOUtils.closeQuietly(buf);

		/*
		 * try (DataOutputStream out = BioUtil.newDataOutputStreamCompresed(fileOut)) {
		 * 
		 * // Zapis: UTF:peptide, INT:kolko ID ima: LONG:ID-evi.... Set<Entry<String,
		 * List<PeptideMassIdRow>>> entrySet = map.entrySet(); for (Entry<String,
		 * List<PeptideMassIdRow>> entry : entrySet) { String peptide = entry.getKey();
		 * out.writeUTF(peptide); List<PeptideMassIdRow> rowsByPeptide =
		 * entry.getValue(); // long[] arrayID = new long[rowsByPeptide.size()];
		 * 
		 * // int i = 0; out.writeShort(rowsByPeptide.size()); for (PeptideMassIdRow row
		 * : rowsByPeptide) { // arrayID[i] = row.id; out.writeLong(row.id); } // byte[]
		 * shuffle = BitShuffle.shuffle(arrayID); // out.write(shuffle); } out.flush();
		 * }
		 */
	}

	public static class PeptideMassIdRow implements Comparable<PeptideMassIdRow> {
		String peptide;
		double mass;
		long id;

		public PeptideMassIdRow(double mass, long id, String peptide) {
			this.mass = mass;
			this.id = id;
			this.peptide = peptide;
		}

		@Override
		public int compareTo(PeptideMassIdRow o) {
			return this.peptide.compareTo(o.peptide);
		}

	}
}
