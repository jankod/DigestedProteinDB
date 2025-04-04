package hr.pbf.digestdb.demo;

import java.io.*;
import java.nio.file.*;

public class CsvProcessorLowMemory {
    public static void main(String[] args) {
        String inputFile = "input.csv";                  // Your 200GB input file
        String tempMappingFile = "temp_accNum_accession.csv"; // Temporary mapping file
        String outputFile1 = "mass_peptide_accNum.csv";  // mass,peptide,accNum
        String outputFile2 = "accNum_accession.csv";     // accNum,accession

        try {
            // Step 1: Create accession to accNum mapping with minimal memory usage
            createAccessionMapping(inputFile, tempMappingFile);

            // Step 2: Rewrite input file with accNum and finalize mapping file
            rewriteWithAccNum(inputFile, tempMappingFile, outputFile1, outputFile2);

            // Clean up temporary file
            Files.deleteIfExists(Paths.get(tempMappingFile));
            System.out.println("Processing complete.");

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void createAccessionMapping(String inputFile, String tempMappingFile) throws IOException {
        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter tempWriter = new BufferedWriter(new FileWriter(tempMappingFile))
        ) {
            // Write temporary header
            tempWriter.write("accNum,accession\n");

            // Use a simple counter for accNum
            int accNumCounter = 0;
            String lastAccession = null;

            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }

                String[] parts = line.split(",");
                if (parts.length != 3) {
                    System.err.println("Invalid line: " + line);
                    continue;
                }

                String accession = parts[2];

                // Only write new accession values (assuming input is sorted or we deduplicate later)
                if (lastAccession == null || !accession.equals(lastAccession)) {
                    tempWriter.write(accNumCounter + "," + accession + "\n");
                    lastAccession = accession;
                    accNumCounter++;
                }
            }
            System.out.println("Total unique accessions: " + accNumCounter);
        }
    }

    private static void rewriteWithAccNum(String inputFile, String tempMappingFile,
                                          String outputFile1, String outputFile2) throws IOException {
        try (
            BufferedReader inputReader = new BufferedReader(new FileReader(inputFile));
            BufferedReader mappingReader = new BufferedReader(new FileReader(tempMappingFile));
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(outputFile1));
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(outputFile2))
        ) {
            // Write headers
            writer1.write("mass,peptide,accNum\n");
            writer2.write("accNum,accession\n");

            // Copy mapping to final output (accNum,accession)
            String mappingLine;
            while ((mappingLine = mappingReader.readLine()) != null) {
                writer2.write(mappingLine + "\n");
            }

            // Process input file and replace accession with accNum
            String line;
            boolean firstLine = true;
            mappingReader.close(); // Reopen mapping file for second pass
            try (BufferedReader mappingReader2 = new BufferedReader(new FileReader(tempMappingFile))) {
                String currentMapping = mappingReader2.readLine(); // Skip header
                currentMapping = mappingReader2.readLine(); // First data line
                String[] mappingParts = currentMapping.split(",");
                int currentAccNum = Integer.parseInt(mappingParts[0]);
                String currentAccession = mappingParts[1];

                while ((line = inputReader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue; // Skip header
                    }

                    String[] parts = line.split(",");
                    if (parts.length != 3) continue;

                    String mass = parts[0];
                    String peptide = parts[1];
                    String accession = parts[2];

                    // Find matching accNum by scanning mapping file
                    while (!accession.equals(currentAccession) && (currentMapping = mappingReader2.readLine()) != null) {
                        mappingParts = currentMapping.split(",");
                        currentAccNum = Integer.parseInt(mappingParts[0]);
                        currentAccession = mappingParts[1];
                    }

                    // Write to output file
                    writer1.write(mass + "," + peptide + "," + currentAccNum + "\n");
                }
            }
        }
    }
}