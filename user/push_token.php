<?php

define('IN_API', true);
require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();
header('Content-Type: application/json; charset=utf-8');

$token = isset($_SERVER['HTTP_X_TOKEN']) ? $_SERVER['HTTP_X_TOKEN'] : '';
$tokenData = $token ? C::t('app_token')->get_by_token($token) : null;

if (!$tokenData || intval($tokenData['expire']) < TIMESTAMP) {
    echo json_encode(array('code' => 401, 'message' => '请先登录'), JSON_UNESCAPED_UNICODE);
    exit;
}

$fcmToken = isset($_POST['token']) ? trim($_POST['token']) : '';
if (strlen($fcmToken) < 20 || strlen($fcmToken) > 255) {
    echo json_encode(array('code' => 400, 'message' => '推送标识无效'), JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    DB::query("CREATE TABLE IF NOT EXISTS `pre_app_push_token` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `uid` int(10) unsigned NOT NULL,
        `token` varchar(255) NOT NULL,
        `platform` varchar(20) NOT NULL DEFAULT 'android',
        `dateline` int(10) unsigned NOT NULL DEFAULT '0',
        PRIMARY KEY (`id`),
        UNIQUE KEY `token` (`token`),
        KEY `uid` (`uid`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8");

    DB::query(
        "INSERT INTO pre_app_push_token (uid, token, platform, dateline)
         VALUES (%d, %s, 'android', %d)
         ON DUPLICATE KEY UPDATE uid = VALUES(uid), dateline = VALUES(dateline)",
        array(intval($tokenData['uid']), $fcmToken, TIMESTAMP)
    );

    echo json_encode(array('code' => 0, 'message' => '推送设备已登记'), JSON_UNESCAPED_UNICODE);
} catch (Exception $error) {
    echo json_encode(array('code' => 500, 'message' => '推送设备登记失败'), JSON_UNESCAPED_UNICODE);
}
