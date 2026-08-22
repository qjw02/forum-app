<?php
/*
 * Firebase Cloud Messaging HTTP v1 helper.
 * The service-account JSON must stay outside the web root.
 */
if (!defined('FCM_PROJECT_ID')) {
    define('FCM_PROJECT_ID', 'qjw-forum');
}
if (!defined('FCM_SERVICE_ACCOUNT_FILE')) {
    define('FCM_SERVICE_ACCOUNT_FILE', '/www/secure/firebase/qjwforum-firebase-admin.json');
}

function fcm_base64url($value) {
    return rtrim(strtr(base64_encode($value), '+/', '-_'), '=');
}

function fcm_service_account() {
    static $serviceAccount = null;
    if ($serviceAccount !== null) {
        return $serviceAccount;
    }

    if (!is_readable(FCM_SERVICE_ACCOUNT_FILE)) {
        return false;
    }

    $content = file_get_contents(FCM_SERVICE_ACCOUNT_FILE);
    $json = json_decode($content, true);
    if (!is_array($json) || empty($json['client_email']) || empty($json['private_key'])) {
        return false;
    }

    $serviceAccount = $json;
    return $serviceAccount;
}

function fcm_access_token() {
    static $accessToken = null;
    if ($accessToken !== null) {
        return $accessToken;
    }

    $account = fcm_service_account();
    if (!$account || !function_exists('openssl_sign') || !function_exists('curl_init')) {
        return false;
    }

    $now = time();
    $header = fcm_base64url(json_encode(array('alg' => 'RS256', 'typ' => 'JWT')));
    $claims = fcm_base64url(json_encode(array(
        'iss' => $account['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => 'https://oauth2.googleapis.com/token',
        'iat' => $now,
        'exp' => $now + 3600
    )));
    $unsigned = $header.'.'.$claims;

    $signature = '';
    if (!openssl_sign($unsigned, $signature, $account['private_key'], 'SHA256')) {
        return false;
    }
    $assertion = $unsigned.'.'.fcm_base64url($signature);

    $curl = curl_init('https://oauth2.googleapis.com/token');
    curl_setopt_array($curl, array(
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => http_build_query(array(
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' => $assertion
        )),
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => 15,
        CURLOPT_HTTPHEADER => array('Content-Type: application/x-www-form-urlencoded')
    ));
    $response = curl_exec($curl);
    $status = intval(curl_getinfo($curl, CURLINFO_HTTP_CODE));
    curl_close($curl);

    $data = json_decode($response, true);
    if ($status !== 200 || empty($data['access_token'])) {
        return false;
    }

    $accessToken = $data['access_token'];
    return $accessToken;
}

function fcm_send_to_token($token, $title, $body, $data = array()) {
    $accessToken = fcm_access_token();
    if (!$accessToken || !$token) {
        return false;
    }

    $safeData = array();
    foreach ((array)$data as $key => $value) {
        $safeData[(string)$key] = (string)$value;
    }

    $payload = array(
        'message' => array(
            'token' => $token,
            'notification' => array('title' => (string)$title, 'body' => (string)$body),
            'data' => $safeData,
            'android' => array('priority' => 'high')
        )
    );

    $curl = curl_init('https://fcm.googleapis.com/v1/projects/'.rawurlencode(FCM_PROJECT_ID).'/messages:send');
    curl_setopt_array($curl, array(
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => json_encode($payload, JSON_UNESCAPED_UNICODE),
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => 15,
        CURLOPT_HTTPHEADER => array(
            'Authorization: Bearer '.$accessToken,
            'Content-Type: application/json; charset=utf-8'
        )
    ));
    $response = curl_exec($curl);
    $status = intval(curl_getinfo($curl, CURLINFO_HTTP_CODE));
    curl_close($curl);

    return $status >= 200 && $status < 300;
}

function fcm_send_to_user($uid, $title, $body, $data = array()) {
    $uid = intval($uid);
    if ($uid <= 0) {
        return 0;
    }

    $tokens = DB::fetch_all(
        "SELECT id, token FROM pre_app_push_token WHERE uid=%d ORDER BY dateline DESC LIMIT 5",
        array($uid)
    );

    $sent = 0;
    foreach ($tokens as $row) {
        if (fcm_send_to_token($row['token'], $title, $body, $data)) {
            $sent++;
        }
    }
    return $sent;
}
