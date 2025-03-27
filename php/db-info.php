<?php
include_once "lib.php";
header('Content-Type: application/json');

$url = DIGESTED_DB_URL . '/db-info';

//$ch = curl_init($url);
$result = sendRestRequest($url, 'GET');
//var_dump($result);
echo $result;
//echo json_encode($result['response']);
