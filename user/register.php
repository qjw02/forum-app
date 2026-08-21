<?php

define('IN_API', true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
C::app()->init();
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';

header('Content-Type: application/json; charset=utf-8');

function register_response($code, $message, $data = array()) {
    echo json_encode(array(
        'code' => $code,
        'message' => $message,
        'data' => $data
    ), JSON_UNESCAPED_UNICODE);
    exit;
}

$username = trim($_POST['username'] ?? '');
$password = $_POST['password'] ?? '';
$email = trim($_POST['email'] ?? '');

if ($username === '' || $password === '' || $email === '') {
    register_response(400, '用户名、密码和邮箱不能为空');
}

if (strlen($username) < 3 || strlen($username) > 15) {
    register_response(400, '用户名长度应为 3 到 15 个字符');
}

if (strlen($password) < 6) {
    register_response(400, '密码至少需要 6 位');
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    register_response(400, '请输入有效的邮箱地址');
}

if (!function_exists('uc_user_register')) {
    $ucClient = defined('DISCUZ_ROOT')
        ? DISCUZ_ROOT . './uc_client/client.php'
        : '/www/wwwroot/qq/wwwroot/uc_client/client.php';

    if (is_readable($ucClient)) {
        require_once $ucClient;
    }
}

if (!function_exists('uc_user_register')) {
    register_response(500, '注册服务未正确加载，请检查服务器的 uc_client 目录');
}

$uid = intval(uc_user_register($username, $password, $email));

if ($uid <= 0) {
    $messages = array(
        -1 => '用户名不符合论坛规则',
        -2 => '用户名包含论坛禁止使用的字符',
        -3 => '用户名已存在，请换一个用户名',
        -4 => '邮箱格式不正确',
        -5 => '此邮箱不允许注册',
        -6 => '此邮箱已被注册'
    );
    register_response(400, isset($messages[$uid]) ? $messages[$uid] : '注册失败，请稍后重试');
}

/*
 * uc_user_register 只负责创建 UCenter 用户。下面补齐 Discuz 本地会员资料，
 * 避免出现“注册成功但不能登录”的情况。
 */
$member = getuserbyuid($uid, 1);
if (empty($member)) {
    global $_G;

    $groupid = intval($_G['setting']['newusergroupid'] ?? 0);
    if ($groupid <= 0) {
        $groupid = 10;
    }

    DB::insert('common_member', array(
        'uid' => $uid,
        'username' => $username,
        'password' => md5(random(10)),
        'email' => $email,
        'adminid' => 0,
        'groupid' => $groupid,
        'regdate' => TIMESTAMP,
        'credits' => 0,
        'timeoffset' => 9999
    ));

    DB::insert('common_member_status', array(
        'uid' => $uid,
        'regip' => $_G['clientip'] ?? '',
        'lastip' => $_G['clientip'] ?? '',
        'lastvisit' => TIMESTAMP,
        'lastactivity' => TIMESTAMP
    ));

    DB::insert('common_member_profile', array('uid' => $uid));
    DB::insert('common_member_field_forum', array('uid' => $uid));
    DB::insert('common_member_field_home', array('uid' => $uid));
    DB::insert('common_member_count', array('uid' => $uid));
}

register_response(0, '注册成功，请使用新账号登录', array('uid' => $uid));
