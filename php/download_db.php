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
        The database is available for download
    </p>
    <ul class="list-group ">
        <li class="list-group-item"><a class=" link-info" href='dow/disk3_download/db_swisprot_chymotrypsin.zip' download>db_swisprot_chymotrypsin.zip (208 MB)</a></li>
        <li class="list-group-item"><a class="link-info" href='dow/disk3_download/db_swisprot_trypsin.zip' download>db_swisprot_trypsin.zip (263 MB)</a></li>
        <li class="list-group-item"><a class="link-info" href='dow/disk3_download/db_trembl_bacteria_trypsin.zip' download>trembl_bacteria_trypsin.zip (50 GB)</a></li>
    </ul>

</div>

<?php include_once "inc/html_footer.php"; ?>
</body>
</html>