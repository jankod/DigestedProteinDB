package hr.pbf.digestdb.workflow;

import hr.pbf.digestdb.util.CsvReader;
import hr.pbf.digestdb.util.CsvReader.CsvRow;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 Not thread safe.
 */
@Slf4j
@Data
public class JobCsvMassGrouperWithAccIds2 {

	private final StringBuilder sb = new StringBuilder(1024);

	String inputCsvPeptideMassSorted = "";
	String outputGroupedCsv = "";
	String outputAccessionMapCsv = "";
	int bufferSize = 32 * 1024 * 1024; // 32MB buffer

	int currentAccNum = 0;
	MassRow massRow = new MassRow();



	private void createAccList() {
		String[] accList;
	}

	/**
	 * Read csv:
	 * 667.3,ACGALAY,Q0KHV6
	 * 667.3,AGGAMVY,A1KRK6
	 *
	 * Produce:
	 *
	 * 667.3,ASGIACF:15;14;12;13;9;11;10;16-AGGAMVY:2;6;4;1;3;5-CATVAFG:17-ACGALAY:0-VGGCVAY:26-GCAALAY:19;20-AGGVVCY:8;7-IGGGMSF:22-GVSCVGF:21-SAIFCAG:25;24-LAGCSAF:23-CSGGVVF:18
	 * 679.3,GAPGCLY:35-ACPVSGF:33;29;27;31;32;30;28;34-MGASPAF:37-GCSVPAF:36
	 * 691.3,GPCPTAF:38-GPMPSGF:39;41;40
	 * 715.3,AAMGFGY:42;43-GGCVFAY:64;60;61;59;63;62-GFAMSGF:55-GFAAGMY:50;51;54;53;52-ACGTFAF:46;45-GFMSGAF:58-FAGCVGY:48-FGGAMAY:49-AAYGMGF:44-GFMGAAY:56;57-AVGGCFY:47
	 *
	 *
	 *
	 * @param inputCsv
	 * @param outputGroupedCsv
	 */
	private void startCreateGroupWithAccIds(String inputCsv, String outputGroupedCsv) {
		CsvReader csvReader = new CsvReader(inputCsv, 3);
		Object2IntMap<String> accessionToIdMap = new Object2IntOpenHashMap<>();

		currentAccNum = 0;

		it.unimi.dsi.util.BloomFilter<Void> bloomFilterAccession = it.unimi.dsi.util.BloomFilter.create(2232334);


		csvReader.onRow((CsvRow row) -> {
			double mass = row.getDouble(0);
			String peptide = row.getString(1);
			String accession = row.getString(2);
			if(!accessionToIdMap.containsKey(accession)) {
				accessionToIdMap.put(accession, currentAccNum);
				currentAccNum++;
			}
		});

		log.debug("Accession map size: " + accessionToIdMap.size());


	}

	public static void main(String[] args) {
		JobCsvMassGrouperWithAccIds2 jon = new JobCsvMassGrouperWithAccIds2();

	}

	@Data
	class MassRow {
		String mass;
		List<PeptideAccNums> peptideAccNums = new ArrayList<>();

		public boolean hasMass() {
			return mass != null;
		}
	}

	@Data
	class PeptideAccNums {
		String peptide;
		List<Integer> accNums = new ArrayList<>();

		public void addAccNum(int accNum) {
			accNums.add(accNum);
		}

	}

}

