<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Digested DB</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">

</head>
<body>
<h1>DigestedDB</h1>

<div class="container" x-data="searchData()">
    <div class="row">
        <div class="col">
            <h2>Search for mass </h2>
            <div x-show="dbInfo">
                <p class="mb-1"><strong>Enzyme:</strong> <span x-text="dbInfo.enzyme_name"></span></p>
                <p class="mb-1"><strong>Miss Cleavage:</strong> <span x-text="dbInfo.miss_cleavage"></span></p>
                <p class="mb-1"><strong>Min Peptide Length:</strong> <span x-text="dbInfo.min_peptide_length"></span>
                </p>
                <p class="mb-1"><strong>Max Peptide Length:</strong> <span x-text="dbInfo.max_peptide_length"></span>
                </p>
                <p class="mb-1"><strong>Protein count:</strong> <span x-text="dbInfo.protein_count"></span></p>
                <p class="mb-3"><strong>DB name:</strong> <span x-text="dbInfo.db_name"></span></p>
            </div>
            <div class="row">
                <div class="col">
                    <h3>Search by mass</h3>
                    <form @submit.prevent="search">
                        <div class="mb-3">
                            <label for="mass1" class="form-label">Mass from</label>
                            <input x-model="mass1" type="text" class="form-control" id="mass1" name="mass1" required
                                   value="1500.6">
                        </div>
                        <div class="mb-3">
                            <label for="mass2" class="form-label">Mass to</label>
                            <input x-model="mass2" type="text" class="form-control" id="mass2" name="mass2" required
                                   value="1500.8">
                        </div>
                        <button type="submit" class="btn btn-primary" :disabled="loading">
                            <span x-show="!loading">Search</span>
                            <span x-show="loading" style="display: none;">Loading...</span>
                        </button>
                    </form>
                </div>

                <div class="col">


                    <h3>Search by sequence</h3>
                    <form method="get" @submit.prevent="search">
                        <div class="mb-3">
                            <label for="sequence" class="form-label">Sequence</label>
                            <input x-model="peptideSequence" @input="calculatePeptideMass" type="text" class="form-control" id="sequence" name="sequence" >
                        </div>
                    </form>

                </div>
            </div>


        </div>


    </div>

    <div class="row">
        <div class="col">
            <h2>Results</h2>
            <div class="alert alert-danger" role="alert" x-text="error" x-show="error" style="display: none;"></div>

            <pre x-text="results"></pre>

        </div>
    </div>
</div>


<script>

    function searchData() {
        return {
            calculatedMass: "",
            peptideSequence: "",

            mass1: "1500.6",
            mass2: "1500.8",
            results: "",
            error: "",
            loading: false,
            dbInfo: {},
            async init() {
                try {
                    const response = await fetch('/db-info.php');
                    if (!response.ok) {
                        throw new Error('Failed to fetch database info');
                    }
                    this.dbInfo = await response.json();
                    console.log("dbInfo", this.dbInfo);
                } catch (error) {
                    console.error('Error fetching database info:', error);
                    this.dbInfo = 'Error fetching database info';
                }
            },
            calculatePeptideMass() {
                try {
                    if (this.peptideSequence.trim() === '') {
                        this.calculatedMass = '';
                        return;
                    }

                    const mass = calculateMassWidthH2O(this.peptideSequence.toUpperCase());
                    this.calculatedMass = mass.toFixed(6);
                    this.mass1 = this.calculatedMass;
                    this.mass2 = this.calculatedMass;
                } catch (error) {
                    console.error('Error calculating mass:', error);
                    this.calculatedMass = 'Error: ' + error.message;
                }
            },

            async search() {
                this.error = null;
                this.loading = true;
                try {
                    const response = await fetch(`/search.php?mass1=${this.mass1}&mass2=${this.mass2}`);
                    if (!response.ok) {
                        const errorMessage = await response.text(); // Read error from response
                        throw new Error(`Server error: ${errorMessage}`);
                    }
                    this.results = await response.json();
                    this.results = JSON.stringify(this.results, null, 2);
                } catch (error) {
                    console.error('Error:', error);
                    this.results = `Error: ${error.message}`;
                    this.error = error.message;


                    this.results = [];
                } finally {
                    this.loading = false;
                }
            }
        }
    }

</script>


<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>
<script src="bio-lib.js" defer></script>
<script src="//unpkg.com/alpinejs" defer></script>

</body>
</html>
