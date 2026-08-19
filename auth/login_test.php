<?php

error_reporting(E_ALL);
ini_set('display_errors', '1');

define('IN_API', true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';

/*
 * 初始化 Discuz
 */
C::app()->init();

header('Content-Type: application/json; charset=utf-8');

$result = userlogin(
    $_POST['username'] ?? '',
    $_POST['password'] ?? '',
    0,
    '',
    'username',
    ''
);

echo json_encode([
    'code' => 0,
    'message' => 'userlogin执行完成',
    'result' => $result
], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
