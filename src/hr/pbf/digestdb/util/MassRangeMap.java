package hr.pbf.digestdb.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class MassRangeMap implements Externalizable {
	private static final Logger log = LoggerFactory.getLogger(MassRangeMap.class);
	
	private static final long serialVersionUID = 1L;

	private float delta;
	private int min;
	private int max;
	private transient TreeRangeMap<Float, String> map;
	private int decimalPlaces;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + decimalPlaces;
		long temp;
		temp = Float.floatToIntBits(delta);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + max;
		result = prime * result + min;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MassRangeMap other = (MassRangeMap) obj;
		if (decimalPlaces != other.decimalPlaces)
			return false;
		if (Float.floatToIntBits(delta) != Float.floatToIntBits(other.delta))
			return false;
		if (max != other.max)
			return false;
		if (min != other.min)
			return false;
		return true;
	}

	/**
	 * 
	 * 
	 * @param delta,
	 *            koja delta se uzima. Kolko decimalnih mjesta ima delta tolko ce ih
	 *            biti i u filename svima.
	 * @param min
	 *            min Da od kojeg se rade filovi
	 * @param max
	 *            max Da od kojeg se rade filovi.
	 */
	public MassRangeMap(float delta, int min, int max) {
		this.delta = delta;
		this.min = min;
		this.max = max;
		this.decimalPlaces = getDecimalPlaces(delta);

		createMap();
	}

	public MassRangeMap() {
	}

	private void createMap() {
		DecimalFormat nf = new DecimalFormat("###.##");
		DecimalFormatSymbols dec = new DecimalFormatSymbols();
		dec.setDecimalSeparator('.');
		dec.setGroupingSeparator(',');
		nf.setDecimalFormatSymbols(dec);
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(decimalPlaces);
		nf.setMinimumFractionDigits(decimalPlaces);
		// DecimalFormat nf = new DecimalFormat();

		map = TreeRangeMap.create();

		for (float i = min; i < max + delta; i = i + delta) {

			float from = BioUtil.roundToDecimals(i, decimalPlaces);
			float to = BioUtil.roundToDecimals(i + delta, decimalPlaces);
			Range<Float> r = Range.closed(from, to);
			// map.put(r, BioUtil.roundToDecimals(r.lowerEndpoint(), decimalPlaces) + "");
			map.put(r, nf.format(r.lowerEndpoint()));
		}
	}

	private int getDecimalPlaces(float d) {
		String text = Float.toString(Math.abs(d));
		int integerPlaces = text.indexOf('.');
		int decimalPlaces = text.length() - integerPlaces - 1;
		return decimalPlaces;
	}

	public TreeRangeMap<Float, String> getMap() {
		return map;
	}

	/**
	 * 
	 * @param mass
	 * @return null ako nista ne nadje, inace file name za tu masu.
	 */
	public String getFileName(float mass) {
		String name = map.get(mass);
		if (name == null) {
			throw new NullPointerException("Not find for mass: " + mass);
		}
		return name;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		Properties p = new Properties();
		p.setProperty("delta", delta + "");
		p.setProperty("max", max + "");
		p.setProperty("min", min + "");
		p.setProperty("decimalPlaces", decimalPlaces + "");
		StringWriter writer = new StringWriter();
		p.store(writer, "Podaci po kojima su distribuirani filovi na disku.");
		out.writeUTF(writer.toString());

		createMap();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		String utf = in.readUTF();
		Properties p = new Properties();
		p.load(new StringReader(utf));
		delta = Float.parseFloat(p.getProperty("delta"));
		max = Integer.parseInt(p.getProperty("max"));
		min = Integer.parseInt(p.getProperty("min"));
		decimalPlaces = Integer.parseInt(p.getProperty("decimalPlaces"));

		// TODO: napravi mapu i decimal places TreeRangeMap<Double, String> map!!!
	}

}
