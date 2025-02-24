package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.util.MyUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

/**
 * Read csv file with columns: mass, sequence, accession
 * Group by mass and write to new csv file.
 *
 */
@Slf4j
@Data
public class MainCsvMassGrouper {


    public String inputCsv = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated/peptide_mass_sorted_console.csv";
    public String outputCsv = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated/peptide_mass_sorted_console_grouped.csv";


    public void start() {

        int sz = 16 * 1024 * 1024; // 16 MB buffer
        try (BufferedReader reader = new BufferedReader(new FileReader(inputCsv), sz);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputCsv), sz)) {

            // Preskoči header
            // reader.readLine();

            String line = reader.readLine();
            if (line == null) return; // Prazna datoteka

            // Inicijalizacija za prvu liniju
            String[] parts = line.split(",");
            if (parts.length < 3) return; // Nevažeća datoteka

            double prevMass = Double.parseDouble(parts[0]);
            String prevMass4 = MyUtil.discretizedTo4(prevMass);
            Map<String, Set<String>> seqAccessionsMap = new HashMap<>();

            String sequence = parts[1];
            String accession = parts[2];
            Set<String> accessions = seqAccessionsMap.computeIfAbsent(sequence, k -> new HashSet<>());
            accessions.add(accession);

            // Čitanje ostalih linija
            while ((line = reader.readLine()) != null) {
                parts = line.split(",");
                if (parts.length < 3) throw new RuntimeException("Invalid line: " + line);

                double mass = Double.parseDouble(parts[0]);
                String mass4 = MyUtil.discretizedTo4(mass);
                //long key = Math.round(mass * 10_000);

                if (mass4.equals(prevMass4)) {
                    // Ista masa, dodaj u trenutnu mapu
                    sequence = parts[1];
                    accession = parts[2];
                    accessions = seqAccessionsMap.computeIfAbsent(sequence, k -> new HashSet<>());
                    accessions.add(accession);
                } else {
                    // Nova masa, napiši trenutnu grupu i resetiraj
                    writeMassToCsv(writer, prevMass4, seqAccessionsMap);
                    prevMass4 = mass4;
                    seqAccessionsMap = new HashMap<>();
                    sequence = parts[1];
                    accession = parts[2];
                    accessions = seqAccessionsMap.computeIfAbsent(sequence, k -> new HashSet<>());
                    accessions.add(accession);
                }
            }

            // Napiši posljednju grupu
            writeMassToCsv(writer, prevMass4, seqAccessionsMap);

        } catch (IOException | NumberFormatException e) {
            log.error("Error reading file: "+ inputCsv, e);
        }
    }

    private final StringBuilder sb = new StringBuilder(1024*16); // Inicijalna veličina po potrebi

    private void writeMassToCsv(BufferedWriter writer, String mass, Map<String, Set<String>> sequenceMap)
          throws IOException {
        sb.setLength(0); // Resetiraj StringBuilder

        for (Map.Entry<String, Set<String>> entry : sequenceMap.entrySet()) {
            String sequence = entry.getKey();
            Set<String> accessions = entry.getValue();
            String accessionsStr = String.join(";", accessions);
            sb.append(sequence).append(":").append(accessionsStr).append("-");
        }

        // Ukloni zadnji zarez
        if (sb.length() > 0) sb.setLength(sb.length() - 1);

        writer.write(mass + "," + sb);
        writer.newLine();
    }
}
