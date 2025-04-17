<?php

const DIGESTED_DB_URL = 'http://localhost:7071';

/**
 * Send a REST request to a specified URL
 *
 * @param string $url The URL to send the request to
 * @param string $method HTTP method (GET, POST, PUT, DELETE, etc.)
 * @param array|null $data Data to send with the request
 * @param array $headers Additional headers to send
 * @return array Associative array containing 'response' and 'status_code'
 */
function sendRestRequest($url, $method = 'GET', $data = null, $headers = [])
{
    $ch = curl_init($url);

    // Set common options
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);

    // Set headers
    $defaultHeaders = ['Content-Type: application/json'];
    $allHeaders = array_merge($defaultHeaders, $headers);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $allHeaders);

    // Set data for POST, PUT, etc.
    if ($data !== null && in_array($method, ['POST', 'PUT', 'PATCH'])) {
        $jsonData = json_encode($data);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $jsonData);
    }

    // Execute the request
    $response = curl_exec($ch);
    $statusCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    // Check for errors
    if (curl_errno($ch)) {
        $error = curl_error($ch);
        curl_close($ch);
        return ['response' => null, 'status_code' => 0, 'error' => $error];
    }

    curl_close($ch);

    // Try to decode JSON response
    // $decoded = json_decode($response, true);
    // $result = $decoded !== null ? $decoded : $response;

    // return ['response' => $result, 'status_code' => $statusCode];
    return $response;
}


function getCachedUniProtData(string $url, string $cacheKey, int $cacheExpiry = 3600): string
{
    $cacheFile = sys_get_temp_dir() . '/' . $cacheKey . '.cache';

    // Check if the cache file exists and is still valid
    if (file_exists($cacheFile) && (time() - filemtime($cacheFile) < $cacheExpiry)) {
        return file_get_contents($cacheFile);
    }

    // Fetch data from the URL
    $data = @file_get_contents($url);

    if ($data === false) {
        // Handle error if needed
       return false;
    }

    // If data was successfully fetched, cache it
    if ($data !== false) {
        file_put_contents($cacheFile, $data);
    }

    return $data;
}


class UniprotEntry
{
    public string $accession;
    public string $proteinName;
    public string $sequence = '';
    public string $lineage = '';
    public string $errors = '';

    public function __construct(string $accession, string $proteinName, string $sequence)
    {
        $this->accession = $accession;
        $this->proteinName = $proteinName;
        $this->sequence = $sequence;
    }
}

function isActive(string $page): string
{
    return basename($_SERVER['PHP_SELF']) === $page ? 'active' : '';
}

function parseUniProtData($jsonString): UniprotEntry
{
    $data = json_decode($jsonString, true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        // Handle parse error if needed
        $entry = new UniprotEntry('', '', '');
        $entry->errors = 'Error parsing JSON: ' . json_last_error_msg();
        return $entry;
    }

    // Get protein name from either recommendedName or submissionNames
    $proteinName = '';
    if (isset($data['proteinDescription']['recommendedName']['fullName']['value'])) {
        $proteinName = $data['proteinDescription']['recommendedName']['fullName']['value'];
    } elseif (isset($data['proteinDescription']['submissionNames'][0]['fullName']['value'])) {
        $proteinName = $data['proteinDescription']['submissionNames'][0]['fullName']['value'];
    }


    return new UniprotEntry(
        $data['primaryAccession'] ?? '',
        $proteinName,
        $data['sequence']['value'] ?? ''
    );
}

