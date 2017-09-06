package hr.pbf.digestdb.test.proba;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import hr.pbf.digestdb.util.BioUtil;

public class CompressProtein {

	
	public static void main(String[] args) throws IOException {
		
		
		//BiMap<String, String> map = HashBiMap.create(50000);
		byte c = 0;
	//	String[] map = new String[Byte.MAX_VALUE];
		//-128 do 127; == 254
		
		//System.out.println(Byte.MIN_VALUE);
		
		DataInputStream in = BioUtil.newDataInputStream("");
		
		
		while(in.available() > 0) {
			
		}
	}
}
