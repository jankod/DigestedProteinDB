<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Digested Protein DB</title>
    <link href="bootstrap-sandstone.min.css" rel="stylesheet">

</head>
<body>
<h1 class="text-center display-5 fw-bold text-primary shadow p-3 mb-5 bg-body rounded">
    Digested Protein DB
</h1>

<div class="container" x-data="searchData()">
    <div class="row">
        <div class="col">

            <div class="row">
                <div class="col-md-6">
                    <p class="mb-1"><strong>DB name:</strong> <span x-text="dbInfo.db_name"></span></p>
                    <p class="mb-1"><strong>Enzyme:</strong> <span x-text="dbInfo.enzyme_name"></span></p>
                    <p class="mb-1"><strong>Missed Cleavage:</strong> <span x-text="dbInfo.miss_cleavage"></span></p>
                    <p class="mb-1"><strong>Protein count:</strong> <span x-text="dbInfo.protein_count"></span></p>
                </div>
                <div class="col-md-6">

                    <p class="mb-1"><strong>DB size:</strong> <span x-text="dbInfo.db_size"></span></p>
                    <p class="mb-1"><strong>Min Peptide Length:</strong> <span
                                x-text="dbInfo.min_peptide_length"></span></p>
                    <p class="mb-1"><strong>Max Peptide Length:</strong> <span
                                x-text="dbInfo.max_peptide_length"></span></p>
                </div>
            </div>
            <div>
                Download: <a class="btn btn-link" href="https://digestedproteindb.pbf.hr/dow/trembl_bacteria.zip">trembl_bacteria.zip
                    (50 GB)</a>
            </div>


            <hr>

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
            <!--            <pre x-text="results"></pre>-->


            <!-- Table for displaying results instead of raw JSON -->
            <div x-show="results && parsedResults.length > 0" class="table-responsive">
                <table class="table  table-hover">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th title="Mass monoisotopic (Da)" class="text-nowrap">Mass (Da)</th>
                        <th>Peptide Sequence</th>
                    </tr>
                    </thead>
                    <tbody>
                    <template x-for="(item, index) in parsedResults" :key="index">
                        <tr>
                            <td x-text="(index + 1) + ((currentPage - 1) * pageSize)"></td>
                            <td x-text="item.mass"></td>
                            <td>
                                <div class="">
                                    <table class="table table-hover">
                                        <tbody>
                                        <template x-for="i in item.data" :key="i.seq">
                                            <tr>
                                                <td x-text="i.seq" class="font-monospace align-top"></td>
                                                <td class="align-top">
                                                    <template x-for="(acc, index) in i.acc" :key="acc">
                                                    <span><a class="btn-link pt-0 px-1 font-monospace"
                                                             :href="'https://www.uniprot.org/uniprot/'+acc.trim()"
                                                             target="_blank" x-text="acc"> </a>
                                                          <template x-if="(index + 1) % 7 === 0">
                                                            <br>
                                                        </template>

                                                    </span>

                                                    </template>

                                                </td>
                                            </tr>
                                        </template>
                                        </tbody>
                                    </table>
                                </div>
                            </td>

                        </tr>
                    </template>
                    </tbody>
                </table>
            </div>

            <div class="alert alert-info" x-show="parsedResults && parsedResults.length === 0">
                No results found for the search criteria.
            </div>


        </div>
    </div>


    <style>
        .card a {
            color: #0056b3 !important;
        }

        .card a:hover {
            color: #003580;
        }
    </style>
    <div class="row mt-5 mb-4">
        <div class="col">
            <div class="card shadow-sm">
                <div class="card-body">
                    <h4 class="card-title text-center mb-3">Contact</h4>
                    <p class="card-text ">

                        <strong>Department:</strong> <a
                                href="https://www.pbf.unizg.hr/en/departments/department_of_biochemical_engineering/laboratory_for_bioinformatics/bioinformatics"
                                class="href"> Bioinformatics Laboratory</a><br>
                        <strong>Institute:</strong>
                        <a href="https://www.pbf.unizg.hr/en/departments/department_of_biochemical_engineering/laboratory_for_bioinformatics/bioinformatics"
                           class="btn-link" target="_blank">
                            Faculty of Food Technology and Biotechnology
                            University of Zagreb, Croatia
                        </a>
                        <br>
                        <strong>GitHub:</strong> <a href="https://github.com/jankod/DigestedProteinDB" class="btn-link"
                                                    target="_blank">Digested Protein DB</a>
                        <br>
                        <strong>Email:</strong> <a href="mailto:jdiminic@pbf.hr">jdiminic@pbf.hr</a><br>

                    </p>

                </div>
            </div>
        </div>
    </div>


</div>


<script>

    function searchData() {
        function toTableData(result) {
            if (result === null || result === undefined) {
                return [];
            }
            // result is array of objects like this {"1500.6046":[{"seq":"TCDDECCPVNFKK","acc":["Q9YLG5","P03313"]},{"seq":"TCDEDCCPVNFKK","acc":["O91734"]}]} kako da
            // pretvori ga u array of objects like this {mass:1500.6046, data: [{"seq":"TCDDECCPVNFKK","acc":["Q9YLG5","P03313"]},{"seq":"TCDEDCCPVNFKK","acc":["O91734"]}]}
            const parsedResults = [];
            for (const [num, data] of Object.entries(result)) {
                const entry = Object.entries(data);
                const mass = entry[0][0];
                const seqAcc = entry[0][1];
                const massValue = parseFloat(mass);
                if (!isNaN(massValue)) {
                    parsedResults.push({
                        mass: massValue,
                        data: seqAcc
                    });
                }
            }

            return parsedResults;
        }

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
            parsedResults: [],

            async init() {
                try {
                    const response = await fetch('/db-info.php');
                    if (!response.ok) {
                        throw new Error('Failed to fetch database info');
                    }
                    this.dbInfo = await response.json();
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
                this.currentPage = 1;
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

                    const data = JSON.parse(responseText);

                    this.totalItems = data.totalResult || 0;
                    this.totalPages = Math.ceil(this.totalItems / this.pageSize);

                    if (typeof data === 'string') {
                        this.results = data;
                        this.loading = false;
                        this.error = data;
                        return;
                    }

                    this.parsedResults = toTableData(data.result);

//                    const memoryUsage = data.memory;
//                    const duration = data.duration;

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
<script src="bootstrap.bundle.min.js"></script>
<script src="bio-lib.js" defer></script>
<script src="//unpkg.com/alpinejs" defer></script>
</body>
</html>
