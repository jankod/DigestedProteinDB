<?php
include_once "lib.php";
header('Content-Type: application/json');

$url = DIGESTED_DB_URL . '/search?mass1=' . $_GET['mass1'] . '&mass2=' . $_GET['mass2'];
$result = sendRestRequest($url, 'GET');
//echo json_encode($result['response']);

//echo var_dump($result);
// echo json_encode($result['response']);  // Make sure you're using this approach
echo $result;

