<?php
define('IN_API', true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

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
if ($tid <= 0) app_response(400, '主题参数错误');

$thread = DB::fetch_first(
    'SELECT tid, authorid FROM '.DB::table('forum_thread').' WHERE tid=%d',
    array($tid)
);
if (!$thread) app_response(404, '主题不存在');
if (intval($thread['authorid']) !== $uid) app_response(403, '只能删除自己的主题');

/*
 * 删除主题正文、分类信息和 APP 的联系方式购买记录。
 * 附件文件会由论坛后台的附件清理机制处理，避免误删其他引用文件。
 */
DB::delete('forum_post', array('tid' => $tid));
DB::delete('forum_typeoptionvar', array('tid' => $tid));
DB::delete('app_thread_field_price', array('tid' => $tid));
DB::delete('app_field_buy', array('tid' => $tid));
DB::delete('forum_thread', array('tid' => $tid));

app_response(0, '主题已删除', array('tid' => $tid));
