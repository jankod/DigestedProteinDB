<?php
const HEAD_TITLE = "Digested Protein DB";
include_once 'lib.php';
?>

<!doctype html>
<html lang="en">
<head>
    <?php include_once "inc/html_head.php"; ?>

</head>
<body>
<!--<h1 class="text-center display-5 fw-bold text-primary shadow p-3 mb-5 bg-body rounded">-->
<!--    Digested Protein DB-->
<!--</h1>-->

<?php include_once "inc/html_navbar.php"; ?>

<div class="container" x-data="searchData()">
    <div class="row">
        <div class="col">

            <div class="row">
                <h4>Current digested protein database</h4>
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

            <hr>

            <div class="row">
                <div class="col">
                    <h3>Search by mass</h3>
                    <form @submit.prevent="searchNew">
                        <div class="mb-3">
                            <label for="mass1" class="form-label">Mass from (Da)</label>
                            <input x-model="mass1" type="text" class="form-control w-50" id="mass1" name="mass1"
                                   required
                                   value="1500.6">
                        </div>
                        <div class="mb-3">
                            <label for="mass2" class="form-label">Mass to (Da)</label>
                            <input x-model="mass2" type="text" class="form-control w-50" id="mass2" name="mass2"
                                   required
                                   value="1500.62">
                        </div>
                        <button type="submit" class="btn btn-primary" :disabled="loading">
                            <span x-show="!loading">Search</span>
                            <span x-show="loading" style="display: none;">Loading...</span>
                        </button>
                    </form>
                </div>

                <div class="col mt-2">
                    <h4>Calculate by m/z</h4>
                    <form @submit.prevent="addByMZ">

                        <div class="mb-3">
                            <label for="mz" class="form-label">m/z value</label>
                            <input x-model="mzValue" type="text" class="form-control w-50" id="mz" name="mz"
                                   placeholder="756.41" @input="addByMZ">
                        </div>
                        <div class="mb-3">
                            <label for="charge" class="form-label">Charge (z)</label>
                            <select x-model.number="charge" class="form-select w-50" id="charge" name="charge"
                                    @change="addByMZ">
                                <option value="1">+1</option>
                                <option value="2">+2</option>
                                <option value="3">+3</option>
                                <option value="4">+4</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="tolerance" class="form-label">Mass tolerance</label>
                            <div class="input-group w-50">
                                <input x-model="tolerance" type="text" class="form-control" id="tolerance"
                                       name="tolerance" placeholder="0.02" @input="addByMZ">
                                <select x-model="toleranceUnit" class="form-select" style="max-width: 100px;"
                                        @change="addByMZ">
                                    <option value="Da">Da</option>
                                    <option value="ppm">ppm</option>
                                </select>
                            </div>
                        </div>
                        <div class="text-danger" x-show="mzError" x-text=" mzError "></div>

                    </form>
                    <div class="mb-3">
                        <h4>Calulcate mass by sequence</h4>
                        <input x-model="peptideSequence" @input="calculatePeptideMass" type="text"
                               class="form-control" id="sequence" name="sequence">
                        <div class="text-danger" x-show="calculatedMass && calculatedMass.includes('Error:')"
                             x-text="calculatedMass"></div>

                    </div>

                </div>
            </div>
        </div>
    </div>

    <div class="row mt-4 mb-4" x-show="ptmSearchResults && totalPages > 0">
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
                    <span>Total: <span x-text="totalItems"></span> ptmSearchResults</span>
                </div>
                <div class="badge bg-success text-white">
                    <div>Response JSON Size: <span x-text="responseSize"></span></div>
                </div>
            </div>
        </div>
    </div>

    <div class="row mt-2">
        <div class="col">
            <h2>Results</h2>
            <div class="alert alert-danger" role="alert" x-text="error" x-show="error" style="display: none;"></div>
            <!--            <pre x-text="ptmSearchResults"></pre>-->


            <!-- Table for displaying ptmSearchResults instead of raw JSON -->
            <div x-show="ptmSearchResults && parsedResults.length > 0" class="table-responsive">
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

                            <td x-text="((index + 1) + ((currentPage - 1) * pageSize)) + '.' "></td>
                            <td x-text="item.mass"></td>
                            <td>
                                <div class="">
                                    <table class="table-sm table-hover">
                                        <tbody>
                                        <template x-for="(i, index2) in item.data" x-key="index2">
                                            <tr>
                                                <td class="align-top"><span x-text="index2+1"></span>.</td>
                                                <td x-text="i.seq" class="font-monospace align-top"></td>
                                                <td class="align-top">
                                                    <template x-for="(acc, index) in i.acc" :key="acc">
                                                        <!--                                                         :href="'https://www.uniprot.org/uniprot/'+acc.trim()"-->
                                                        <span><a class="btn-link pt-0 px-1 font-monospace"
                                                                 :href="'tool.php?accession='+acc.trim()"
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
                No ptmSearchResults found for the search criteria.
            </div>


        </div>
    </div>


    <?php include_once "inc/html_footer.php"; ?>


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

            mzValue: 1500,
            charge: 1,
            tolerance: 0.1,
            toleranceUnit: "Da",
            mzError: "",

            calculatedMass: "",
            peptideSequence: "",

            mass1: 1500.6,
            mass2: 1500.8,
            ptmSearchResults: "",
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

            addByMZ() {
                this.mzError = "";
                let result = this.calculateMassFromMZ();
                if (result.error) {
                    this.mzError = result.error;
                    return;
                }

                this.error = "";
                const exactMass = result.mass;

                let toleranceValue = parseFloat(this.tolerance);

                if (isNaN(toleranceValue) || toleranceValue <= 0) {
                    this.mzError = "Tolerance must be a positive number";
                    return;
                }

                let massFrom, massTo;

                if (this.toleranceUnit === "ppm") {
                    const absoluteTolerance = (exactMass * toleranceValue) / 1000000;
                    console.log("absoluteTolerance ppm", absoluteTolerance)
                    massFrom = exactMass - absoluteTolerance;
                    massTo = exactMass + absoluteTolerance;
                } else {
                    console.log("toleranceValue Da", toleranceValue)
                    massFrom = exactMass - toleranceValue;
                    massTo = exactMass + toleranceValue;
                }

                this.mass1 = massFrom.toFixed(4);
                this.mass2 = massTo.toFixed(4);


                //this.search();
            },

            calculateMassFromMZ() {
                try {
                    if (!this.mzValue || isNaN(this.mzValue) || !this.charge || isNaN(this.charge)) {
                        return {error: "m/z and charge must be valid numbers"};
                    }

                    const mz = parseFloat(this.mzValue);
                    const z = parseInt(this.charge);
                    const protonMass = 1.0078;

                    // Old Formula: M = (m/z * z) - (z * H+)
                    const calculatedMass = (mz * z) - (z * protonMass);

                    console.log("calculatedMass", calculatedMass);

                    return {
                        mass: calculatedMass,
                        error: null
                    };
                } catch (error) {
                    console.error('Error calculating mass from m/z:', error);
                    return {error: error.message};
                }
            },

            calculatePeptideMass() {
                try {
                    if (this.peptideSequence.trim() === '') {
                        this.calculatedMass = '';
                        return;
                    }

                    let mass = calculateMassWidthH2O(this.peptideSequence.toUpperCase());
                    console.log("this.ptmType", this.ptmType)

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
                        this.ptmSearchResults = data;
                        this.loading = false;
                        this.error = data;
                        return;
                    }

                    this.parsedResults = toTableData(data.result);

//                    const memoryUsage = data.memory;
//                    const duration = data.duration;

                    this.ptmSearchResults = JSON.stringify(data, null, 2);
                } catch (error) {
                    console.error('Error:', error);
                    this.ptmSearchResults = `Error: ${error.message}`;
                    this.error = error.message;

                    this.totalPages = 0;
                    this.ptmSearchResults = [];
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

<?php include_once "inc/google_analytics_body.php"; ?>
</body>
</html>
