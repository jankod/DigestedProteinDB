<?php
const HEAD_TITLE = "Digested Protein DB REST API";
include_once 'lib.php';
?>
<!doctype html>
<html lang="en">
<head>
    <?php include_once "inc/html_head.php"; ?>
</head>
<body>

<?php include_once "inc/html_navbar.php"; ?>

<div class="container">
    <h1 class="mb-4">Digested Protein DB REST API Documentation</h1>

    <p class="lead mb-4">
        The DigestedProteinDB REST API provides programmatic access to a large database of theoretically
        digested proteins for use in mass spectrometry (MS) peptide identification workflows.
        All endpoints return <code>application/json</code> and support pagination.
    </p>

    <!-- Endpoints overview -->
    <div class="card mb-4">
        <div class="card-header"><strong>Available Endpoints</strong></div>
        <div class="card-body p-0">
            <table class="table table-bordered table-hover mb-0">
                <thead class="table-light">
                <tr>
                    <th>Method</th>
                    <th>Endpoint</th>
                    <th>Description</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><span class="badge bg-success">GET</span></td>
                    <td><code>search.php</code></td>
                    <td>Search peptides by mass range. Returns accession numbers.</td>
                </tr>
                <tr>
                    <td><span class="badge bg-success">GET</span></td>
                    <td><code>search-peptide.php</code></td>
                    <td>Search by peptide amino acid sequence (exact monoisotopic mass match).</td>
                </tr>
                <tr>
                    <td><span class="badge bg-success">GET</span></td>
                    <td><code>search-taxonomy.php</code></td>
                    <td>Search peptides by mass range. Returns accession numbers enriched with NCBI taxonomy IDs.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <!-- ============================================================ -->
    <!--  ENDPOINT 1: /search                                         -->
    <!-- ============================================================ -->
    <h2 class="mt-5 mb-3 fs-4 border-bottom pb-2">1. Search by Mass Range &mdash; <code>GET search.php</code></h2>

    <div class="card mb-4">
        <div class="card-header"><strong>Endpoint</strong></div>
        <div class="card-body">
            <code>GET https://digestedproteindb.pbf.hr/search.php</code>
            <p class="mt-3 mb-0">
                Retrieves peptides whose monoisotopic mass falls within the specified range.
                Returns UniProt accession numbers for each matching peptide.
            </p>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Query Parameters</strong></div>
        <div class="card-body">
            <table class="table table-bordered table-hover mb-0">
                <thead class="table-light">
                <tr>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Required</th>
                    <th>Description</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><code>mass1</code></td>
                    <td>float</td>
                    <td>Yes</td>
                    <td>Lower bound of the mass range in Daltons (inclusive).</td>
                </tr>
                <tr>
                    <td><code>mass2</code></td>
                    <td>float</td>
                    <td>Yes</td>
                    <td>Upper bound of the mass range in Daltons (inclusive).</td>
                </tr>
                <tr>
                    <td><code>page</code></td>
                    <td>integer</td>
                    <td>No</td>
                    <td>Page number for pagination. Default: <code>1</code>.</td>
                </tr>
                <tr>
                    <td><code>pageSize</code></td>
                    <td>integer</td>
                    <td>No</td>
                    <td>Results per page. Default: <code>10</code>. Maximum: <code>1000</code>.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Request Example</strong></div>
        <div class="card-body">
            <pre><code>GET https://digestedproteindb.pbf.hr/search.php?mass1=1247.5&amp;mass2=1247.7&amp;page=1&amp;pageSize=10</code></pre>
            <p class="mt-2 mb-0">cURL:</p>
            <pre class="mb-0"><code>curl "https://digestedproteindb.pbf.hr/search.php?mass1=1247.5&amp;mass2=1247.7&amp;page=1&amp;pageSize=10"</code></pre>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Response</strong></div>
        <div class="card-body">
            <p><b>Content-Type:</b> <code>application/json</code></p>
            <pre><code>{
    "totalResult": 431,
    "memory": "2008 MB",
    "duration": "00:00:00.024",
    "page": 1,
    "pageSize": 10,
    "result": [
        {
            "1247.6087": [
                {
                    "seq": "SYTFHFKYR",
                    "acc": ["A0A0C9U8Z7", "A0A0C9UZS6", "A0A0C9UMQ1"]
                },
                {
                    "seq": "AIGFDGWHAFK",
                    "acc": ["A0A8J3B0H4", "A4G425"]
                }
            ]
        }
    ]
}</code></pre>
            <p class="mt-3 mb-1"><b>Fields:</b></p>
            <ul class="mb-0">
                <li><code>totalResult</code> – Total number of matching peptides across all pages.</li>
                <li><code>memory</code> – Server memory usage at the time of the request.</li>
                <li><code>duration</code> – Server-side query execution time.</li>
                <li><code>page</code> / <code>pageSize</code> – Pagination info.</li>
                <li><code>result</code> – Array of objects keyed by peptide mass (Da):
                    <ul>
                        <li><code>seq</code> – Peptide amino acid sequence (single-letter code).</li>
                        <li><code>acc</code> – List of UniProt accession numbers of proteins containing this peptide.</li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>

    <!-- ============================================================ -->
    <!--  ENDPOINT 2: /search-peptide                                 -->
    <!-- ============================================================ -->
    <h2 class="mt-5 mb-3 fs-4 border-bottom pb-2">2. Search by Peptide Sequence &mdash; <code>GET search-peptide.php</code></h2>

    <div class="card mb-4">
        <div class="card-header"><strong>Endpoint</strong></div>
        <div class="card-body">
            <code>GET https://digestedproteindb.pbf.hr/search-peptide.php</code>
            <p class="mt-3 mb-0">
                Accepts a peptide amino acid sequence, computes its exact monoisotopic mass server-side,
                and returns all database entries matching that precise mass.
                The response format is identical to <code>search.php</code>.
            </p>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Query Parameters</strong></div>
        <div class="card-body">
            <table class="table table-bordered table-hover mb-0">
                <thead class="table-light">
                <tr>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Required</th>
                    <th>Description</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><code>peptide</code></td>
                    <td>string</td>
                    <td>Yes</td>
                    <td>Amino acid sequence in single-letter code (e.g. <code>SYTFHFKYR</code>). Mass is computed server-side.</td>
                </tr>
                <tr>
                    <td><code>page</code></td>
                    <td>integer</td>
                    <td>No</td>
                    <td>Page number. Default: <code>1</code>.</td>
                </tr>
                <tr>
                    <td><code>pageSize</code></td>
                    <td>integer</td>
                    <td>No</td>
                    <td>Results per page. Default: <code>1000</code>. Maximum: <code>1000</code>.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Request Example</strong></div>
        <div class="card-body">
            <pre><code>GET https://digestedproteindb.pbf.hr/search-peptide.php?peptide=SYTFHFKYR</code></pre>
            <p class="mt-2 mb-0">cURL:</p>
            <pre class="mb-0"><code>curl "https://digestedproteindb.pbf.hr/search-peptide.php?peptide=SYTFHFKYR"</code></pre>
        </div>
    </div>

    <!-- ============================================================ -->
    <!--  ENDPOINT 3: /search-taxonomy                                -->
    <!-- ============================================================ -->
    <h2 class="mt-5 mb-3 fs-4 border-bottom pb-2">3. Search by Mass Range with Taxonomy &mdash; <code>GET search-taxonomy.php</code></h2>

    <div class="card mb-4">
        <div class="card-header"><strong>Endpoint</strong></div>
        <div class="card-body">
            <code>GET https://digestedproteindb.pbf.hr/search-taxonomy.php</code>
            <p class="mt-3 mb-0">
                Identical to <code>search.php</code> in its query parameters, but the response enriches each
                accession number with the corresponding <b>NCBI Taxonomy ID</b> (<code>taxId</code>).
                This allows downstream filtering of results by organism directly from the API response,
                without requiring additional lookups.
            </p>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Query Parameters</strong></div>
        <div class="card-body">
            <table class="table table-bordered table-hover mb-0">
                <thead class="table-light">
                <tr>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Required</th>
                    <th>Description</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><code>mass1</code></td>
                    <td>float</td>
                    <td>Yes</td>
                    <td>Lower bound of the mass range in Daltons (inclusive).</td>
                </tr>
                <tr>
                    <td><code>mass2</code></td>
                    <td>float</td>
                    <td>Yes</td>
                    <td>Upper bound of the mass range in Daltons (inclusive).</td>
                </tr>
                <tr>
                    <td><code>page</code></td>
                    <td>integer</td>
                    <td>No</td>
                    <td>Page number. Default: <code>1</code>.</td>
                </tr>
                <tr>
                    <td><code>pageSize</code></td>
                    <td>integer</td>
                    <td>No</td>
                    <td>Results per page. Default: <code>1000</code>. Maximum: <code>1000</code>.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Request Example</strong></div>
        <div class="card-body">
            <pre><code>GET https://digestedproteindb.pbf.hr/search-taxonomy.php?mass1=1247.5&amp;mass2=1247.7&amp;page=1&amp;pageSize=10</code></pre>
            <p class="mt-2 mb-0">cURL:</p>
            <pre class="mb-0"><code>curl "https://digestedproteindb.pbf.hr/search-taxonomy.php?mass1=1247.5&amp;mass2=1247.7&amp;page=1&amp;pageSize=10"</code></pre>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Response</strong></div>
        <div class="card-body">
            <p><b>Content-Type:</b> <code>application/json</code></p>
            <p>
                The structure is the same as <code>search.php</code>, except each peptide entry uses
                <code>accsTax</code> instead of <code>acc</code>. Each element of <code>accsTax</code>
                is an object containing the UniProt accession and its NCBI Taxonomy ID.
            </p>
            <pre><code>{
    "totalResult": 431,
    "memory": "2014 MB",
    "duration": "00:00:00.047",
    "page": 1,
    "pageSize": 10,
    "result": [
        {
            "1247.6087": [
                {
                    "seq": "SYTFHFKYR",
                    "accsTax": [
                        {"acc": "A0A0C9U8Z7", "taxId": 9606},
                        {"acc": "A0A0C9UZS6", "taxId": 10090},
                        {"acc": "A0A0C9UMQ1", "taxId": 10116}
                    ]
                },
                {
                    "seq": "AIGFDGWHAFK",
                    "accsTax": [
                        {"acc": "A0A8J3B0H4", "taxId": 3702},
                        {"acc": "A4G425",     "taxId": 4577}
                    ]
                }
            ]
        }
    ]
}</code></pre>
            <p class="mt-3 mb-1"><b>Fields specific to this endpoint:</b></p>
            <ul class="mb-0">
                <li>
                    <code>accsTax</code> – List of accession/taxonomy objects per peptide (replaces <code>acc</code>):
                    <ul>
                        <li><code>acc</code> – UniProt accession number.</li>
                        <li><code>taxId</code> – NCBI Taxonomy ID of the source organism
                            (e.g. <code>9606</code> = <em>Homo sapiens</em>,
                            <code>10090</code> = <em>Mus musculus</em>,
                            <code>3702</code> = <em>Arabidopsis thaliana</em>).
                            Use <a href="https://www.ncbi.nlm.nih.gov/taxonomy" target="_blank">NCBI Taxonomy</a>
                            to resolve IDs to organism names.
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>

    <!-- ============================================================ -->
    <!--  Error responses & general notes                             -->
    <!-- ============================================================ -->
    <h2 class="mt-5 mb-3 fs-4 border-bottom pb-2">Error Responses</h2>

    <div class="card mb-4">
        <div class="card-body">
            <ul class="mb-0">
                <li>
                    <code>400 Bad Request</code> – Missing or invalid parameters.<br>
                    <code>{"error": "Mass1 and Mass2 are required as doubles."}</code>
                </li>
                <li class="mt-2">
                    <code>500 Internal Server Error</code> – Unexpected server-side error.<br>
                    <code>{"error": "Error details"}</code>
                </li>
            </ul>
        </div>
    </div>

    <h2 class="mt-5 mb-3 fs-4 border-bottom pb-2">General Notes</h2>

    <div class="card mb-4">
        <div class="card-body">
            <ul class="mb-0">
                <li>All masses are <b>monoisotopic</b> masses in Daltons (Da).</li>
                <li>All endpoints are read-only and optimized for high-performance querying.</li>
                <li>Maximum <code>pageSize</code> is <code>1000</code> for all endpoints.</li>
                <li>
                    For programmatic access examples in Python, see the
                    <a href="https://github.com/tag/DigestedProteinDB/tree/master/python" target="_blank">
                        Python example scripts</a> included with the database distribution.
                </li>
            </ul>
        </div>
    </div>
</div>

<?php include_once "inc/html_footer.php"; ?>

<script src="bootstrap.bundle.min.js"></script>
<script src="bio-lib.js" defer></script>
<script src="//unpkg.com/alpinejs" defer></script>

<?php include_once "inc/google_analytics_body.php"; ?>
</body>
</html>
