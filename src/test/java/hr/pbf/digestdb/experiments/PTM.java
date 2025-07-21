package hr.pbf.digestdb.experiments;

import lombok.Getter;

@Getter
public enum PTM {
    CARBAMIDOMETHYL("Carbamidomethyl", 57.0215, "C", Type.FIXED, Frequency.VERY_COMMON),
    OXIDATION("Oxidation", 15.9949, "M,H,W", Type.VARIABLE, Frequency.VERY_COMMON),
    DEAMIDATION("Deamidation", 0.9840, "N,Q", Type.VARIABLE, Frequency.COMMON),
    PHOSPHORYLATION("Phosphorylation", 79.9663, "S,T,Y", Type.VARIABLE, Frequency.MODERATE),
    ACETYLATION("Acetylation", 42.0106, "K,N-term", Type.VARIABLE, Frequency.MODERATE),
    FORMYLATION("Formylation", 27.9949, "K,N-term", Type.VARIABLE, Frequency.RARE),
    METHYLATION("Methylation", 14.0157, "K,R", Type.VARIABLE, Frequency.MODERATE),
    DIMETHYLATION("Dimethylation", 28.0313, "K,R", Type.VARIABLE, Frequency.MODERATE),
    TRIMETHYLATION("Trimethylation", 42.0469, "K,R", Type.VARIABLE, Frequency.RARE),
    SULFATION("Sulfation", 79.9568, "Y", Type.VARIABLE, Frequency.VERY_RARE);

    private final String name;
    private final double deltaMass;
    private final String targetAminoAcids;
    private final Type type;
    private final Frequency frequency;

    PTM(String name, double deltaMass, String targetAminoAcids, Type type, Frequency frequency) {
        this.name = name;
        this.deltaMass = deltaMass;
        this.targetAminoAcids = targetAminoAcids;
        this.type = type;
        this.frequency = frequency;
    }

    public enum Type {
        FIXED,
        VARIABLE
    }

    public enum Frequency {
        VERY_COMMON,
        COMMON,
        MODERATE,
        RARE,
        VERY_RARE
    }
}
