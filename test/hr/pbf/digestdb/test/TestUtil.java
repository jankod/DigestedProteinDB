package hr.pbf.digestdb.test;

import hr.pbf.digestdb.util.BioUtil;

public class TestUtil {

	
	public static void main(String[] args) {
		
		
		String res = BioUtil.removeVersionFromAccession("AAK06287.1");
		System.out.println(res);
	}
}
