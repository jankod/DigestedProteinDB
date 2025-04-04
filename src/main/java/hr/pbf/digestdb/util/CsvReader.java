package hr.pbf.digestdb.util;

import com.google.common.hash.Funnel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.function.Consumer;

public class CsvReader {
	private final String path;
	private final int colonCount;
	boolean skipEmptyLines = true;
	boolean skipHeader = false;
	String separator = ",";

	public CsvReader(String path, int colonCount) {
		this.path = path;
		this.colonCount = colonCount;
	}

	public void onRow(Consumer<CsvRow> consumer) {
		String line = null;
		try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
			if(skipHeader) {
				reader.readLine(); // Skip header
			}
			while((line = reader.readLine()) != null) {
				if(skipEmptyLines && line.trim().isEmpty()) {
					continue;
				}

				CsvRow row = new CsvRow(line.split(separator));
				consumer.accept(row);
			}
		} catch(Exception e) {
			throw new RuntimeException("Error reading CSV file: " + path + " on line: '" + line + "'", e);
		}
	}

	public static class CsvRow {

		private final String[] split;

		public CsvRow(String[] split) {
			this.split = split;
		}

		public int getInt(int index) {
			return Integer.parseInt(split[index]);
		}

		public String getString(int index) {
			return split[index];
		}

		public double getDouble(int index) {
			return Double.parseDouble(split[index]);
		}
	}
}
