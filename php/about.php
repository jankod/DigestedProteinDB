<?php
const HEAD_TITLE = "Digested Protein DB REST API";
include_once 'lib.php';
?>

<!doctype html>
<html lang="en">
<head>
    <?php include_once "inc/html_head.php"; ?>

    <style>

    </style>
</head>
<body>

<?php include_once "inc/html_navbar.php"; ?>


<div class="container py-4">
    <div class="row">
        <div class="col-lg-8 mx-auto">
            <h1 class="mb-4">About Digested Protein DB</h1>
            <p>
                <strong>Digested Protein DB</strong> is a specialized database for storing, searching, and analyzing
                digested peptide and protein information, with a focus on mass spectrometry data and protein
                identification workflows.
            </p>
            <p>
                The database is designed and maintained by the <strong>Bioinformatics Laboratory</strong> at the
                <a href="https://www.pbf.unizg.hr/en" target="_blank">Faculty of Food Technology and Biotechnology,
                    University of Zagreb, Croatia</a>.
            </p>
            <h4 class="mt-4">Project Mission</h4>
            <ul>
                <li>To provide fast and efficient access to peptide/protein data for research in proteomics and related
                    fields.
                </li>
                <li>To support the scientific community with open data and robust search tools for mass spectrometry
                    analysis.
                </li>
                <li>To foster collaboration and knowledge exchange among bioinformatics and biotechnology researchers.
                </li>
            </ul>
            <h4 class="mt-4">Contact & Resources</h4>
            <table class="table table-bordered w-75">
                <tbody>
                <tr>
                    <th scope="row">Department</th>
                    <td>Bioinformatics Laboratory,<br> Faculty of Food Technology and Biotechnology,<br> University of
                        Zagreb, Croatia <a href="https://www.pbf.unizg.hr/en/departments/department_of_biochemical_engineering/laboratory_for_bioinformatics/bioinformatics">PBF</a>
                    </td>
                </tr>
                <tr>
                    <th scope="row">Email</th>
                    <td><a href="mailto:jdiminic@pbf.hr">jdiminic@pbf.hr</a></td>
                </tr>

                </tbody>
            </table>
            <h4 class="mt-4">Acknowledgements</h4>
            <p>
                This project is supported by the Faculty of Food Technology and Biotechnology, University of Zagreb.
                We thank all collaborators and contributors to the Digested Protein DB project.
            </p>
            <hr>
            <div class="text-muted small">
                Last updated: May 2025 | Version: 1.0.0
            </div>
        </div>
    </div>
</div>


<?php include_once "inc/html_footer.php"; ?>

<script src="bootstrap.bundle.min.js"></script>
<script src="bio-lib.js" defer></script>
<script src="//unpkg.com/alpinejs" defer></script>


</body>
</html>
