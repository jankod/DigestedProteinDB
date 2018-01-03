package hr.pbf.digestdb.test.probe;

import org.apache.commons.math3.stat.Frequency;

public class MathFrequency {

	public static void main(String[] args) {
		Frequency f = new Frequency();
		f.addValue("pero");
		f.addValue("pero1");
		f.addValue("pero22");
		f.addValue("pero22");
		System.out.println(f);
	}
}
