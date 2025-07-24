package hr.pbf.digestdb.experiments;

import java.util.function.Predicate;

import lombok.Getter;

@Getter
public enum PTM {
    CARBAMIDOMETHYL("Carbamidomethyl", 57.0215, "C", Type.FIXED, Frequency.VERY_COMMON,
          peptide -> peptide.indexOf('C') >= 0),

    OXIDATION("Oxidation", 15.9949, "M,H,W", Type.VARIABLE, Frequency.VERY_COMMON,
          peptide -> peptide.chars().anyMatch(ch -> ch == 'M' || ch == 'H' || ch == 'W')),

    DEAMIDATION("Deamidation", 0.9840, "N,Q", Type.VARIABLE, Frequency.COMMON,
          peptide -> peptide.chars().anyMatch(ch -> ch == 'N' || ch == 'Q')),

    PHOSPHORYLATION("Phosphorylation", 79.9663, "S,T,Y", Type.VARIABLE, Frequency.MODERATE,
          peptide -> peptide.chars().anyMatch(ch -> ch == 'S' || ch == 'T' || ch == 'Y')),

    ACETYLATION("Acetylation", 42.0106, "K,N-term", Type.VARIABLE, Frequency.MODERATE,
          peptide -> peptide.indexOf('K') >= 0),

    FORMYLATION("Formylation", 27.9949, "K,N-term", Type.VARIABLE, Frequency.RARE,
          peptide -> peptide.indexOf('K') >= 0),

    METHYLATION("Methylation", 14.0157, "K,R", Type.VARIABLE, Frequency.MODERATE,
          peptide -> peptide.chars().anyMatch(ch -> ch == 'K' || ch == 'R')),

    DIMETHYLATION("Dimethylation", 28.0313, "K,R", Type.VARIABLE, Frequency.MODERATE,
          peptide -> peptide.chars().anyMatch(ch -> ch == 'K' || ch == 'R')),

    TRIMETHYLATION("Trimethylation", 42.0469, "K,R", Type.VARIABLE, Frequency.RARE,
          peptide -> peptide.chars().anyMatch(ch -> ch == 'K' || ch == 'R')),

    SULFATION("Sulfation", 79.9568, "Y", Type.VARIABLE, Frequency.VERY_RARE,
          peptide -> peptide.indexOf('Y') >= 0);

    private final String name;
    private final double deltaMass;
    private final String targetAminoAcids;
    private final Type type;
    private final Frequency frequency;
    private final Predicate<String> applicability;

    PTM(String name,
        double deltaMass,
        String targetAminoAcids,
        Type type,
        Frequency frequency,
        Predicate<String> applicability) {
        this.name = name;
        this.deltaMass = deltaMass;
        this.targetAminoAcids = targetAminoAcids;
        this.type = type;
        this.frequency = frequency;
        this.applicability = applicability;
    }

    /**
     * Returns true if this PTM can occur on the given peptide sequence.
     */
    public boolean canApplyTo(String peptide) {
        return applicability.test(peptide);
    }

    public enum Type {FIXED, VARIABLE}

    public enum Frequency {VERY_COMMON, COMMON, MODERATE, RARE, VERY_RARE}
}
