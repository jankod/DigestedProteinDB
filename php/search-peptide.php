<?php
include_once "lib.php";
header('Content-Type: application/json');

$page     = isset($_GET['page'])     ? intval($_GET['page'])     : 1;
$pageSize = isset($_GET['pageSize']) ? intval($_GET['pageSize']) : 1000;

if ($page < 1)     $page     = 1;
if ($pageSize < 1) $pageSize = 10;

$peptide = isset($_GET['peptide']) ? trim($_GET['peptide']) : '';

if (empty($peptide)) {
    echo json_encode(['error' => 'Peptide sequence is required']);
    return;
}

$url = DIGESTED_DB_URL . '/search-peptide?peptide=' . urlencode($peptide) . '&page=' . $page . '&pageSize=' . $pageSize;

$result = sendRestRequest($url, 'GET');

if (is_array($result) && isset($result['error'])) {
    echo json_encode($result['error']);
    return;
}

echo $result;
