<?php
include_once "lib.php";
header('Content-Type: application/json');

$url = DIGESTED_DB_URL . '/db-info';

//$ch = curl_init($url);
$result = sendRestRequest($url, 'GET');
echo $result;
// send json to javascript
//echo json_encode($result['response']);
