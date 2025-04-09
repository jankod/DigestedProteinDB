package hr.pbf.digestdb.db;

import hr.pbf.digestdb.util.MyUtil;

public class Test1 {

	public static void main(String[] args) {
		double mass = 500.0f;

		while(mass < 3000.0f) {
			System.out.println("Mass: " + mass + " " + MyUtil.toInt(mass));
			mass += 0.001F;
			mass = MyUtil.roundTo4(mass);
		}
	}
}
