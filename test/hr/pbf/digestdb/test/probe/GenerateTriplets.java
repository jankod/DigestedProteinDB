package hr.pbf.digestdb.test.probe;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.spi.CharsetProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;

import com.google.common.collect.Maps;

public class GenerateTriplets {

	public static void main(String[] args) throws IOException {
		String path = "C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\statis_result_3plet_all.txt";
		// od 22 linije..args
		Stream<String> lines = Files.lines(Paths.get(path), StandardCharsets.US_ASCII);
		// 256 znakova imam, od 65 pocinje A do 90 Z

		int count = 0;
		
		lines.skip(22).limit(500).filter(t -> {
			
			return true;
		}).forEach(t -> {
			// System.out.println(t);
			if (count > 256) {

				return;
			}
			if (65 < count && count > 90) {
				// to su
				return;
			}
		});

		for (int c = 32; c < 128; c++) {
			System.out.println(c + ": " + (char) c);
		}
		Map<Byte, String> map = Maps.newHashMap();
		map.put((byte) 0, "AAA");

	}
}
