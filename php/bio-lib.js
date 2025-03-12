function calculateMassWidthH2O(peptide) {
    const H2O = 18.0105646;
    let h = 0;

    for (let i = 0; i < peptide.length; i++) {
        h += getMassFromAA(peptide[i]);
    }

    return h + H2O;
}

function getMassFromAA(aa) {
    switch (aa) {
        case 'G':
            return 57.021463724;
        case 'A':
            return 71.03711379;
        case 'S':
            return 87.03202841;
        case 'P':
            return 97.05276385;
        case 'V':
            return 99.06841392;
        case 'T':
            return 101.04767847;
        case 'C':
            return 103.00918448;
        case 'I':
        case 'J':
        case 'L':
            return 113.08406398;
        case 'N':
            return 114.04292745;
        case 'D':
            return 115.02694303;
        case 'Q':
            return 128.05857751;
        case 'K':
            return 128.09496302;
        case 'E':
            return 129.0425931;
        case 'M':
            return 131.04048461;
        case 'H':
            return 137.05891186;
        case 'F':
            return 147.0684139162;
        case 'R':
            return 156.10111103;
        case 'Y':
            return 163.06332854;
        case 'W':
            return 186.07931295;
        case 'U':
            return 150.95363559;
        case 'O':
            return 114.0793129535;
        default:
            throw new Error("Wrong AA '" + aa + "'");
    }
}