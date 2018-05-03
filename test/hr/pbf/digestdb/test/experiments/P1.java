package hr.pbf.digestdb.test.experiments;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.KryoDataInput;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.io.ByteArrayDataOutput;

import hr.pbf.digestdb.uniprot.MyDataInputStream;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideAccTaxMass;
import hr.pbf.digestdb.uniprot.UniprotUtil;
import hr.pbf.digestdb.util.CallbackMass;
import hr.pbf.digestdb.util.MassCSV;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

public class P1 {
	private static final Logger log = LoggerFactory.getLogger(P1.class);

	public static void main(String[] args) throws NumberFormatException, IOException {
		// testASCII();
	//	testData();

	}

	private static void testData() throws NumberFormatException, IOException {
		String s = "2039281ADSDFSDFewrewrewr2348698234234";
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
//		data.writeInt(Integer.parseInt(s));
//		System.out.println("string len "+ s.length());
//		System.out.println("bytes len "+ bytes.size());
		
//		MyDataInputStream s;
//		s.readUTF()
//		Input in;
		Output out = new Output(new FastByteArrayOutputStream());
		out.writeAscii(s);
		out.flush();
		System.out.println("len "+ s.length());
		System.out.println(out.toBytes().length);
				
	}

	private static void testASCII() {
		String s = "CGAGHG  A0A1Y0CGV9:482462,A0A2C9SVL5:2039281";
		byte[] bytes = s.getBytes(StandardCharsets.US_ASCII);
		System.out.println(s.length() + ": " + bytes.length);
		System.out.println(s);
		System.out.println(new String(bytes));
	}

	public static void main22(String[] args) throws IOException {
		String p = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\nemoze citati\\2801.3.db";
		ArrayList<PeptideAccTax> peptides = UniprotUtil.fromFormat1(UniprotUtil.toByteArrayFast(p));
		Map<String, List<PeptideAccTax>> groupByPeptide = UniprotUtil.groupByPeptide(peptides);
		System.out.println("peptida ima ukupno: " + peptides.size());
		System.out.println("Unique peptida    : " + groupByPeptide.size());
		System.out.println("Grupiranih peptida ukupno: " + count(groupByPeptide));

		byte[] format2 = UniprotUtil.toFormat2(groupByPeptide);
		format2 = Snappy.compress(format2);
		File filef2s = new File(p + ".f2s");
		FileOutputStream output = new FileOutputStream(filef2s);
		IOUtils.write(format2, output);
		output.close();

		byte[] bytes = UniprotUtil.toByteArrayFast(filef2s);
		bytes = Snappy.uncompress(bytes);
		Map<String, List<PeptideAccTaxMass>> data = UniprotUtil.fromFormat2(bytes, true);
		log.debug("Unique peptides " + data.size());

	}

	private static int count(Map<String, List<PeptideAccTax>> groupByPeptide) {
		int c = 0;
		Set<Entry<String, List<PeptideAccTax>>> entrySet = groupByPeptide.entrySet();
		for (Entry<String, List<PeptideAccTax>> entry : entrySet) {
			c += entry.getValue().size();
		}
		return c;
	}

	public static void main222(String[] args) {
		float fromMass = 1300f;
		float toMass = 1300.1f;

		float mass = 1300.3457f;

		if (mass < fromMass || toMass < mass) {
			System.out.println("skip");
		} else {
			System.out.println("not skip");
		}
	}

	static int c = 0;

	public static void main2(String[] args) throws FileNotFoundException, IOException {

		MassCSV csv = new MassCSV(
				"C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\nr_mass_sorted_200_000.csv");
		csv.startIterate(new CallbackMass() {
			@Override
			public void row(double mass, String accVersion, String peptide) {
				// System.out.println(mass + " "+ accVersion + " "+ peptide);
				c++;
			}
		});
		System.out.println(c);

	}
}
