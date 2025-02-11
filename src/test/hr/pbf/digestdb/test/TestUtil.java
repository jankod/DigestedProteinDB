package hr.pbf.digestdb.test;

import org.apache.commons.lang3.StringUtils;

import hr.pbf.digestdb.util.BioUtil;

public class TestUtil {

	public static void main(String[] args) {
		String line = "AC   Q16653; O00713; O00714; O00715; Q13054; Q13055; Q14855; Q92891; ";
		// line = "AC Q6GZX4;";
		line = line.substring(5);
		String[] split = StringUtils.split(line, ";");
		for (String ac : split) {
			ac = ac.trim();
			if (StringUtils.isBlank(ac)) {
				//System.out.println("Empty");
				continue;
			}
			System.out.println(ac.trim());
		}
	}

	public static void main2(String[] args) {

		String res = BioUtil.removeVersionFromAccession("AAK06287.1");
		System.out.println(res);
	}
}
