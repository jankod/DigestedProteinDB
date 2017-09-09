package test.hr.pbf.digestdb.util;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.pbf.digestdb.util.MassRangeMap;

public class MassRangeMapTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
		
	}
private static final Logger log = LoggerFactory.getLogger(MassRangeMapTest.class);

	@Test
	public void test() {
		MassRangeMap map = new MassRangeMap(0.3, 500, 6000);
		byte[] serializeBytes = SerializationUtils.serialize(map);
		
		MassRangeMap deserialize = SerializationUtils.deserialize(serializeBytes);
		
		assertEquals(map, deserialize);
		try {
			File file = new File("./ser.txt");
			log.debug("File {}", file);
			System.out.println(file.getAbsolutePath());
			IOUtils.write(serializeBytes, new FileOutputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
