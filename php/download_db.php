<?php
const HEAD_TITLE = "Digested Protein DB - download";
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
    <h2>Download software and database</h2>

    <p>
        The database is available for download, all with 1 missed clevage and 7 to 30 peptide length.
    </p>
    <ul class="list-group ">
        <li class="list-group-item"><a class=" link-info" href='dow/disk3_download/db_swisprot_chymotrypsin.zip' download>db_swisprot_chymotrypsin.zip</a> (208 MB)</li>
        <li class="list-group-item"><a class="link-info" href='dow/disk3_download/db_swisprot_trypsin.zip' download>db_swisprot_trypsin.zip</a>  (263 MB)</li>
        <li class="list-group-item"><a class="link-info" href='dow/disk3_download/db_trembl_bacteria_trypsin.zip' download>db_trembl_bacteria_trypsin.zip</a> (50 GB)</li>
        <li class="list-group-item"><a class="link-info" href='dow/disk3_download/db_trembl_trypsin.zip' download>db_trembl_trypsin.zip</a> (208 GB)</li>
    </ul>
    <div class="custom-database-note text-body mt-3">
        <p>Creating custom databases for different taxonomic groups or enzymes is straightforward.
            Complete instructions are available on our <a  href="https://github.com/jankod/DigestedProteinDB" class="github-link link-info">GitHub repository</a>.</p>
    </div>



</div>

<?php include_once "inc/html_footer.php"; ?>
</body>
</html>
