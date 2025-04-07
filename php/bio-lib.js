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

/**
 * Digests a peptide sequence using chymotrypsin cleavage rules.
 * Cleaves after F, Y, W (C-terminus), unless immediately followed by P.
 * Supports a specified number of missed cleavage sites.
 * All characters in the sequence must be uppercase English alphabet letters.
 *
 * @param {string} sequence Input peptide sequence (uppercase only).
 * @param {number} [missedCleavages=0] Maximum allowed number of missed cleavage sites (0 or more).
 * @param {number} [minLength=6] Minimum length of peptides to include in the result.
 * @param {number} [maxLength=30] Maximum length of peptides to include in the result.
 * @returns {string[]} Array of resulting peptides, sorted by length.
 * @throws {TypeError} If the sequence is not a string.
 * @throws {Error} If the sequence contains non-uppercase characters.
 * @throws {RangeError} If the parameters `missedCleavages`, `minLength`, `maxLength` are invalid.
 */
function chymotrypsin1Digest(sequence, missedCleavages = 0, minLength = 6, maxLength = 30) {
    if (typeof sequence !== 'string') {
        throw new TypeError("Sequence must be a string.");
    }
    // Allows empty string, but checks for uppercase if not empty
    if (sequence !== "" && !/^[A-Z]+$/.test(sequence)) {
        throw new Error("Sequence must contain only uppercase letters (A-Z).");
    }
    if (typeof missedCleavages !== 'number' || !Number.isInteger(missedCleavages) || missedCleavages < 0) {
        throw new RangeError("Missed cleavages must be a non-negative integer.");
    }
    if (typeof minLength !== 'number' || !Number.isInteger(minLength) || minLength < 0) {
        throw new RangeError("Minimum length must be a non-negative integer.");
    }
    if (typeof maxLength !== 'number' || !Number.isInteger(maxLength) || maxLength < 0) {
        throw new RangeError("Maximum length must be a non-negative integer.");
    }
    if (minLength > maxLength) {
        throw new RangeError("Minimum length cannot be greater than maximum length.");
    }

    if (sequence === "") {
        return [];
    }

    // --- Finding Cleavage Sites ---
    const chymotrypsinRegex = /([FYW])(?!P)/g; // Cleaves AFTER F, Y, W if NOT followed by P
    const cleaveSites = [];
    let match;
    // Regex looks for F/Y/W NOT followed by P. match.index is the index of F/Y/W.
    // The cleavage site is AFTER that character, hence `match.index + 1`.
    while ((match = chymotrypsinRegex.exec(sequence)) !== null) {
        cleaveSites.push(match.index + 1);
    }

    // --- Defining Peptide Boundaries ---
    // Boundaries are the start (0), all cleavage sites, and the end of the sequence.
    const boundaries = [0, ...cleaveSites, sequence.length];
    // Remove duplicates if they exist (e.g., cleavage at the very end of the sequence)
    // and ensure boundaries are sorted (though they should be already)
    const uniqueSortedBoundaries = [...new Set(boundaries)].sort((a, b) => a - b);
    // Filter out boundaries that would create zero-length peptides (e.g., if there are duplicate boundaries)
    const effectiveBoundaries = uniqueSortedBoundaries.filter((val, idx, arr) => idx === 0 || val > arr[idx - 1]);


    // --- Generating Peptides ---
    const peptidesSet = new Set(); // Use a Set for automatic duplicate removal

    // Iterate through all possible start boundaries
    // Loop goes up to the second-to-last boundary, as each boundary defines a start
    for (let i = 0; i < effectiveBoundaries.length - 1; i++) {
        // For each start boundary, iterate through possible end boundaries
        // considering the number of missed cleavages (k)
        // k=0 means no missed cleavages (ends at the next boundary)
        // k=1 means 1 missed cleavage (ends at the second next boundary), etc.
        for (let k = 0; k <= missedCleavages; k++) {
            const startIndex = i;
            const endIndex = i + k + 1; // Index of the end boundary in `effectiveBoundaries`

            // Check if this end boundary exists
            if (endIndex < effectiveBoundaries.length) {
                const startPosition = effectiveBoundaries[startIndex];
                const endPosition = effectiveBoundaries[endIndex];
                // Add peptide to the Set (substring does not include the character at endPosition)
                peptidesSet.add(sequence.substring(startPosition, endPosition));
            } else {
                // If we have exceeded the number of available boundaries for this `k`,
                // there's no point continuing for this starting point (`startIndex`)
                break;
            }
        }
    }

    // --- Filtering and Sorting ---
    // Convert Set to array
    let peptides = Array.from(peptidesSet);

    // Filter by length
    peptides = peptides.filter(peptide => {
        const len = peptide.length;
        return len >= minLength && len <= maxLength;
    });

    // Sort by length (optional, could also sort by mass or alphabetically)
    peptides.sort((a, b) => a.length - b.length);

    return peptides;
}



function trypsinDigest(sequence, missedCleavages = 0) {
    if (missedCleavages < 0 || missedCleavages > 2) {
        throw new Error("Missed cleavages must be 0, 1, or 2.");
    }

    const trypsinRegex = /([KR](?!P))/g;
    let peptides = [];
    let cleaveSites = [];
    let match;

    // Find all potential cleavage sites
    while ((match = trypsinRegex.exec(sequence)) !== null) {
        cleaveSites.push(match.index + 1); // Store the index after K or R
    }

    if (cleaveSites.length === 0) {
        return [sequence]; // No cleavage sites, return the original sequence
    }

    // Generate peptides based on missed cleavages
    for (let i = 0; i < cleaveSites.length + 1 - missedCleavages; i++) {
        let start = (i === 0) ? 0 : cleaveSites[i - 1];
        let end;

        if (i + missedCleavages < cleaveSites.length) {
            end = cleaveSites[i + missedCleavages];
        } else {
            end = sequence.length;
        }
        peptides.push(sequence.substring(start, end));
    }

    // sort peptides by mass
    peptides = peptides.sort((a, b) => {
        return calculateMassWidthH2O(a) - calculateMassWidthH2O(b);
    });

    // remove small peptides
    peptides = peptides.filter(peptide => {
        return peptide.length >= 6 && peptide.length <= 30;
    });

    return peptides;
}

if (typeof module !== 'undefined' && module.exports) {
    // Node.js or CommonJS
    module.exports = {
        chymotrypsin1Digest,
        trypsinDigest
    };
}
