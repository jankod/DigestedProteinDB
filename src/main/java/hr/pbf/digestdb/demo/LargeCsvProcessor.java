package hr.pbf.digestdb.demo;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LargeCsvProcessor {

	public static void main(String[] args) throws IOException {
		String inputFilePath = "/media/tag/G/digested_db/db_swisprot_chymotrypsin/gen/peptide_mass_sorted.csv";
		String outputFilePath1 = "/media/tag/G/digested_db/db_swisprot_chymotrypsin/gen/mass_peptide_accNum.csv";
		String outputFilePath2 = "/media/tag/G/digested_db/db_swisprot_chymotrypsin/gen/accNum_accession.csv";

		processLargeCsv(inputFilePath, outputFilePath1, outputFilePath2);
	}

	public static void processLargeCsv(String inputFilePath, String outputFilePath1, String outputFilePath2) throws IOException {
		Map<String, Integer> accessionMap = new HashMap<>();
		int accNumCounter = 0;
		BloomFilter<String> bloomFilter = BloomFilter.create(
				Funnels.stringFunnel(StandardCharsets.UTF_8),
				10000000,
				0.01
		);

		try(BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);


				BufferedWriter writer1 = new BufferedWriter(new FileWriter(outputFilePath1));
				CSVPrinter csvPrinter1 = new CSVPrinter(writer1, CSVFormat.DEFAULT.withHeader("mass", "peptide", "accNum"));
				BufferedWriter writer2 = new BufferedWriter(new FileWriter(outputFilePath2));
				CSVPrinter csvPrinter2 = new CSVPrinter(writer2, CSVFormat.DEFAULT.withHeader("accNum", "accession"))) {

			for(CSVRecord record : csvParser) {
				String mass = record.get(0);
				String peptide = record.get(1);
				String accession = record.get(2);

				if(!bloomFilter.mightContain(accession)) {
					bloomFilter.put(accession);
					if(!accessionMap.containsKey(accession)) {
						accessionMap.put(accession, accNumCounter);
						csvPrinter2.printRecord(accNumCounter, accession);
						accNumCounter++;
					}

					int accNum = accessionMap.get(accession);
					csvPrinter1.printRecord(mass, peptide, accNum);
				} else {
					int accNum = accessionMap.get(accession);
					csvPrinter1.printRecord(mass, peptide, accNum);
				}
			}

		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}