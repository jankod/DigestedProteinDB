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
    <div class="card mb-4">
        <div class="card-header"><strong>Endpoint</strong></div>
        <div class="card-body">
            <code>GET https://digestedproteindb.pbf.hr/search.php</code>
            <p class="mt-3">
                Retrieves peptide/protein results filtered by a specific mass range or peptide query.<br>
                Returns results in JSON format.
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
                    <td>Yes (or peptide)</td>
                    <td>Lower bound of the mass range (inclusive). If <code>peptide</code> parameter is used, <code>mass1</code>
                        and <code>mass2</code> will be calculated from its mass.
                    </td>
                </tr>
                <tr>
                    <td><code>mass2</code></td>
                    <td>float</td>
                    <td>Yes (or peptide)</td>
                    <td>Upper bound of the mass range (inclusive).</td>
                </tr>
                <tr>
                    <td><code>peptide</code></td>
                    <td>string</td>
                    <td>No</td>
                    <td>Peptide sequence. If provided, calculates <code>mass1</code> and <code>mass2</code> for the
                        sequence.
                    </td>
                </tr>
                <tr>
                    <td><code>page</code></td>
                    <td>integer</td>
                    <td>No</td>
                    <td>Page number for pagination. Default is <code>1</code>.</td>
                </tr>
                <tr>
                    <td><code>pageSize</code></td>
                    <td>integer</td>
                    <td>No</td>
                    <td>Number of results per page. Default is <code>10</code>. Maximum allowed is <code>1000</code>.
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Request Example</strong></div>
        <div class="card-body">
            <pre><code>GET https://digestedproteindb.pbf.hr/search.php?mass1=1247.5&amp;mass2=1247.7&amp;page=1&amp;pageSize=10</code></pre>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Response</strong></div>
        <div class="card-body">
            <p><b>Content-Type:</b> <code>application/json</code></p>
            <p><b>Response structure:</b></p>
            <pre><code>{
    "totalResult": 431,
    "memory": "2008 MB",
    "duration": "00:00:54.966",
    "page": 1,
    "pageSize": 10,
    "result": [
        {
            "1247.6087": [
                {
                    "seq": "SYTFHFKYR",
                    "acc": [
                        "A0A0C9U8Z7",
                        "A0A0C9UZS6",
                        "A0A0C9UMQ1"
                    ]
                },
                {
                    "seq": "AIGFDGWHAFK",
                    "acc": [
                        "A0A8J3B0H4",
                        "A4G425",
                        "A0A4R6G671"
                    ]
                }
                // ... more peptide objects for this mass
            ]
        }
        // ... more mass keys and peptide arrays
    ]
}</code></pre>
            <p class="mt-3 mb-1"><b>Top-level fields:</b></p>
            <ul>
                <li><code>totalResult</code> – Total number of matching entries.</li>
                <li><code>memory</code> – Memory usage for the request.</li>
                <li><code>duration</code> – Query execution time.</li>
                <li><code>page</code> – Current page number.</li>
                <li><code>pageSize</code> – Number of results per page.</li>
                <li>
                    <code>result</code> – Array of objects, each key is the peptide mass (as a string/number), value is
                    an array of peptide objects:
                    <ul>
                        <li><code>seq</code> – Peptide sequence</li>
                        <li><code>acc</code> – List of protein accession numbers for this peptide</li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Error Responses</strong></div>
        <div class="card-body">
            <ul>
                <li><code>400 Bad Request</code> – Returned if required parameters are missing or invalid.<br>
                    <code>{"error": "Peptide is required"}</code>
                </li>
                <li><code>500 Internal Server Error</code> – Returned if the server encounters an error.<br>
                    <code>{"error": "Error details"}</code>
                </li>
            </ul>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Example cURL Request</strong></div>
        <div class="card-body">
            <pre><code>curl "https://digestedproteindb.pbf.hr/search.php?mass1=1247.5&amp;mass2=1247.7&amp;page=1&amp;pageSize=10"</code></pre>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Usage Notes</strong></div>
        <div class="card-body">
            <ul>
                <li>
                    Use either <code>mass1</code>/<code>mass2</code> for direct mass range filtering or
                    <code>peptide</code> for sequence-based mass search.
                </li>
                <li>
                    Each entry in <code>result</code> is an object with the mass as the key, mapping to an array of
                    peptide/protein hits.
                </li>
                <li>
                    The endpoint is optimized for read-only, high-performance querying.
                </li>
                <li>
                    Maximum <code>pageSize</code> is <code>1000</code>.
                </li>
            </ul>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header"><strong>Implementation Note (Java server method)</strong></div>
        <div class="card-body">
<pre><code>private void handleBySearch(HttpServerExchange http) {
    if (http.isInIoThread()) {
        http.dispatch(this::handleBySearch);
        return;
    }
    Map&lt;String, String&gt; params = createParam(http);

    try {
        String peptide = params.getOrDefault("peptide", "");
        if (peptide.isEmpty()) {
            sendJsonResponse(http, StatusCodes.BAD_REQUEST,
                    "{\"error\": \"Peptide is required\"}");
            return;
        }
        double mass1 = BioUtil.calculateMassWidthH2O(peptide);
        double mass2 = mass1;
        int page = Integer.parseInt(params.getOrDefault("page", "1"));
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "1000"));

        searchByMass(http, mass1, mass2, page, pageSize);
    } catch (Exception e) {
        sendJsonResponse(http, StatusCodes.INTERNAL_SERVER_ERROR,
                "{\"error\": \"" + e.getMessage() + "\"}");
    }
}</code></pre>
            <p class="mt-2">For <code>peptide</code> queries, mass is computed server-side and search is performed for
                that precise mass.</p>
        </div>
    </div>
</div>

<?php include_once "inc/html_footer.php"; ?>

<script src="bootstrap.bundle.min.js"></script>
<script src="bio-lib.js" defer></script>
<script src="//unpkg.com/alpinejs" defer></script>


</body>
</html>
