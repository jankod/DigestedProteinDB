package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.util.MyUtil;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Data
public class MainCsvMassGrouperWithAccIds {

    private final StringBuilder sb = new StringBuilder(1024);

    String inputCsvPeptideMassSorted = "";
    String outputGroupedCsv = "";
    String outputAccessionMapCsv = "";
    int bufferSize = 32 * 1024 * 1024; // 32MB buffer

    public Object2IntMap<String> start() {
        Object2IntMap<String> accessionToIdMap = startCreateAccessionMap();
        startCreateGroupWithAccIds(inputCsvPeptideMassSorted, outputGroupedCsv, accessionToIdMap, bufferSize);
        return accessionToIdMap;
    }

    public Object2IntMap<String> startCreateAccessionMap() {
        return buildAccessionMap(inputCsvPeptideMassSorted, outputAccessionMapCsv, bufferSize);
    }

    private Object2IntMap<String> buildAccessionMap(String inputCsvPath, String outputGroupedCsvPath, int bufferSize) {
        Object2IntMap<String> accessionToIdMap = new Object2IntOpenHashMap<>();

        int nextAccNumId = 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputCsvPath), bufferSize)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) throw new IllegalArgumentException("Invalid input CSV format " + line);
                String accession = parts[2];
                if (!accessionToIdMap.containsKey(accession)) {
                    if (nextAccNumId == Integer.MAX_VALUE) {
                        throw new IllegalStateException("Counter reached maximum int value.");
                    }
                    accessionToIdMap.put(accession, nextAccNumId++);
                }

            }
        } catch (IOException e) {
            log.error("Error reading input CSV", e);
            throw new RuntimeException(e);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputGroupedCsvPath), bufferSize)) {

            accessionToIdMap.forEach((accession, id) -> {
                try {
                    writer.write(id + "," + accession);
                    writer.newLine();
                } catch (IOException e) {
                    log.error("Error writing accession map", e);
                    throw new RuntimeException(e);
                }
            });

        } catch (IOException e) {
            log.error("Error writing accession map", e);
            throw new RuntimeException(e);
        }
        return accessionToIdMap;
    }

    private void startCreateGroupWithAccIds(String inputCsv, String outputGroupedCsv,
                                            Object2IntMap<String> accessionToIdMap, int bufferSize) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputCsv), bufferSize);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputGroupedCsv), bufferSize)) {

            //  reader.readLine(); // Preskoči header

            String line = reader.readLine();
            if (line == null || line.isBlank()) return;

            String[] parts = line.split(",");
            if (parts.length < 3) throw new IllegalArgumentException("Invalid input CSV format: " + line);

            double prevMass = Double.parseDouble(parts[0]);
            String prevMass4 = MyUtil.discretizedTo4(prevMass);

            Map<String, IntOpenHashSet> seqIdsMap = new HashMap<>();

            String sequence = parts[1];
            int id = accessionToIdMap.getInt(parts[2]);
            seqIdsMap.computeIfAbsent(sequence, k -> new IntOpenHashSet()).add(id);

            while ((line = reader.readLine()) != null) {
                parts = line.split(",");
                if (parts.length < 3) throw new IllegalArgumentException("Invalid input CSV format: " + line);

                double mass = Double.parseDouble(parts[0]);
                String mass4 = MyUtil.discretizedTo4(mass);

                if (mass4.equals(prevMass4)) {
                    sequence = parts[1];
                    id = accessionToIdMap.getInt(parts[2]);
                    seqIdsMap.computeIfAbsent(sequence, k -> new IntOpenHashSet()).add(id);
                } else {
                    writeMassToCsv(writer, prevMass4, seqIdsMap);
                    prevMass4 = mass4;
                    seqIdsMap.clear();
                    sequence = parts[1];
                    id = accessionToIdMap.getInt(parts[2]);
                    seqIdsMap.computeIfAbsent(sequence, k -> new IntOpenHashSet()).add(id);
                }
            }
            writeMassToCsv(writer, prevMass4, seqIdsMap);

        } catch (IOException | NumberFormatException e) {
            log.error("Error reading input CSV", e);
            throw new RuntimeException(e);
        }
    }

    private void writeMassToCsv(BufferedWriter writer, String mass, Map<String, IntOpenHashSet> sequenceMap)
          throws IOException {
        sb.setLength(0);
        Set<Map.Entry<String, IntOpenHashSet>> entries = sequenceMap.entrySet();
        for (Map.Entry<String, IntOpenHashSet> entry : entries) {
            String sequence = entry.getKey();
            IntOpenHashSet ids = entry.getValue();

            StringBuilder idsStr = new StringBuilder(ids.size() * 6);
            for (IntIterator it = ids.iterator(); it.hasNext(); ) {
                idsStr.append(it.nextInt()).append(";");
            }
            if (!idsStr.isEmpty()) idsStr.setLength(idsStr.length() - 1);

            sb.append(sequence).append(":").append(idsStr).append("-");
        }
        if (!sb.isEmpty()) sb.setLength(sb.length() - 1);

        writer.write(mass + "," + sb);
        writer.newLine();
    }
}

