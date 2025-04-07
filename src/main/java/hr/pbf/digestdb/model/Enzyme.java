package hr.pbf.digestdb.model;

import java.util.List;

public interface Enzyme {

	List<String> cleavage(String prot, int missedCleavage, int minLength, int maxLength);
}