<?php

header('Content-Type: application/json; charset=utf-8');

echo json_encode([
    'code' => 0,
    'message' => 'API OK'
], JSON_UNESCAPED_UNICODE);