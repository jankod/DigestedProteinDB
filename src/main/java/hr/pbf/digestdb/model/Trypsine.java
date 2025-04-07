package hr.pbf.digestdb.model;

import hr.pbf.digestdb.util.BioUtil;

import java.util.List;

public class Trypsine implements Enzyme {

	@Override
	public List<String> cleavage(String prot, int missedCleavage, int minLength, int maxLength) {
		if(missedCleavage != 1) {
			throw new IllegalArgumentException("Missed cleavage must be 1 for trypsine.");
		}
		return BioUtil.tripsyn1mc(prot, missedCleavage, maxLength);
	}

	@Override
	public String toString() {
		return "Trypsine";
	}
}

