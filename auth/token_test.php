<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

header('Content-Type: application/json; charset=utf-8');

$token = bin2hex(random_bytes(32));


$id = C::t('app_token')->insert_token(
    562,
    $token,
    TIMESTAMP+2592000
);


echo json_encode([
    'id'=>$id,
    'token'=>$token
],JSON_UNESCAPED_UNICODE);

