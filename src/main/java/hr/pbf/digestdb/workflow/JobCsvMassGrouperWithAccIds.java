package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.util.BinaryPeptideDbUtil;
import hr.pbf.digestdb.util.MyStopWatch;
import hr.pbf.digestdb.util.MyUtil;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 Not thread safe.
 */
@Slf4j
@Data
public class JobCsvMassGrouperWithAccIds {

	private final StringBuilder sb = new StringBuilder(1024);

	String inputCsvPeptideMassSorted = "";
	String outputGroupedCsv = "";
	String outputAccessionMapCsv = "";
	int bufferSize = 64 * 1024 * 1024; // 64MB buffer
	long proteinCount = 0;

	public Long2IntMap start() {
		Long2IntMap accessionToIdMap = buildAccessionMap(inputCsvPeptideMassSorted, outputAccessionMapCsv, bufferSize);
		log.debug("Accession map is builded. Size: {}", accessionToIdMap.size());
		startCreateGroupWithAccIds(inputCsvPeptideMassSorted, outputGroupedCsv, accessionToIdMap, bufferSize);
		return accessionToIdMap;
	}

	/**
	 * Read csv and remove double accession and sort with
	 *
	 * @param inputCsvPath
	 * @param outputGroupedCsvPath
	 * @param bufferSize
	 * @return
	 */
	private Long2IntMap buildAccessionMap(String inputCsvPath, String outputGroupedCsvPath, int bufferSize) {
		// Java heap space for TrEMLB od 32GB RAM
		// accession => accessionNum
		//Object2IntMap<String> accessionToIdMap = new Object2IntOpenHashMap<>();
		Long2IntMap accessionLongToIntMap = new Long2IntOpenHashMap(Math.toIntExact(proteinCount));
		int nextAccNumId = 0;
		// Trembl ha 10_252_140_061 lines

		MyStopWatch stopWatch = new MyStopWatch();
		log.debug("Start creating accession map from CSV file: {}", inputCsvPath);

		try(BufferedReader reader = new BufferedReader(new FileReader(inputCsvPath), bufferSize)) {
			String line;
			long lineCount = 0;
			while((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				if(parts.length < 3)
					throw new IllegalArgumentException("Invalid input CSV format " + line);
				String accession = parts[2];
				long accessionLong = MyUtil.toAccessionLong36(accession);
				if(!accessionLongToIntMap.containsKey(accessionLong)) {
					if(nextAccNumId == Integer.MAX_VALUE) {
						throw new IllegalStateException("Counter reached maximum int value for accession number.");
					}
					//accessionToIdMap.put(accession, nextAccNumId++);
					accessionLongToIntMap.put(accessionLong, nextAccNumId++);
				}
				if(lineCount++ % 100_000_000 == 0) {
					log.debug("Processed {} accession numbers. Time: {}", nextAccNumId, stopWatch.getCurrentDuration());
				}

			}
		} catch(IOException e) {
			log.error("Error reading input CSV", e);
			throw new RuntimeException(e);
		}
		log.debug("Finished creating accession map. Size: {}. Time: {}", accessionLongToIntMap.size(), stopWatch.getCurrentDuration());

		log.debug("Start writing accession map to CSV file: {}", outputGroupedCsvPath);
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputGroupedCsvPath), bufferSize)) {

			accessionLongToIntMap.forEach((accession, id) -> {
				try {
					writer.write(id + "," + MyUtil.fromAccessionLong36(accession));
					writer.newLine();
				} catch(IOException e) {
					log.error("Error writing accession map", e);
					throw new RuntimeException(e);
				}
			});

		} catch(IOException e) {
			log.error("Error writing accession map", e);
			throw new RuntimeException(e);
		}
		return accessionLongToIntMap;
	}

	private void startCreateGroupWithAccIds(String inputCsv, String outputGroupedCsv,
			Long2IntMap accessionToIdMap, int bufferSize) {
		log.debug("Reading input CSV file: {} and write CSV file: {}", inputCsv, outputGroupedCsv);
		try(BufferedReader reader = new BufferedReader(new FileReader(inputCsv), bufferSize);
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputGroupedCsv), bufferSize)) {

			String line = reader.readLine();
			if(line == null || line.isBlank())
				return;

			String[] parts = line.split(",");
			if(parts.length < 3)
				throw new IllegalArgumentException("Invalid input CSV format: " + line);

			String sequence = parts[1];
			String accession = parts[2];
			String massString = parts[0];

			double prevMass = Double.parseDouble(massString);
			String prevMass4 = MyUtil.discretizedTo4(prevMass);

			Map<String, IntOpenHashSet> seqIdsMap = new HashMap<>();

			//int accNum = accessionToIdMap.getInt(accession);
			int accNum = accessionToIdMap.get(MyUtil.toAccessionLong36(accession));
			seqIdsMap.computeIfAbsent(sequence, k -> new IntOpenHashSet()).add(accNum);

			while((line = reader.readLine()) != null) {
				parts = line.split(",");
				if(parts.length < 3)
					throw new IllegalArgumentException("Invalid input CSV format: " + line);

				sequence = parts[1];
				accession = parts[2];

				double mass = Double.parseDouble(parts[0]);
				String mass4 = MyUtil.discretizedTo4(mass);

				accNum = accessionToIdMap.get(MyUtil.toAccessionLong36(accession));

				if(mass4.equals(prevMass4)) {
					seqIdsMap.computeIfAbsent(sequence, k -> new IntOpenHashSet()).add(accNum);
				} else {
					writeMassToCsv(writer, prevMass4, seqIdsMap);
					prevMass4 = mass4;
					seqIdsMap.clear();
					seqIdsMap.computeIfAbsent(sequence, k -> new IntOpenHashSet()).add(accNum);
				}
			}
			if(!seqIdsMap.isEmpty())
				writeMassToCsv(writer, prevMass4, seqIdsMap);

		} catch(IOException | NumberFormatException e) {
			log.error("Error reading input CSV", e);
			throw new RuntimeException(e);
		}
		log.debug("Finished writing grouped CSV file: {}", outputGroupedCsv);
	}

	private void writeMassToCsv(BufferedWriter writer, String mass, Map<String, IntOpenHashSet> sequenceMap) throws IOException {
		BinaryPeptideDbUtil.writeMassToCsvRow(sb,sequenceMap);

		writer.write(mass + "," + sb);
		writer.newLine();
	}

	private void writeMassToCsvOld(BufferedWriter writer, String mass, Map<String, IntOpenHashSet> sequenceMap)
			throws IOException {
		sb.setLength(0);
		Set<Map.Entry<String, IntOpenHashSet>> entries = sequenceMap.entrySet();
		for(Map.Entry<String, IntOpenHashSet> entry : entries) {
			String sequence = entry.getKey();
			IntOpenHashSet ids = entry.getValue();

			StringBuilder idsStr = new StringBuilder(ids.size() * 6);
			for(IntIterator it = ids.iterator(); it.hasNext(); ) {
				idsStr.append(it.nextInt()).append(";");
			}
			if(!idsStr.isEmpty())
				idsStr.setLength(idsStr.length() - 1);

			sb.append(sequence).append(":").append(idsStr).append("-");
		}
		if(!sb.isEmpty())
			sb.setLength(sb.length() - 1);

		writer.write(mass + "," + sb);
		writer.newLine();
	}
}

