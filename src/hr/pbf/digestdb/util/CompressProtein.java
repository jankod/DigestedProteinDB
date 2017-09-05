package hr.pbf.digestdb.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CompressProtein {

	
	public static void main(String[] args) {
		
		
		//BiMap<String, String> map = HashBiMap.create(50000);
		byte c = 0;
		String[] map = new String[Byte.MAX_VALUE];
		//-128 do 127; == 254
		
		System.out.println(Byte.MIN_VALUE);
	}
}
