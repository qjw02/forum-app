<?php

define('IN_API', true);
require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();
header('Content-Type: application/json; charset=utf-8');

function permission_response($code, $message, $data = array()) {
    echo json_encode(array(
        'code' => $code,
        'message' => $message,
        'data' => $data
    ), JSON_UNESCAPED_UNICODE);
    exit;
}

$fid = intval($_GET['fid'] ?? 0);
$uid = intval($_GET['uid'] ?? 0);

if (!$fid) {
    permission_response(400, '板块参数不能为空');
}

$forum = DB::fetch_first(
    "SELECT fid, name FROM " . DB::table('forum_forum') . " WHERE fid = %d AND type = 'forum'",
    array($fid)
);
if (!$forum) {
    permission_response(404, '板块不存在');
}

if ($uid > 0) {
    $member = getuserbyuid($uid, 1);
    if (!$member) {
        permission_response(401, '用户不存在');
    }
    $groupid = intval($member['groupid']);
    $credits = intval($member['credits']);
} else {
    // Discuz 默认游客组。
    $groupid = 7;
    $credits = 0;
}

$group = DB::fetch_first(
    "SELECT groupid, grouptitle, allowvisit, allowpost
     FROM " . DB::table('common_usergroup') . " WHERE groupid = %d",
    array($groupid)
);
if (!$group || intval($group['allowvisit']) !== 1) {
    permission_response(403, '当前用户组无权访问论坛');
}

$access = DB::fetch_first(
    "SELECT allowview, allowpost
     FROM " . DB::table('forum_forum_access') . "
     WHERE fid = %d AND groupid = %d",
    array($fid, $groupid)
);

$allowView = !$access || intval($access['allowview']) > 0;
if (!$allowView) {
    permission_response(403, '当前用户组无权访问该板块');
}

/*
 * Discuz 权限以用户组为基础，若该板块设置了单独权限，则还需通过 allowpost。
 * 这里不另造 VIP 规则；你在论坛后台调整用户组或板块权限后，APP 会自动遵守。
 */
$allowPost = $uid > 0 && intval($group['allowpost']) > 0;
if ($access && intval($access['allowpost']) <= 0) {
    $allowPost = false;
}

permission_response(0, '权限正常', array(
    'allow' => true,
    'allow_post' => $allowPost,
    'groupid' => $groupid,
    'group_name' => $group['grouptitle'] ?: '普通会员',
    'credits' => $credits
));