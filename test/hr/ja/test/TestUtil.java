package hr.ja.test;

import hr.createnr.util.BioUtil;

public class TestUtil {

	
	public static void main(String[] args) {
		
		
		String res = BioUtil.removeVersionFromAccession("AAK06287.1");
		System.out.println(res);
	}
}
