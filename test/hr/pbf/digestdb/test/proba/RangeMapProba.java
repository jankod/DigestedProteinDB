package hr.pbf.digestdb.test.proba;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

public class RangeMapProba {

	
	public static void main(String[] args) {
		RangeMap<Float, String> rangeMap = TreeRangeMap.create();
		rangeMap.put(Range.closed(0.0f, 0.3f), "0    : 0.3");
		rangeMap.put(Range.closed(0.3f, 0.6f), "0.3  : 0.6");
		
		System.out.println(rangeMap.get(0.31f));
	}
	
}
