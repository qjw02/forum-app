<?php

define('IN_API', true);
require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();
header('Content-Type: application/json; charset=utf-8');

function permission_response($code, $message, $data = array()) {
    echo json_encode(array(
        'code' => intval($code),
        'message' => $message,
        'data' => $data
    ), JSON_UNESCAPED_UNICODE);
    exit;
}

$fid = intval($_GET['fid'] ?? 0);
$uid = intval($_GET['uid'] ?? 0);

if ($fid <= 0) {
    permission_response(400, '板块参数不能为空');
}

$forum = DB::fetch_first(
    "SELECT fid FROM " . DB::table('forum_forum') . " WHERE fid = %d AND type = 'forum'",
    array($fid)
);
if (!$forum) {
    permission_response(404, '板块不存在');
}

if ($uid > 0) {
    /*
     * 直接读取会员表，避免 API 文件未加载 function_member.php 时输出 PHP 错误页面。
     */
    $member = DB::fetch_first(
        "SELECT uid, groupid, credits FROM " . DB::table('common_member') . " WHERE uid = %d",
        array($uid)
    );
    if (!$member) {
        permission_response(401, '用户不存在');
    }
    $groupid = intval($member['groupid']);
    $credits = intval($member['credits']);
} else {
    $groupid = 7; // Discuz 默认游客组
    $credits = 0;
}

$group = DB::fetch_first(
    "SELECT groupid, grouptitle, allowvisit, allowpost
     FROM " . DB::table('common_usergroup') . " WHERE groupid = %d",
    array($groupid)
);
if (!$group) {
    permission_response(403, '当前用户组不存在');
}

if (intval($group['allowvisit']) <= 0) {
    permission_response(403, '当前用户组无权访问论坛');
}

$access = DB::fetch_first(
    "SELECT allowview, allowpost
     FROM " . DB::table('forum_forum_access') . "
     WHERE fid = %d AND groupid = %d",
    array($fid, $groupid)
);

if ($access && intval($access['allowview']) <= 0) {
    permission_response(403, '当前用户组无权访问该板块');
}

/*
 * 权限完全以 Discuz 后台的“用户组”和“板块权限”为准。
 * APP 仅显示结果；post.php 会作同样的最终拦截。
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