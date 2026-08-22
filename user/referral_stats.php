<?php

define('IN_API', true);
require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();
header('Content-Type: application/json; charset=utf-8');

function app_referral_ensure_table() {
    DB::query("CREATE TABLE IF NOT EXISTS `pre_app_referral_log` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `referrer_uid` int(10) unsigned NOT NULL,
        `referred_uid` int(10) unsigned NOT NULL,
        `money_reward` int(10) NOT NULL DEFAULT '50',
        `coin_reward` int(10) NOT NULL DEFAULT '10',
        `contribution_reward` int(10) NOT NULL DEFAULT '1',
        `dateline` int(10) unsigned NOT NULL DEFAULT '0',
        PRIMARY KEY (`id`),
        UNIQUE KEY `referred_uid` (`referred_uid`),
        KEY `referrer_uid` (`referrer_uid`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8");
}

$token = $_SERVER['HTTP_X_TOKEN'] ?? '';
$tokenData = $token ? C::t('app_token')->get_by_token($token) : null;

if (!$tokenData || intval($tokenData['expire']) < TIMESTAMP) {
    echo json_encode(array('code' => 401, 'message' => '请先登录'), JSON_UNESCAPED_UNICODE);
    exit;
}

$uid = intval($tokenData['uid']);

try {
    app_referral_ensure_table();

    // Discuz 原生表保留为推广 IP 访问统计。
    $visitCount = intval(DB::result_first(
        "SELECT COUNT(*) FROM pre_forum_promotion WHERE uid = %d",
        array($uid)
    ));

    $summary = DB::fetch_first(
        "SELECT COUNT(*) AS registered_count,
                COALESCE(SUM(money_reward), 0) AS total_money,
                COALESCE(SUM(coin_reward), 0) AS total_coin,
                COALESCE(SUM(contribution_reward), 0) AS total_contribution
         FROM pre_app_referral_log WHERE referrer_uid = %d",
        array($uid)
    );

    $rows = DB::fetch_all(
        "SELECT l.referred_uid, l.money_reward, l.coin_reward,
                l.contribution_reward, l.dateline, m.username
         FROM pre_app_referral_log l
         LEFT JOIN pre_common_member m ON m.uid = l.referred_uid
         WHERE l.referrer_uid = %d
         ORDER BY l.id DESC LIMIT 20",
        array($uid)
    );

    $rewards = array();
    foreach ($rows as $row) {
        $rewards[] = array(
            'uid' => intval($row['referred_uid']),
            'username' => !empty($row['username']) ? $row['username'] : '新会员',
            'money' => intval($row['money_reward']),
            'coin' => intval($row['coin_reward']),
            'contribution' => intval($row['contribution_reward']),
            'dateline' => intval($row['dateline'])
        );
    }

    echo json_encode(array(
        'code' => 0,
        'data' => array(
            'visit_count' => $visitCount,
            'registered_count' => intval($summary['registered_count']),
            'total_money' => intval($summary['total_money']),
            'total_coin' => intval($summary['total_coin']),
            'total_contribution' => intval($summary['total_contribution']),
            'rewards' => $rewards
        )
    ), JSON_UNESCAPED_UNICODE);
} catch (Exception $error) {
    echo json_encode(array('code' => 500, 'message' => '推广统计读取失败：' . $error->getMessage()), JSON_UNESCAPED_UNICODE);
}
