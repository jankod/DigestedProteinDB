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
    <h1>Download software and database</h1>


    <p>
        The database is available for download
    </p>
    <ul>
        <li><a href='dow/disk3_download/db_swisprot_chymotrypsin.zip' download>db_swisprot_chymotrypsin.zip (208 MB)</a></li>
        <li><a href='db_swisprot_trypsin.zip' download>db_swisprot_trypsin.zip (263 MB)</a></li>
        <li><a href='trembl_bacteria_trypsin.zip' download>trembl_bacteria_trypsin.zip (50 GB)</a></li>
    </ul>

</div>

<?php include_once "inc/html_footer.php"; ?>
</body>
</html>