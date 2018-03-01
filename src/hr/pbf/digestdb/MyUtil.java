package hr.pbf.digestdb;

import java.util.ArrayList;
import java.util.Arrays;

public class MyUtil {

	public static boolean argsFirstElementContain(String search, String[] args) {
		if (args.length >= 1) {
			if (search.equals(args[0].trim())) {
				return true;
			}
		}
		return false;
	}

	public static String[] argsMinusFirstElement(String[] args) {
		ArrayList<String> s = new ArrayList<>(Arrays.asList(args));
		s.remove(0);
		return s.toArray(new String[args.length - 1]);
	}

}
