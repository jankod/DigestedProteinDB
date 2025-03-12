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
<h1 class="text-center">DigestedDB</h1>

<div class="container" x-data="searchData()">
    <div class="row">
        <div class="col">

            <div class="container">
                <div class="row align-items-start">
                    <div class="col">
                        <h3>Database info </h3>
                        <div x-show="dbInfo">
                            <p class="mb-1"><strong>Enzyme:</strong> <span x-text="dbInfo.enzyme_name"></span></p>
                            <p class="mb-1"><strong>Miss Cleavage:</strong> <span x-text="dbInfo.miss_cleavage"></span>
                            </p>
                            <p class="mb-1"><strong>Min Peptide Length:</strong> <span
                                        x-text="dbInfo.min_peptide_length"></span>
                            </p>
                            <p class="mb-1"><strong>Max Peptide Length:</strong> <span
                                        x-text="dbInfo.max_peptide_length"></span>
                            </p>
                            <p class="mb-1"><strong>Protein count:</strong> <span x-text="dbInfo.protein_count"></span>
                            </p>
                            <p class="mb-1"><strong>DB name:</strong> <span x-text="dbInfo.db_name"></span></p>
                            <p class="mb-3"><strong>DB size:</strong> <span x-text="dbInfo.db_size"></span></p>
                        </div>
                    </div>


                </div>
            </div>


            <div class="row">
                <div class="col">
                    <h3>Search by mass</h3>
                    <form @submit.prevent="searchNew">
                        <div class="mb-3">
                            <label for="mass1" class="form-label">Mass from (Da)</label>
                            <input x-model="mass1" type="text" class="form-control" id="mass1" name="mass1" required
                                   value="1500.6">
                        </div>
                        <div class="mb-3">
                            <label for="mass2" class="form-label">Mass to (Da)</label>
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
                            <input x-model="peptideSequence" @input="calculatePeptideMass" type="text"
                                   class="form-control" id="sequence" name="sequence">
                        </div>
                    </form>

                </div>
            </div>
        </div>


    </div>

    <!-- Improved Pagination Section -->
    <div class="row mt-4 mb-4" x-show="results && totalPages > 0">
        <div class="col">
            <div class="card shadow-sm">
                <div class="card-body">
                    <div class="d-flex flex-column flex-md-row justify-content-between align-items-center gap-3">
                        <nav aria-label="Page navigation">
                            <ul class="pagination m-0">
                                <li class="page-item" :class="{ 'disabled': currentPage <= 1 }">
                                    <a class="page-link" href="#"
                                       @click.prevent="currentPage > 1 && goToPage(currentPage - 1)">&laquo;</a>
                                </li>

                                <template x-for="p in getPaginationRange()" :key="p">
                                    <li class="page-item" :class="{ 'active': currentPage === p }">
                                        <a class="page-link" href="#" @click.prevent="goToPage(p)" x-text="p"></a>
                                    </li>
                                </template>

                                <li class="page-item" :class="{ 'disabled': currentPage >= totalPages }">
                                    <a class="page-link" href="#"
                                       @click.prevent="currentPage < totalPages && goToPage(currentPage + 1)">&raquo;</a>
                                </li>
                            </ul>
                        </nav>

                        <div class="d-flex align-items-center">
                            <label class="me-2 mb-0">Items per page:</label>
                            <select class="form-select form-select-sm" style="width: 100px;" x-model.number="pageSize"
                                    @change="pageChanged()">
                                <option>100</option>
                                <option>200</option>
                                <option>500</option>
                                <option>1000</option>
                            </select>
                        </div>
                    </div>


                    <div class="text-muted text-left mt-2">
                        <small>Page <span x-text="currentPage"></span> of <span x-text="totalPages"></span></small>
                    </div>
                </div>

            </div>
        </div>
        <div class="row">
            <div class="col">
                <div class="badge bg-success text-white">
                    <span>Total: <span x-text="totalItems"></span> results</span>
                </div>
                <div class="badge bg-success text-white">
                    <div>Response JSON Size: <span x-text="responseSize"></span></div>
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

    <div class="row mt-4 mb-4">
        <h4>Contact</h4>
        <div class="text-body-secondary">
            Contact: <a href="mailto:jdiminic@pbf.hr">jdiminic@pbf.hr</a>
            <br>
            <span>
                Bioinformatics Laboratory,
                Faculty of Food Technology and Biotechnology,
                University of Zagreb, Croatia
                <a href="https://www.pbf.unizg.hr/en/departments/department_of_biochemical_engineering/laboratory_for_bioinformatics/bioinformatics"
                   class="btn-link link-underline" target="_blank">
                    PBF
                </a>

            </span>
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

            currentPage: 1,
            pageSize: 10,
            totalItems: 0,
            totalPages: 0,

            responseSize: "",

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
                    this.calculatedMass = mass.toFixed(4);
                    this.mass1 = this.calculatedMass;
                    this.mass2 = this.calculatedMass;
                } catch (error) {
                    console.error('Error calculating mass:', error);
                    this.calculatedMass = 'Error: ' + error.message;
                }
            },


            getPaginationRange() {
                const range = [];
                const maxVisiblePages = 5;

                let start = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
                let end = Math.min(this.totalPages, start + maxVisiblePages - 1);

                if (end - start + 1 < maxVisiblePages) {
                    start = Math.max(1, end - maxVisiblePages + 1);
                }

                for (let i = start; i <= end; i++) {
                    range.push(i);
                }

                return range;
            },

            goToPage(page) {
                if (page < 1 || page > this.totalPages) return;
                this.currentPage = page;
                this.search();
            },

            pageChanged() {
                this.currentPage = 1; // Reset to first page when changing page size
                this.search();
            },

            searchNew() {
                this.currentPage = 1;
                this.search();
            },

            async search() {
                this.error = null;
                this.loading = true;
                try {
                    const url = `/search.php?mass1=${this.mass1}&mass2=${this.mass2}&page=${this.currentPage}&pageSize=${this.pageSize}`;
                    const response = await fetch(url);

                    if (!response.ok) {
                        const errorMessage = await response.text();


                        throw new Error(`Server error: ${errorMessage}`);
                    }

                    const responseText = await response.text();


                    const sizeInKB = responseText.length / 1024;
                    if (sizeInKB < 1000) {
                        this.responseSize = sizeInKB.toFixed(2) + " KB";
                    } else {
                        this.responseSize = (sizeInKB / 1024).toFixed(2) + " MB";
                    }

                    //  this.responseSizeInMB = (responseText.length / (1024 * 1024)).toFixed(2) + " MB";


                    const data = JSON.parse(responseText);

                    this.totalItems = data.totalResult || 0;
                    //this.totalPages = data.totalPages || 1;
                    this.totalPages = Math.ceil(this.totalItems / this.pageSize);

                    const memoryUsage = data.memory;
                    const duration = data.duration;

                    this.results = JSON.stringify(data, null, 2);
                } catch (error) {
                    console.error('Error:', error);
                    this.results = `Error: ${error.message}`;
                    this.error = error.message;

                    this.totalPages = 0;
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
