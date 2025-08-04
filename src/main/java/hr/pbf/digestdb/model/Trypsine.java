package hr.pbf.digestdb.model;

import hr.pbf.digestdb.util.BioUtil;

import java.util.List;

public class Trypsine implements Enzyme {

    @Override
    public List<String> cleavage(String prot, int missedCleavage, int minLength, int maxLength) {
        if (missedCleavage == 2) {
            return BioUtil.tripsyn2mc(prot, minLength, maxLength);
        }
        if (missedCleavage == 1)
            return BioUtil.tripsyn1mc(prot, minLength, maxLength);

        throw new IllegalArgumentException("Missed cleavage must be 1 or 2 for trypsine.");
    }

    @Override
    public String toString() {
        return "Trypsine";
    }
}

