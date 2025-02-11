package hr.pbf.digestdb.uniprot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import hr.pbf.digestdb.uniprot.UniprotCSVformat.CallbackReadLine;
import hr.pbf.digestdb.uniprot.UniprotModel.AccTax;
import hr.pbf.digestdb.uniprot.UniprotModel.PeptideMassAccTaxList;
import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.BiteUtil;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;

public class A5_UniprotCSVtoFormat3 {
	private static final Logger log = LoggerFactory.getLogger(A5_UniprotCSVtoFormat3.class);

//	static String pathCsv = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_test.csv";
//	static String pathDb = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_text_uncompress.db";
//	static String pathIndex = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_test.index";

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		String pathCsv = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_test.csv";
		String pathDb = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_text_uncompress.db";
		String pathIndex = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl_for_test.index";
		 createDBNew(pathCsv, pathDb, pathIndex);
		
	}

	public static void createDBNew(String pathCsv, String pathDb, String pathIndex) throws UnsupportedEncodingException, IOException {
		int numEntry = 13_353_898;
		UniprotFormat3Creator creator = new UniprotFormat3Creator(pathDb, pathIndex, numEntry);
		UniprotCSVformat csv = new UniprotCSVformat(pathCsv);
		csv.readLines(new CallbackReadLine() {

			@Override
			public void readedOne(PeptideMassAccTaxList result) {
				try {
					creator.putNext(result.getMass(), result.getPeptide(), result.getAccTaxs());
				} catch (IOException e) {
					ExceptionUtils.rethrow(e);
				}
			}

		});
		creator.finish();

	}

}
