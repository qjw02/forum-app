<?php

define('IN_API', true);
require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

header('Content-Type: application/json; charset=utf-8');

$token = $_SERVER['HTTP_X_TOKEN'] ?? '';

if (!$token) {
    echo json_encode(array('code' => 401, 'message' => '请先登录'), JSON_UNESCAPED_UNICODE);
    exit;
}

$tokenData = C::t('app_token')->get_by_token($token);

if (!$tokenData || intval($tokenData['expire']) < TIMESTAMP) {
    echo json_encode(array('code' => 401, 'message' => '登录已失效'), JSON_UNESCAPED_UNICODE);
    exit;
}

$uid = intval($tokenData['uid']);

$member = DB::fetch_first(
    "SELECT m.uid, m.username, m.groupid, m.credits,
            c.extcredits4 AS money, c.posts, c.threads,
            g.grouptitle AS group_name
     FROM pre_common_member m
     LEFT JOIN pre_common_member_count c ON c.uid = m.uid
     LEFT JOIN pre_common_usergroup g ON g.groupid = m.groupid
     WHERE m.uid = %d",
    array($uid)
);

if (!$member) {
    echo json_encode(array('code' => 404, 'message' => '用户不存在'), JSON_UNESCAPED_UNICODE);
    exit;
}

$avatar = $_G['siteurl'] . 'uc_server/avatar.php?uid=' . $uid . '&size=middle';

// 自定义 VIP 用户组：永久=22，半年=23，临时=24。
$isVip = in_array(intval($member['groupid']), array(22, 23, 24), true);

echo json_encode(array(
    'code' => 0,
    'data' => array(
        'uid' => $member['uid'],
        'username' => $member['username'],
        'avatar' => $avatar,
        'groupid' => intval($member['groupid']),
        'group_name' => $member['group_name'] ?: '普通会员',
        'is_vip' => $isVip,
        'credits' => intval($member['credits']),
        'money' => intval($member['money']),
        'threads' => intval($member['threads']),
        'posts' => intval($member['posts'])
    )
), JSON_UNESCAPED_UNICODE);
