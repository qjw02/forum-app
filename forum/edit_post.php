<?php
define('IN_API', true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';

C::app()->init();
header('Content-Type: application/json; charset=utf-8');

function app_response($code, $message, $data = array()) {
    echo json_encode(array('code' => $code, 'message' => $message, 'data' => $data), JSON_UNESCAPED_UNICODE);
    exit;
}

$token = trim($_SERVER['HTTP_X_TOKEN'] ?? '');
if ($token === '') app_response(401, 'token缺失');

$tokenRow = C::t('app_token')->get_by_token($token);
if (!$tokenRow || intval($tokenRow['expire']) < TIMESTAMP) app_response(401, 'token无效或已过期');

$uid = intval($tokenRow['uid']);
$tid = intval($_POST['tid'] ?? 0);
$subject = trim($_POST['subject'] ?? '');
$message = trim($_POST['message'] ?? '');

if ($tid <= 0 || $subject === '' || $message === '') app_response(400, '标题和内容不能为空');
if (dstrlen($subject) > 80) app_response(400, '标题不能超过 80 个字符');

$thread = DB::fetch_first(
    'SELECT tid, fid, authorid FROM '.DB::table('forum_thread').' WHERE tid=%d',
    array($tid)
);
if (!$thread) app_response(404, '主题不存在');
if (intval($thread['authorid']) !== $uid) app_response(403, '只能编辑自己的主题');

DB::update('forum_thread', array('subject' => $subject, 'lastpost' => TIMESTAMP), array('tid' => $tid));
DB::update('forum_post', array('subject' => $subject, 'message' => dhtmlspecialchars($message)), array('tid' => $tid, 'first' => 1));

app_response(0, '主题已保存', array('tid' => $tid, 'fid' => intval($thread['fid'])));
