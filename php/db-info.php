<?php
include_once "lib.php";
header('Content-Type: application/json');

$url = DIGESTED_DB_URL . '/db-info';

$result = sendRestRequest($url, 'GET');
echo $result;
