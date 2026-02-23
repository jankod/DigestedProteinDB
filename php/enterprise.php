<?php
const HEAD_TITLE = "Digested Protein DB - Enterprise";
include_once 'lib.php';
?>

<!doctype html>
<html lang="en">
<head>
    <?php include_once "inc/html_head.php"; ?>
</head>
<body>

<?php include_once "inc/html_navbar.php"; ?>

<div class="container py-4">
    <div class="row">
        <div class="col-lg-8 mx-auto">
            <div class="card shadow-sm border-0">
                <div class="card-body p-4">
                    <h1 class="card-title h3 mb-3">DigestedProteinDB Engine (Rust)</h1>
                    <h2 class="h5 text-muted mb-4">Ultra-fast peptide mass-range search for large-scale proteomics</h2>
                    <p class="mb-3">
                        DigestedProteinDB Engine is a high-performance command-line tool designed to index and search
                        large virtual digests of protein databases. It enables millisecond-level peptide mass queries
                        even on very large datasets, making it suitable for modern MS-based proteomics and
                        metaproteomics workflows.
                    </p>
                </div>
            </div>
        </div>
    </div>
</div>

<?php include_once "inc/html_footer.php"; ?>

<script src="bootstrap.bundle.min.js"></script>
<script src="bio-lib.js" defer></script>

</body>
</html>
