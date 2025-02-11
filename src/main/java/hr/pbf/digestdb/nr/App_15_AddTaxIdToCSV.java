package hr.pbf.digestdb.nr;

//import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
//import static org.fusesource.leveldbjni.JniDBFactory.*;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.SystemUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.BiteUtil;
import hr.pbf.digestdb.util.CallbackMass;
import hr.pbf.digestdb.util.MassCSV;
import hr.pbf.digestdb.util.LevelDButil;
import hr.pbf.digestdb.util.TimeScheduler;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;

public class App_15_AddTaxIdToCSV {
	private static final Logger log = LoggerFactory.getLogger(App_15_AddTaxIdToCSV.class);

	private static long c = 0;
	private final static ArrayList<Row> sameMassRow = new ArrayList<>();
	private static float lastMass = 0;

	private static DB db;

	private static void writeLevelDb() throws IOException {
		float mass = sameMassRow.get(0).getMass();
		// StringBuilder b = new StringBuilder(sameMassRow.size() * 60);
		// Output o = new Output();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream o = new DataOutputStream(out);
		o.writeInt(sameMassRow.size());
		for (Row r : sameMassRow) {
			o.writeUTF(r.getPeptide());
		}
		for (Row r : sameMassRow) {
			o.writeUTF(r.getAcc());
		}
		for (Row r : sameMassRow) {
			o.writeInt(r.getTaxId());
		}

		db.put(BiteUtil.toBytes(mass), out.toByteArray());
		o.close();
	}
	public static void mainOld(String[] args) throws IOException {
		Options options = new Options();
		options.createIfMissing(true);

		options.cacheSize(100 * 1048576 * 10); // 100MB cache
		options.compressionType(CompressionType.SNAPPY);
		options.verifyChecksums(false);
		options.paranoidChecks(false);
		String dirResult = "c:/tmp/";
		String leveldbPath = dirResult + "nr_mass_leveldb";
		System.out.println("Level DB: " + leveldbPath);
		db = LevelDButil.open(leveldbPath, options);
		DBIterator it = db.iterator();
		it.seek(BiteUtil.toBytes(446.22375F));
		while(it.hasNext()) {
			Entry<byte[], byte[]> t = it.next();
			float key = BiteUtil.toFloat(t.getKey());
			byte[] value = t.getValue();
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(value));
			int length = in.readInt();
			System.out.println("Key "+ key);
			for (int i = 0; i < value.length; i++) {
				
			}
			
		}
		
		db.close();
		
		
	}
	
	public static void main(String[] args) throws SQLException, IOException {
		TimeScheduler.runEvery1Minutes(new Runnable() {

			@Override
			public void run() {
				log.debug("Dosao do " + NumberFormat.getIntegerInstance().format(c));
			}
		});
		String outPath = "/home/mysql-ib/nr_mass_sorted_taxid.csv";
		String outPathAccessionNotFind = "/home/mysql-ib/nr_mass_taxid_sorted_accession_not_found.csv";
		String storeFileDb = "/home/users/tag/nr_db/mvstore_prot.accession2taxid.db";
		String leveldbPath = "/home/mysql-ib/nr_mass_leveldb";

		// String storeDeadFileDb =
		// "/home/users/tag/nr_db/mvstore_prot.accession2taxid_dead.db";
		String csvPath = "/home/mysql-ib/nr_mass_sorted.csv";

		if (SystemUtils.IS_OS_WINDOWS) {
			System.out.println("WINDOWS");
			String dir = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\";
			storeFileDb = dir + "mvstore_prot.accession2taxid_all.db";
			csvPath = dir + "nr_mass_sorted_8_000_000.csv";

			String dirResult = "c:/tmp/";
			outPath = dirResult + "nr_mass_taxid_sorted.csv";
			// storeDeadFileDb = dir + "mvstore_prot.accession2taxid_dead.db";
			leveldbPath = dirResult + "nr_mass_leveldb";
			outPathAccessionNotFind = dirResult + "nr_mass_taxid_sorted_accession_not_found.csv";
		}
		System.out.println("OUT " + outPath);

		Options options = new Options();
		options.createIfMissing(true);

		options.cacheSize(100 * 1048576 * 10); // 100MB cache
		options.compressionType(CompressionType.SNAPPY);
		options.verifyChecksums(false);
		options.paranoidChecks(false);

		System.out.println("Level DB: " + leveldbPath);
		db = LevelDButil.open(leveldbPath, options);

		// MVStoreTool.compact(storeDeadFileDb, true);
		// MVStoreTool.info(storeFileDb);

		FastBufferedOutputStream out = new FastBufferedOutputStream(new FileOutputStream(outPath));
		FastBufferedOutputStream outAccNotFound = new FastBufferedOutputStream(
				new FileOutputStream(outPathAccessionNotFind));

		MVStore storeAcc = new MVStore.Builder().cacheSize(1024).fileName(storeFileDb).compress().open();
		MVMap<Object, Object> mapAcc = storeAcc.openMap("a");
		System.out.println("Ima acc u MVStore bazi: " + mapAcc.size());

		// map
		// MVStore storeAccDead = new
		// MVStore.Builder().cacheSize(512).fileName(storeDeadFileDb).compress().open();
		// MVMap<Object, Object> mapAccDead = storeAcc.openMap("acc_taxid");
		// System.out.println(storeAcc.getMapNames());
		// System.out.println("Ima ih acc dead " + mapAccDead.size());

		MassCSV csv = new MassCSV(csvPath);
		csv.startIterate(new CallbackMass() {
			int cNotFind = 0;

			int cDead = 0;

			@Override
			public void row(double mass, String accVersion, String peptide) {
				try {
					String acc = BioUtil.removeVersionFromAccession(accVersion).trim();
					c++;
					Object taxId = mapAcc.get(acc);
					int taxIdInt = 0;
					if (taxId != null) {
						//taxIdInt = Integer.parseInt(taxId.toString());
					} else {
						cNotFind++;
						outAccNotFound.write((acc + "\n").getBytes(Charsets.US_ASCII));
					}
//					EntrySax r = new EntrySax();
//					r.setAcc(acc);
//					r.setMass((float) mass);
//					r.setPeptide(peptide);
//					r.setTaxId(taxIdInt);
//
//					processRow(r, out);

				} catch (Throwable e) {
					e.printStackTrace();
				}

				// if (c > 1000000)
				// csv.stop();

			}

			@Override
			public void finish() {
				System.out.println("Total of dead " + cDead);
				System.out.println("Total " + c);
				System.out.println("Total not find " + cNotFind);

				storeAcc.close();
				// storeAccDead.close();
				IOUtils.closeQuietly(out);
				IOUtils.closeQuietly(outAccNotFound);
				try {
					// db.close();
					// db.compactRange(null, null);
					db.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

	}

	protected static void processRow(Row r, FastBufferedOutputStream out) throws IOException {
		if (lastMass != r.getMass() && lastMass != 0) {
			processRowsSameMass(out);
			sameMassRow.clear();
		}

		sameMassRow.add(r);
		lastMass = r.getMass();

	}

	private static void processRowsSameMass(FastBufferedOutputStream out) throws IOException {

		sameMassRow
				.sort(Comparator.comparing(Row::getMass).thenComparing(Row::getPeptide).thenComparing(Row::getTaxId));
		StringBuilder b = new StringBuilder(sameMassRow.size() * 60);
		for (Row r : sameMassRow) {
			// @formatter:off
			b.append(r.getMass()).append("\t")
			.append(r.getPeptide()).append("\t")
			.append(r.getAcc()).append("\t")
			.append(r.getTaxId()).append("\n");
			// @formatter:on
		}
		out.write(b.toString().getBytes(Charsets.US_ASCII));

		writeLevelDb();
	}

	
}

class Row {
	private float mass;
	private String peptide;
	/**
	 * without version
	 */
	private String acc;

	private int taxId;

	public float getMass() {
		return mass;
	}

	public void setMass(float mass) {
		this.mass = mass;
	}

	public String getPeptide() {
		return peptide;
	}

	public void setPeptide(String peptide) {
		this.peptide = peptide;
	}
	public String getAcc() {
		return acc;
	}
	public void setAcc(String acc) {
		this.acc = acc;
	}
	public int getTaxId() {
		return taxId;
	}
	public void setTaxId(int taxId) {
		this.taxId = taxId;
	}
}
