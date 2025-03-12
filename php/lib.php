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
function sendRestRequest($url, $method = 'GET', $data = null, $headers = []) {
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