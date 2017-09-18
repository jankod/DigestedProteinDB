package hr.pbf.digestdb.test.probe;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;

import hr.pbf.digestdb.util.BioUtil;

public class MassDataFiles {

	private double delta;
	private RangeSet<Float> ranges;

	public MassDataFiles(float delta, int min, int max) {

		this.delta = delta;

		ranges = TreeRangeSet.create();

		for (float i = min; i <= max; i = i + delta) {

			float start = i;
			float stop = delta + i;
			System.out.println(start + " " + stop);
			ranges.add(Range.closedOpen(start, stop));
		}
	}

	public RangeSet<Float> getRanges() {
		return ranges;
	}

	public static void main(String[] args) {
		// MassDataFiles m = new MassDataFiles(0.3F, 500, 6000);

		// System.out.println(m.getRanges());
		RangeMap<Double, Object> map = TreeRangeMap.create();
		int c = 0;
		for (double i = 500; i < 6000; i += 0.3) {
			i = BioUtil.roundToDecimals(i, 1);
			map.put(Range.closedOpen(i, i+0.3d), i+"");
//			System.out.println(i);
			c++;
//			System.out.println(c);
			
			
		}
		
		for (double i = 500; i < 530.4; i = i + 0.00001) {
			Object object = map.get(i);
			if(object == null) {
				System.out.println("Nisam nasao za "+ i);
			}
		}
	}

	public void openFilePath(double mass) {

	}

}
