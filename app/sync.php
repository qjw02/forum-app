<?php

define('IN_API', true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

header('Content-Type: application/json; charset=utf-8');

$scope = $_GET['scope'] ?? 'home';
$version = '';

if ($scope === 'home') {
    $latest = DB::fetch_first(
        "SELECT MAX(lastpost) AS latestpost,
                COUNT(*) AS threadcount,
                COALESCE(SUM(displayorder), 0) AS pinned,
                COALESCE(SUM(digest), 0) AS digests
         FROM pre_forum_thread"
    );

    // 精华、置顶状态或首页内容变化时才通知 APP 更新首页缓存。
    $version = md5('home_v2|' . json_encode($latest ?: array()));
} elseif ($scope === 'forum') {
    $fid = intval($_GET['fid'] ?? 0);

    if (!$fid) {
        echo json_encode(array(
            'code' => 400,
            'message' => 'fid不能为空'
        ), JSON_UNESCAPED_UNICODE);
        exit;
    }

    $latestPostTime = DB::result_first(
        "SELECT MAX(lastpost)
         FROM pre_forum_thread
         WHERE fid=%d",
        array($fid)
    );

    $threadCount = DB::result_first(
        "SELECT COUNT(*)
         FROM pre_forum_thread
         WHERE fid=%d",
        array($fid)
    );

    $version = md5('forum|' . $fid . '|' . $latestPostTime . '|' . $threadCount);
} elseif ($scope === 'forums') {
    $latestForum = DB::fetch_first(
        "SELECT fid, displayorder, name
         FROM pre_forum_forum
         WHERE type='forum'
         ORDER BY fid DESC
         LIMIT 1"
    );

    $version = md5('forums|' . json_encode($latestForum ?: array()));
} else {
    echo json_encode(array(
        'code' => 400,
        'message' => '不支持的同步范围'
    ), JSON_UNESCAPED_UNICODE);
    exit;
}

echo json_encode(array(
    'code' => 0,
    'data' => array(
        'version' => $version
    )
), JSON_UNESCAPED_UNICODE);
