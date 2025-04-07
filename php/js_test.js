//import {chymotrypsin1Digest} from './bio-lib.js';
let chymotrypsin1Digest;

const bioLib = require('./bio-lib.js');
const chymotrypsinDigestCorrected = bioLib.chymotrypsin1Digest;

const protein = 'MKVRLTDCTACRWSKIAATLPGRTDNEVKNYWHAHLKKRTVKNQNTLVLKEKCSGSTSESEGSQTNKEMEAKTVVSYTPSNLILESTPLSPETSCSELSNLSTDFAPKLPVSAGTNWNNIAEDVLSSVPTFDESIGDFWTEPFVAGSASDQDNFPGLSFYQEEPFVSYYDDGMDFYYEMMQELPGNN';
const peptides = [
    'TPSNLILESTPLSPETSCSELSNLSTDF',
    'APKLPVSAGTNWNNIAEDVLSSVPTF',
    'NNIAEDVLSSVPTFDESIGDFW',
    'VAGSASDQDNFPGLSFYQEEPF',
    'WTEPFVAGSASDQDNFPGLSF',
    'NNIAEDVLSSVPTFDESIGDF',
    'TEPFVAGSASDQDNFPGLSFY',
    'SKIAATLPGRTDNEVKNYW',
    'TEPFVAGSASDQDNFPGLSF',
    'SKIAATLPGRTDNEVKNY',
    'VAGSASDQDNFPGLSFY',
    'VAGSASDQDNFPGLSF',
    'NNIAEDVLSSVPTF',
    'YYEMMQELPGNN',
    'DESIGDFWTEPF',
    'YEMMQELPGNN',
    'APKLPVSAGTNW',
    'VSYYDDGMDF',
    'EMMQELPGNN',
    'YQEEPFVSY',
    'QEEPFVSYY',
    'YDDGMDFY',
    'DDGMDFYY',
    'QEEPFVSY',
    'DESIGDFW',
    'YDDGMDF',
    'DDGMDFY',
    'YQEEPF',
    'DESIGDF',
    'DDGMDF'
];

const missedCleavages = 2;
const result = bioLib.chymotrypsin1Digest(protein, missedCleavages);
console.log(result);


peptides.forEach((peptide, index) => {
    let contain = result.find(peptideResult => {
        return peptide === peptideResult;
    });
    if (contain === undefined) {
        throw new Error(`Peptide ${peptide} not found in result.`);
    }
});

// --- Example Usage ---
const sequence1 = "ABCYWDEFWGHP"; // Cleave after Y(3), W(7). P after W(10) prevents cleavage.
const sequence2 = "ARNDCEQGHILKMFPSTWYV"; // Many sites, some followed by P
const sequence3 = "AEFAEG"; // No P
const sequence4 = "PPPPPP"; // No cleavage sites
const sequence5 = ""; // Empty

console.log(`Sequence: "${sequence1}"`);
console.log("mc=0:", chymotrypsinDigestCorrected(sequence1, 0, 1, 100));
console.log("mc=1:", chymotrypsinDigestCorrected(sequence1, 1, 1, 100));
console.log("mc=1, len 4-10:", chymotrypsinDigestCorrected(sequence1, 1, 4, 10));
console.log("---");

console.log(`Sequence: "${sequence2}"`);
console.log("mc=0:", chymotrypsinDigestCorrected(sequence2, 0, 3, 10));
console.log("mc=1:", chymotrypsinDigestCorrected(sequence2, 1, 3, 10));
console.log("---");

console.log(`Sequence: "${sequence3}"`);
console.log("mc=0:", chymotrypsinDigestCorrected(sequence3, 0, 1, 100));
console.log("mc=1:", chymotrypsinDigestCorrected(sequence3, 1, 1, 100));
console.log("---");

console.log(`Sequence: "${sequence4}"`);
console.log("mc=0:", chymotrypsinDigestCorrected(sequence4, 0, 1, 100));
console.log("---");

console.log(`Sequence: "${sequence5}"`);
console.log("mc=0:", chymotrypsinDigestCorrected(sequence5, 0, 1, 100));
console.log("---");

try {
    console.log("Test lowercase:");
    chymotrypsinDigestCorrected("abc", 0);
} catch (e) {
    console.error(e.message); // Will output the English error message
}

try {
    console.log("Test min > max:");
    chymotrypsinDigestCorrected("ABC", 0, 5, 3);
} catch (e) {
    console.error(e.message); // Will output the English error message
}

