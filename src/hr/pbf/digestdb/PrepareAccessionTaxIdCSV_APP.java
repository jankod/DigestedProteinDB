package hr.pbf.digestdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class PrepareAccessionTaxIdCSV_APP {

	public static final String ARG_APP_NAME = "acc";

	public static void printArgs() {
		System.out.println();
		System.out.println(PrepareAccessionTaxIdCSV_APP.class.getName());
		System.out.println("" + ARG_APP_NAME + " path");
		System.out.println();
	}

	public static void main(String[] args) throws IOException {

		String csvPath = "C:\\Eclipse\\OxygenWorkspace\\CreateNR\\misc\\sample_data\\900_000prot.accession2taxid.txt";

		if (args.length > 1) {
			csvPath = args[1];
			System.out.println("Read: " + csvPath);
		}

		String resultPath = csvPath + "_prepered.txt";
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(resultPath)));

		// necu header
		// out.write("accession\ttaxid\tgi");

		
		// TODO: postaviti na us-ascii
		BufferedReader in = new BufferedReader(new FileReader(new File(csvPath)));
		
		
		String line = null;
		boolean first = true;

		while ((line = in.readLine()) != null) {

			if (StringUtils.isEmpty(line)) {
				continue;
			}
			if (StringUtils.startsWith(line, "accession")) {
				continue;
			}

			// accession accession.version taxid gi
			String[] split = StringUtils.split(line, '\t');
			// System.out.println(Arrays.toString(split));
			if (first) {
				first = false;
			} else {
				out.write("\n");
			}

			String accession = split[0];
//			BigInteger bi = new BigInteger(accession.getBytes());

			//out.write(bi.longValue() + "\t" + accession + "\t" + split[2] + "\t" + split[3]);
			out.write(accession + "\t" + split[2] + "\t" + split[3]);

		}

		out.close();
		in.close();
		System.out.println("Result file " + resultPath);
	}

}