<?php
include_once "lib.php";
header('Content-Type: application/json');

$page = isset($_GET['page']) ? intval($_GET['page']) : 1;
$pageSize = isset($_GET['pageSize']) ? intval($_GET['pageSize']) : 10;

if ($page < 1) $page = 1;
if ($pageSize < 1) $pageSize = 10;

// Make sure mass1 and mass2 exist
$mass1 = isset($_GET['mass1']) ? $_GET['mass1'] : '';
$mass2 = isset($_GET['mass2']) ? $_GET['mass2'] : '';

// Limit mass2 to be at most 0.3 greater than mass1
$maxDifference = 0.301;
if ($mass2 - $mass1 > $maxDifference) {
    echo json_encode('Mass To must be at most 0.3 Da greater than Mass From');
    return;
}


// Build the URL with search and pagination parameters
$url = DIGESTED_DB_URL . '/search?mass1=' . $mass1 . '&mass2=' . $mass2 . '&page=' . $page . '&pageSize=' . $pageSize;

$result = sendRestRequest($url, 'GET');

if (is_array($result) && isset($result['error'])) {
    echo json_encode($result['error']);
    return;
}

//echo json_encode($result['response']);

//echo var_dump($result);
// echo json_encode($result['response']);  // Make sure you're using this approach
echo $result;

