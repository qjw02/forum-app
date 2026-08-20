<?php

define('IN_API', true);
require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();
header('Content-Type: application/json; charset=utf-8');

$token = $_SERVER['HTTP_X_TOKEN'] ?? '';
$tokenData = $token ? C::t('app_token')->get_by_token($token) : null;

if (!$tokenData || intval($tokenData['expire']) < TIMESTAMP) {
    echo json_encode(array('code' => 401, 'message' => '请先登录'), JSON_UNESCAPED_UNICODE);
    exit;
}

$uid = intval($tokenData['uid']);

/*
 * Discuz 内置 pre_forum_promotion 表每条记录代表一个通过推广链接
 * 访问站点的独立 IP。注册奖励由 Discuz 的推广规则自动结算，
 * 原生表不会保存“推广人 → 注册会员”的对应关系，因此这里不伪造注册数量。
 */
$visitCount = intval(DB::result_first(
    "SELECT COUNT(*) FROM pre_forum_promotion WHERE uid = %d",
    array($uid)
));

echo json_encode(array(
    'code' => 0,
    'data' => array(
        'visit_count' => $visitCount
    )
), JSON_UNESCAPED_UNICODE);
