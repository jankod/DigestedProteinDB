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

    if (isset($_GET["accession"])) {
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
        if ($json != false) {
            //    $json = file_get_contents($url);
            $entry = parseUniProtData($json);
            $sequence = $entry->sequence;
            $proteinName = $entry->proteinName;
            $uniprotErrors = $entry->errors;
        } else {
            $sequence = '';
            $proteinName = '';
            $uniprotErrors = 'Error fetching data from UniProt';
            $accession = '';
        }
    } else {
        $sequence = '';
        $proteinName = '';
        $uniprotErrors = null;
        $accession = '';
    }


    ?>


    <div class="alert alert-info mt-3">
        <h4 class="alert-heading">Protein Digestion Analysis Tool</h4>
        <p>
            This tool enables users to retrieve protein sequences from the UniProt database using accession numbers and perform in silico digestion using trypsin and chymotrypsin enzymes. It is designed to assist researchers in proteomics by providing insights into potential peptide fragments resulting from enzymatic digestion.
        </p>
        <p>
            <strong>How to use:</strong>
        <ul>
            <li>Enter a valid UniProt accession number in the input field below.</li>
            <li>Click on <strong>Search</strong> to fetch the corresponding protein sequence.</li>
            <li>The tool will display the original sequence along with the predicted cleavage sites and resulting peptide fragments for both trypsin and chymotrypsin.</li>
        </ul>
        For more information on UniProt accession numbers, visit the <a href="https://www.uniprot.org/" target="_blank">UniProt website</a>.

    </div>

    <div class="alert alert-danger hiding" x-show="uniprotErrors.length > 0" style="display: none;">
        <strong>Error:</strong> <span x-text="uniprotErrors"></span>
    </div>


    <?php if ($accession === ""):     ?>
        <div>
            <form class="mb-3" method="get" action="tool.php">
                <div class="input-group">
                    <input type="text" class="form-control" name="accession" placeholder="Enter UniProt Accession"
                           aria-label="Enter UniProt Accession" x-model="accession">
                    <button class="btn btn-primary" type="submit">Search</button>
                </div>
            </form>

        </div>

    <?php else: ?>


        <div class="table-responsive">
            <table class="table">
                <tr>
                    <td>Accession</td>
                    <td>
                        <a href="https://www.uniprot.org/uniprot/<?php echo $accession; ?>"
                           class="btn-link external-link"
                           target="_blank">Uniprot <?php echo $accession; ?></a>

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
    <?php endif; ?>

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


    <?php include_once "inc/html_footer.php"; ?>


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
                console.log("seq", this.sequence);
                if(this.sequence.trim() === "") {
                    this.peptides =[];
                    console.log("No sequence to digest");
                    return;
                }
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
