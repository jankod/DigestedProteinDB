<?php
include_once "lib.php";
header('Content-Type: application/json');

$page     = isset($_GET['page'])     ? intval($_GET['page'])     : 1;
$pageSize = isset($_GET['pageSize']) ? intval($_GET['pageSize']) : 10;

if ($page < 1)     $page     = 1;
if ($pageSize < 1) $pageSize = 10;

$mass1 = isset($_GET['mass1']) ? $_GET['mass1'] : '';
$mass2 = isset($_GET['mass2']) ? $_GET['mass2'] : '';

if (!is_numeric($mass1) || !is_numeric($mass2)) {
    echo json_encode(['error' => 'Mass From and Mass To must be numbers']);
    return;
}

// Limit mass range to at most 0.3 Da (same restriction as /search)
$maxDifference = 0.301;
if ($mass2 - $mass1 > $maxDifference) {
    echo json_encode(['error' => 'Mass To must be at most 0.3 Da greater than Mass From']);
    return;
}

$url = DIGESTED_DB_URL . '/search-taxonomy?mass1=' . $mass1 . '&mass2=' . $mass2 . '&page=' . $page . '&pageSize=' . $pageSize;

$result = sendRestRequest($url, 'GET');

if (is_array($result) && isset($result['error'])) {
    echo json_encode($result['error']);
    return;
}

echo $result;
