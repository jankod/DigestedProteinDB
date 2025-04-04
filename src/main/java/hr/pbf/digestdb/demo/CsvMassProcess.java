package hr.pbf.digestdb.demo;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.*;
import java.util.*;

public class CsvMassProcess {
    public static void main(String[] args) {
        // Input and output file paths
        String inputFile = "/media/tag/G/digested_db/trembl_trypsin/gen/peptide_mass_sorted.csv";           // Your 200GB input file
        String outputFile1 = "/media/tag/G/digested_db/trembl_trypsin/gen/mass_peptide_accNum.csv"; // mass,peptide,accNum
        String outputFile2 = "/media/tag/G/digested_db/trembl_trypsin/gen/accNum_accession.csv";    // accNum,accession

        // Map to store accession to accNum mapping
        //Map<String, Integer> accessionToAccNum = new HashMap<>();
        Object2IntMap<String> accessionToAccNum = new Object2IntOpenHashMap<>();
        int accNumCounter = 0;

        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(outputFile1));
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(outputFile2))
        ) {
            // Write headers to output files
            writer1.write("mass,peptide,accNum\n");
            writer2.write("accNum,accession\n");

            String line;
            // Skip header if present
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header row
                }

                // Split CSV line (assuming comma-separated)
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    System.err.println("Invalid line: " + line);
                    continue;
                }

                String mass = parts[0];
                String peptide = parts[1];
                String accession = parts[2];

                // Assign accNum to accession if not already assigned
                accessionToAccNum.putIfAbsent(accession, accNumCounter);
                int currentAccNum = accessionToAccNum.get(accession);
                if (!accessionToAccNum.containsKey(accession)) {
                    accNumCounter++;
                }

                // Write to mass_peptide_accNum.csv
                writer1.write(mass + "," + peptide + "," + currentAccNum + "\n");
            }

            // Write the accNum,accession mapping to the second file
            for (Map.Entry<String, Integer> entry : accessionToAccNum.entrySet()) {
                writer2.write(entry.getValue() + "," + entry.getKey() + "\n");
            }

            System.out.println("Processing complete. Total unique accessions: " + accessionToAccNum.size());

        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }
}