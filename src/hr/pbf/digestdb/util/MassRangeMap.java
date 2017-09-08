package hr.pbf.digestdb.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;

/**
 * Mapira range mase na stringove po kojima su slozeni kompresirane baze
 * podataka.
 * 
 * 
 * @author tag
 *
 */
public class MassRangeMap {

	private double delta;
	private int min;
	private int max;
	private TreeRangeMap<Double, String> map;
	private int decimalPlaces;

	/**
	 * 
	 * 
	 * @param delta,
	 *            koja delta se uzima. Kolko decimalnih mjesta ima delta tolko ce ih
	 *            biti i u filename svima.
	 * @param min
	 *            min Da od kojeg se rade filovi
	 * @param max
	 *            max Da od kojeg se rrade filovi.
	 */
	public MassRangeMap(double delta, int min, int max) {
		this.delta = delta;
		this.min = min;
		this.max = max;
		decimalPlaces = getDecimalPlaces(delta);

		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(decimalPlaces);
		nf.setMinimumFractionDigits(decimalPlaces);
		// DecimalFormat nf = new DecimalFormat();

		map = TreeRangeMap.create();

		for (double i = min; i < max + delta; i = i + delta) {

			double from = BioUtil.roundToDecimals(i, decimalPlaces);
			double to = BioUtil.roundToDecimals(i + delta, decimalPlaces);
			Range<Double> r = Range.closed(from, to);
			// map.put(r, BioUtil.roundToDecimals(r.lowerEndpoint(), decimalPlaces) + "");
			map.put(r, nf.format(r.lowerEndpoint()));
		}
	}

	private int getDecimalPlaces(double d) {
		String text = Double.toString(Math.abs(d));
		int integerPlaces = text.indexOf('.');
		int decimalPlaces = text.length() - integerPlaces - 1;
		return decimalPlaces;
	}

	public TreeRangeMap<Double, String> getMap() {
		return map;
	}

	/**
	 * 
	 * @param mass
	 * @return null ako nista ne nadje, inace file name za tu masu.
	 */
	public String getFileName(double mass) {
		String name = map.get(mass);
		return name;
	}

}
