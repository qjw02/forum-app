<?php

define('IN_API', true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';

C::app()->init();

require_once '/www/wwwroot/qq/wwwroot/api/common/sign.php';

header('Content-Type: application/json; charset=utf-8');

if (!check_api_sign()) {
    echo json_encode(array('code' => 403, 'message' => '签名错误'), JSON_UNESCAPED_UNICODE);
    exit;
}

$uid = intval(isset($_GET['uid']) ? $_GET['uid'] : 0);
if (!$uid) {
    echo json_encode(array('code' => 400, 'message' => '用户ID不能为空'), JSON_UNESCAPED_UNICODE);
    exit;
}

$user = DB::fetch_first(
    "SELECT m.uid, m.username, m.regdate, m.groupid,
            g.grouptitle AS group_name,
            c.extcredits1 AS credits,
            c.extcredits4 AS money
     FROM pre_common_member m
     LEFT JOIN pre_common_usergroup g ON g.groupid = m.groupid
     LEFT JOIN pre_common_member_count c ON c.uid = m.uid
     WHERE m.uid = %d",
    array($uid)
);

if (!$user) {
    echo json_encode(array('code' => 404, 'message' => '用户不存在'), JSON_UNESCAPED_UNICODE);
    exit;
}

$threads = DB::result_first(
    "SELECT COUNT(*) FROM pre_forum_thread WHERE authorid = %d",
    array($uid)
);

$replies = DB::result_first(
    "SELECT COUNT(*) FROM pre_forum_post WHERE authorid = %d AND first = 0",
    array($uid)
);

$favorites = DB::result_first(
    "SELECT COUNT(*) FROM pre_home_favorite WHERE uid = %d AND idtype = 'tid'",
    array($uid)
);

$scheme = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
$host = isset($_SERVER['HTTP_HOST']) ? $_SERVER['HTTP_HOST'] : '';
$avatar = $host
    ? $scheme . '://' . $host . '/uc_server/avatar.php?uid=' . $uid . '&size=middle'
    : '';

echo json_encode(array(
    'code' => 0,
    'message' => 'success',
    'data' => array(
        'uid' => intval($user['uid']),
        'username' => $user['username'],
        'avatar' => $avatar,
        'group_name' => $user['group_name'] ? $user['group_name'] : '普通会员',
        'credits' => intval($user['credits']),
        'money' => intval($user['money']),
        'threads' => intval($threads),
        'replies' => intval($replies),
        'favorites' => intval($favorites),
        'regdate' => intval($user['regdate'])
    )
), JSON_UNESCAPED_UNICODE);
