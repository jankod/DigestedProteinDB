package hr.pbf.digestdb.workflow;

import gnu.trove.map.hash.TObjectIntHashMap;
import hr.pbf.digestdb.util.MyUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

@Slf4j
@Data
public class MainCsvGrouperWithIds {


    private final StringBuilder sb = new StringBuilder(1024);

    String inputCsv = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated/peptide_mass_sorted_console.csv";
    String outputGroupedCsv = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated/grouped_with_ids.csv";
    String outputAccessionMapCsv = "/Users/tag/IdeaProjects/DigestedProteinDB/misc/generated/accession_map.csv";
    int bufferSize = 16 * 1024 * 1024; // 16MB buffer

    public static void main(String[] args) {
        MainCsvGrouperWithIds app = new MainCsvGrouperWithIds();
        app.start();
    }

    public void start() {
        // Faza 1: Izgradi mapiranje accession -> ID
        TObjectIntHashMap<String> accessionToIdMap = buildAccessionMap(inputCsv, outputAccessionMapCsv, bufferSize);
        // Map<String, Integer> accessionToIdMap = buildAccessionMap(inputCsv, outputAccessionMapCsv, bufferSize);
        log.info("Created accessionToIdMap.size() = " + accessionToIdMap.size());
        // Faza 2: Grupiraj podatke s ID-ovima
        groupDataWithIds(inputCsv, outputGroupedCsv, accessionToIdMap, bufferSize);
    }

    private TObjectIntHashMap<String> buildAccessionMap(String inputCsv, String outputMapCsv, int bufferSize) {
        TObjectIntHashMap<String> accessionToIdMap = new TObjectIntHashMap<>();
        int nextId = 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputCsv), bufferSize)) {
            // reader.readLine(); // Preskoči header

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) throw new IllegalArgumentException("Invalid input CSV format " + line);
                String accession = parts[2];
                if (!accessionToIdMap.containsKey(accession)) {
                    accessionToIdMap.put(accession, nextId++);
                }

            }
        } catch (IOException e) {
            log.error("Error reading input CSV", e);
            throw new RuntimeException(e);
        }

        // Spremi mapiranje u CSV
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputMapCsv), bufferSize)) {
            writer.write("id,accession");
            writer.newLine();

            accessionToIdMap.forEachEntry((accession, id) -> {
                try {
                    writer.write(id + "," + accession);
                    writer.newLine();
                } catch (IOException e) {
                    log.error("Error writing accession map", e);
                    return false;
                }
                return true;
            });


        } catch (IOException e) {
            log.error("Error writing accession map", e);
        }
        log.info("Max acc Id = " + nextId);
        return accessionToIdMap;
    }

    private void groupDataWithIds(String inputCsv, String outputGroupedCsv,
                                  TObjectIntHashMap<String> accessionToIdMap, int bufferSize) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputCsv), bufferSize);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputGroupedCsv), bufferSize)) {

            reader.readLine(); // Preskoči header

            String line = reader.readLine();
            if (line == null) return;

            String[] parts = line.split(",");
            if (parts.length < 3) throw new IllegalArgumentException("Invalid input CSV format " + line);

            double prevMass = Double.parseDouble(parts[0]);
            String prevMass4 = MyUtil.discretizedToFour(prevMass); // Pretpostavljam da ovo postoji
            Map<String, Set<Integer>> seqIdsMap = new HashMap<>();

            String sequence = parts[1];
            int id = accessionToIdMap.get(parts[2]);
            seqIdsMap.computeIfAbsent(sequence, k -> new HashSet<>()).add(id);

            while ((line = reader.readLine()) != null) {
                parts = line.split(",");
                if (parts.length < 3) continue;

                double mass = Double.parseDouble(parts[0]);
                String mass4 = MyUtil.discretizedToFour(mass);

                if (mass4.equals(prevMass4)) {
                    sequence = parts[1];
                    id = accessionToIdMap.get(parts[2]);
                    seqIdsMap.computeIfAbsent(sequence, k -> new HashSet<>()).add(id);
                } else {
                    writeMassToCsv(writer, prevMass4, seqIdsMap);
                    prevMass4 = mass4;
                    seqIdsMap.clear();
                    sequence = parts[1];
                    id = accessionToIdMap.get(parts[2]);
                    seqIdsMap.computeIfAbsent(sequence, k -> new HashSet<>()).add(id);
                }
            }
            writeMassToCsv(writer, prevMass4, seqIdsMap);

        } catch (IOException | NumberFormatException e) {
            log.error("Error reading input CSV", e);
            throw new RuntimeException(e);
        }
    }

    private void writeMassToCsv(BufferedWriter writer, String mass, Map<String, Set<Integer>> sequenceMap)
          throws IOException {
        sb.setLength(0);
        for (Map.Entry<String, Set<Integer>> entry : sequenceMap.entrySet()) {
            String sequence = entry.getKey();
            Set<Integer> ids = entry.getValue();
            String idsStr = String.join(";", ids.stream().map(String::valueOf).toArray(String[]::new));
            sb.append(sequence).append(":").append(idsStr).append("-");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        writer.write(mass + "," + sb.toString());
        writer.newLine();
    }
}

