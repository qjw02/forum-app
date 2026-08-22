<?php

define('IN_API', true);
require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
C::app()->init();
require_once __DIR__.'/firebase_push.php';

header('Content-Type: application/json; charset=utf-8');

$token = isset($_SERVER['HTTP_X_TOKEN']) ? $_SERVER['HTTP_X_TOKEN'] : '';
$tokenData = $token ? C::t('app_token')->get_by_token($token) : null;
if (!$tokenData || intval($tokenData['expire']) < TIMESTAMP) {
    echo json_encode(array('code' => 401, 'message' => '请先登录'), JSON_UNESCAPED_UNICODE);
    exit;
}

$uid = intval($tokenData['uid']);
$sent = fcm_send_to_user($uid, 'QJWForum 推送测试', 'Firebase 推送已连接成功', array(
    'type' => 'test',
    'uid' => $uid
));

if ($sent > 0) {
    echo json_encode(array('code' => 0, 'message' => '测试推送已发送'), JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(array('code' => 500, 'message' => '推送未发送，请检查服务器密钥、项目 ID 与网络'), JSON_UNESCAPED_UNICODE);
}
