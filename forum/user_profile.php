<?php

error_reporting(0);
ini_set('display_errors', '0');

if (!defined('IN_API')) {
    define('IN_API', true);
}

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
C::app()->init();

header('Content-Type: application/json; charset=utf-8');

function public_profile_response($code, $message, $data = null) {
    echo json_encode(array(
        'code' => intval($code),
        'message' => $message,
        'data' => $data
    ), JSON_UNESCAPED_UNICODE);
    exit;
}

$uid = intval($_GET['uid'] ?? 0);
if ($uid <= 0) {
    public_profile_response(400, '用户ID不能为空');
}

$user = DB::fetch_first(
    "SELECT m.uid, m.username, m.regdate, m.groupid, m.credits,
            g.grouptitle AS group_name,
            c.extcredits4 AS money
     FROM " . DB::table('common_member') . " m
     LEFT JOIN " . DB::table('common_usergroup') . " g ON g.groupid = m.groupid
     LEFT JOIN " . DB::table('common_member_count') . " c ON c.uid = m.uid
     WHERE m.uid = %d",
    array($uid)
);

if (!$user) {
    public_profile_response(404, '用户不存在');
}

$threads = DB::result_first(
    "SELECT COUNT(*) FROM " . DB::table('forum_thread') . " WHERE authorid = %d",
    array($uid)
);

$replies = DB::result_first(
    "SELECT COUNT(*) FROM " . DB::table('forum_post') . " WHERE authorid = %d AND first = 0",
    array($uid)
);

$favorites = DB::result_first(
    "SELECT COUNT(*) FROM " . DB::table('home_favorite') . " WHERE uid = %d AND idtype = 'tid'",
    array($uid)
);

$scheme = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
$host = $_SERVER['HTTP_HOST'] ?? '';
$avatar = $host
    ? $scheme . '://' . $host . '/uc_server/avatar.php?uid=' . $uid . '&size=middle'
    : '';

public_profile_response(0, 'success', array(
    'uid' => intval($user['uid']),
    'username' => $user['username'],
    'avatar' => $avatar,
    'group_name' => $user['group_name'] ?: '普通会员',
    'credits' => intval($user['credits']),
    'money' => intval($user['money']),
    'threads' => intval($threads),
    'replies' => intval($replies),
    'favorites' => intval($favorites),
    'regdate' => intval($user['regdate'])
));
