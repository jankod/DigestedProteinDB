package hr.pbf.digestdb.test.probe;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.opencsv.CSVReaderBuilder;

import hr.pbf.digestdb.util.BioUtil;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.io.csv.CsvReadOptions.CsvReadOptionsBuilder;
import tech.tablesaw.store.StorageManager;

public class ProbaTablesaw {

	static String storeFolderPath = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\saw_store";

	public static void main(String[] args) throws IOException {
		Table t = StorageManager.readTable("C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\saw_store\\mass.saw");
		System.out.println(t.first(3));

	}

	public static void main1(String[] args) throws FileNotFoundException, IOException {

		BufferedReader in = BioUtil.newFileReader(
				"C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\sample_data\\850_000_nr_mass.csv");

		ColumnType[] columns = { ColumnType.DOUBLE, ColumnType.CATEGORY, ColumnType.LONG_INT };
		CsvReadOptionsBuilder build = CsvReadOptions.builder(in, "mass").columnTypes(columns).separator('\t')
				.header(false);
		Table table = Table.read().csv(build);
		table.column(0).setName("mass");
		table.column(1).setName("seq");
		table.column(2).setName("seq_id");

		Column c = table.column(1);
		c.sortAscending();

		StorageManager.saveTable(storeFolderPath, table);

		System.out.println(table.structure().print());

		in.close();
	}
}
