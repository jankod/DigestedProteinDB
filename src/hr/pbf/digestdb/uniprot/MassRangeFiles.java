package hr.pbf.digestdb.uniprot;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

import hr.pbf.digestdb.util.BioUtil;
import hr.pbf.digestdb.util.MassRangeMap;

public class MassRangeFiles {

	private float delta = 0.3f;
	private int fromMass = 500;
	private int toMass = 6000;
	private final MassRangeMap massRangeMap;
	private static HashMap<String, DataOutput> massStreamMap = new HashMap<>();

	private String fileExtension;
	private String dir;

	/**
	 * Round float mass before and after save peptide mass
	 */
	public static final int ROUND_FLOAT_MASS = 2;

	
	
	/**
	 * 
	 * @param from
	 *            from mass
	 * @param to
	 *            to mass
	 * @param delta
	 *            delta mass
	 * @param fileExtension
	 *            file postfix creatate
	 * @param dir
	 *            directory created for store files
	 */
	public MassRangeFiles(int from, int to, float delta, String fileExtension, String dir) {
		this.fromMass = from;
		this.toMass = to;
		this.delta = delta;
		this.fileExtension = fileExtension;
		this.dir = dir;
		this.massRangeMap = new MassRangeMap(delta, fromMass, toMass);
	}

	public int getFromMass() {
		return fromMass;
	}

	public int getToMass() {
		return toMass;
	}

	public void closeAll() {
		for (DataOutput out : massStreamMap.values()) {
			if (out instanceof MyDataOutputStream) {
				IOUtils.closeQuietly((MyDataOutputStream) out);
				continue;
			}
			if (out instanceof DataOutputStream) {
				IOUtils.closeQuietly((DataOutputStream) out);
			}

		}
	}

	public DataOutput getOuput(float mass) throws FileNotFoundException {
		String fileName = massRangeMap.getFileName(mass);
		DataOutput out;
		if (massStreamMap.containsKey(fileName)) {
			out = massStreamMap.get(fileName);
		} else {
			out = BioUtil.newDataOutputStream(dir + File.separator + fileName + "." + this.fileExtension, 8192);
			massStreamMap.put(fileName, out);
		}
		return out;
	}

}
