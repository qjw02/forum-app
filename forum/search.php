<?php

define('IN_API', true);
require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();
header('Content-Type: application/json; charset=utf-8');

$keyword = trim($_GET['keyword'] ?? '');

if ($keyword === '') {
    echo json_encode(array('code' => 400, 'message' => '请输入搜索内容'), JSON_UNESCAPED_UNICODE);
    exit;
}

if (dstrlen($keyword) > 50) {
    $keyword = dsubstr($keyword, 0, 50);
}

$list = DB::fetch_all(
    "SELECT tid, subject, author, views, replies, displayorder
     FROM pre_forum_thread
     WHERE displayorder >= 0 AND subject LIKE %s
     ORDER BY lastpost DESC
     LIMIT 20",
    array('%' . $keyword . '%')
);

echo json_encode(array(
    'code' => 0,
    'data' => $list
), JSON_UNESCAPED_UNICODE);
