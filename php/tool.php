<?php
const HEAD_TITLE = "Digested Protein DB - tool";
include_once 'lib.php';
?>

<!doctype html>
<html lang="en">
<head>
    <?php include_once "inc/html_head.php"; ?>

</head>
<body>
<!--<h1 class="text-center display-5 fw-bold text-primary shadow p-3 mb-5 bg-body rounded">-->
<!--    Digested Protein DB - tool-->
<!--</h1>-->

<?php include_once "inc/html_navbar.php"; ?>



<div class="container" x-data="init()">
    <?php

    $accession = $_GET["accession"];
    // https://rest.uniprot.org/uniprotkb/A0A085MEL6.fasta
    // https://rest.uniprot.org/uniprotkb/A0A085MEL6?fields=protein_name,lineage,sequence
    //https://rest.uniprot.org/uniprotkb/A0A085MEL6?fields=protein_name,sequence
    // get peptide sequence from uniprot wrebsite
    $url = "https://rest.uniprot.org/uniprotkb/" . $accession;
    $url .= "?fields=protein_name,sequence";


    // Use a cache key that includes the accession to make it unique
    $cacheKey = 'uniprot_' . $accession;

    // Get the JSON data, either from the cache or the URL
    $json = getCachedUniProtData($url, $cacheKey, 3600); // Cache for 1 hour

    //    $json = file_get_contents($url);
    $entry = parseUniProtData($json);
    $sequence = $entry->sequence;
    $proteinName = $entry->proteinName;
    $uniprotErrors = $entry->errors;


    ?>
    <div class="alert alert-danger" x-show="uniprotErrors.length > 0">
        <strong>Error:</strong> <span x-text="uniprotErrors"></span>
    </div>
    <div class="table-responsive">
        <table class="table">
            <tr>
                <td>Accession</td>
                <td>
                    <a href="https://www.uniprot.org/uniprot/<?php echo $accession; ?>" class="btn-link external-link"
                       target="_blank"><?php echo $accession; ?></a>

                </td>
            </tr>
            <tr>
                <td>Protein Name</td>
                <td><span x-text="proteinName"></span></td>
            </tr>
            <tr>
                <td>Sequence</td>
                <td class="align-top">
                    <div class="overflow-x-auto" style="max-width: 100%;">
                        <pre class="mb-0 text-break" style="white-space: pre-wrap; word-wrap: break-word;"
                             x-text="sequence"></pre>
                    </div>
                </td>
            </tr>
        </table>
    </div>

    <h2>Digestion</h2>

    <div class="mb-3">
        <label for="digestEnzyme" class="form-label">Select Enzyme:</label>
        <select class="form-select" id="digestEnzyme" x-model="digestEnzyme" @change="digestSequence()">
            <option value="trypsin">Trypsin</option>
            <option value="chymotrypsin">Chymotrypsin (C-term to F/Y/W, not before P)</option>
            <!-- Add other enzymes here -->
        </select>
    </div>

    <div class="mb-3">
        <label class="form-label">Missed Cleavages:</label>
        <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="missedCleavages" id="missedCleavages0" value="0"
                   x-model="missedCleavages" @change="digestSequence()">
            <label class="form-check-label" for="missedCleavages0">0</label>
        </div>
        <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="missedCleavages" id="missedCleavages1" value="1"
                   x-model="missedCleavages" @change="digestSequence()">
            <label class="form-check-label" for="missedCleavages1">1</label>
        </div>
        <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="missedCleavages" id="missedCleavages2" value="2"
                   x-model="missedCleavages" @change="digestSequence()">
            <label class="form-check-label" for="missedCleavages2">2</label>
        </div>
    </div>


<!--    <button type="button" class="btn btn-primary" @click="digestSequence()">Digest</button>-->

    <div class="table-responsive">
        <table class="table table-striped">
            <thead>
            <tr>
                <th class="" style="width: 1%; white-space: nowrap;">#</th>
                <th style="width: 1%; white-space: nowrap;">Mass (Da)</th>
                <th>Peptide Sequence</th>
            </tr>
            </thead>
            <tbody>
            <template x-for="(peptide, index) in peptides" :key="index">
                <tr>
                    <td x-text="index + 1" style="width: 1%; white-space: nowrap;"></td>
                    <td x-text="calculateMassWidthH2O(peptide).toFixed(4)" style="width: 1%; white-space: nowrap;"></td>
                    <td x-text="peptide"></td>
                </tr>
            </template>
            </tbody>
        </table>
    </div>


</div>

<script>
    function init() {

        return {
            sequence: "<?php echo $sequence; ?>",
            accession: "<?php echo $accession; ?>",
            proteinName: "<?php echo $proteinName; ?>",
            uniprotErrors: "<?php echo $uniprotErrors; ?>",

            digestPeptide: "",
            digestEnzyme: "trypsin",
            missedCleavages: 0,
            peptides: [],
            digestSequence() {
                if (this.digestEnzyme === 'trypsin') {
                    this.peptides = trypsinDigest(this.sequence, parseInt(this.missedCleavages));

                } else if (this.digestEnzyme === 'chymotrypsin') {
                    this.peptides = chymotrypsin1Digest(this.sequence, parseInt(this.missedCleavages));

                } else {
                    // Handle other enzymes
                    this.peptides = [];
                }
            }
            ,
            init() {
                this.digestSequence();
            }
        }
    }
</script>
<script src="bootstrap.bundle.min.js"></script>
<script src="bio-lib.js" defer></script>
<script src="//unpkg.com/alpinejs" defer></script>
</body>
</html>
