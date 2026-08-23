<?php
define('IN_API', true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
C::app()->init();

header('Content-Type: application/json; charset=utf-8');

function app_response($code, $message, $data = array()) {
    echo json_encode(array(
        'code' => $code,
        'message' => $message,
        'data' => $data
    ), JSON_UNESCAPED_UNICODE);
    exit;
}

$token = trim($_SERVER['HTTP_X_TOKEN'] ?? '');
if ($token === '') {
    app_response(401, 'token缺失');
}

$tokenRow = C::t('app_token')->get_by_token($token);
if (!$tokenRow || intval($tokenRow['expire']) < TIMESTAMP) {
    app_response(401, 'token无效或已过期');
}

$uid = intval($tokenRow['uid']);
$tid = intval($_POST['tid'] ?? 0);
$pid = intval($_POST['pid'] ?? 0);

if ($tid <= 0 || $pid <= 0) {
    app_response(400, '回复参数错误');
}

$post = DB::fetch_first(
    'SELECT pid, tid, authorid, first FROM ' . DB::table('forum_post') . ' WHERE pid=%d AND tid=%d',
    array($pid, $tid)
);

if (!$post || intval($post['first']) !== 0) {
    app_response(404, '回复不存在');
}

if (intval($post['authorid']) !== $uid) {
    app_response(403, '只能删除自己的回复');
}

DB::delete('forum_post', array('pid' => $pid, 'tid' => $tid));

DB::query(
    'UPDATE ' . DB::table('forum_thread') . ' SET replies=IF(replies>0, replies-1, 0) WHERE tid=%d',
    array($tid)
);

app_response(0, '回复已删除', array('tid' => $tid, 'pid' => $pid));
