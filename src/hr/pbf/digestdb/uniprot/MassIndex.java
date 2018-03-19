package hr.pbf.digestdb.uniprot;

import java.io.BufferedInputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Index mass (float) => how peptides (ineger) as TreeMap
 * @author tag
 *
 */
public class MassIndex implements Externalizable {

	public static final long serialVersionUID = 5413367467084471654L;

	private TreeMap<Float, Integer> map;

	public MassIndex() {

	}
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		MassIndex massindex = MassIndex.load("C:\\Eclipse\\OxygenWorkspace\\DigestedProteinDB\\misc\\trembl.leveldb.index.compact");
		System.out.println("Dobio "+ massindex.getMap().size());
		
		
		toMassIndexFast(massindex);
	}
	
	
	

	private static void toMassIndexFast(MassIndex massindex) {
		TreeMap<Float, Integer> m = massindex.getMap();
		MassPeptideArrayIndex index = new MassPeptideArrayIndex(m.size());
	}

	public static final MassIndex load(String path) throws IOException, ClassNotFoundException {
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)))) {
			ObjectInputStream oin = new ObjectInputStream(in);
			return (MassIndex) oin.readObject();
		}
	}

	public MassIndex(TreeMap<Float, Integer> map) {
		this.map = map;
	}

	public TreeMap<Float, Integer> getMap() {
		return map;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		if (map == null) {
			map = new TreeMap<>();
		}
		out.writeInt(map.size());
		Set<Entry<Float, Integer>> keySet = map.entrySet();
		for (Entry<Float, Integer> entry : keySet) {
			out.writeFloat(entry.getKey());
			out.writeInt(entry.getValue());
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int how = in.readInt();
		map = new TreeMap<>();
		for (int i = 0; i < how; i++) {
			map.put(in.readFloat(), in.readInt());
		}

	}
}
