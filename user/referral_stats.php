<?php

define('IN_API', true);
require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();
header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');

function app_referral_ensure_table() {
    DB::query("CREATE TABLE IF NOT EXISTS `pre_app_referral_log` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `referrer_uid` int(10) unsigned NOT NULL,
        `referred_uid` int(10) unsigned NOT NULL,
        `money_reward` int(10) NOT NULL DEFAULT '0',
        `coin_reward` int(10) NOT NULL DEFAULT '0',
        `contribution_reward` int(10) NOT NULL DEFAULT '0',
        `source` varchar(16) NOT NULL DEFAULT 'app',
        `dateline` int(10) unsigned NOT NULL DEFAULT '0',
        PRIMARY KEY (`id`),
        UNIQUE KEY `referred_uid` (`referred_uid`),
        KEY `referrer_uid` (`referrer_uid`),
        KEY `source` (`source`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8");

    $sourceColumn = DB::fetch_first("SHOW COLUMNS FROM `pre_app_referral_log` LIKE 'source'");
    if (empty($sourceColumn)) {
        DB::query("ALTER TABLE `pre_app_referral_log`
            ADD COLUMN `source` varchar(16) NOT NULL DEFAULT 'app' AFTER `contribution_reward`");
        DB::query("ALTER TABLE `pre_app_referral_log` ADD KEY `source` (`source`)");
    }
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

    // 原生 Discuz 已实际发放的推广注册奖励。
    $rule = DB::fetch_first(
        "SELECT rid, extcredits1, extcredits2, extcredits3, extcredits4,
                extcredits5, extcredits6, extcredits7, extcredits8
         FROM pre_common_credit_rule
         WHERE rulename = 'promotion_register'
         LIMIT 1"
    );
    $nativeCount = 0;
    $nativeLatestTime = 0;
    if ($rule && intval($rule['rid']) > 0) {
        $nativeLog = DB::fetch_first(
            "SELECT COALESCE(SUM(total), 0) AS registered_count,
                    COALESCE(MAX(dateline), 0) AS latest_dateline
             FROM pre_common_credit_rule_log
             WHERE uid = %d AND rid = %d",
            array($uid, intval($rule['rid']))
        );
        $nativeCount = intval($nativeLog['registered_count']);
        $nativeLatestTime = intval($nativeLog['latest_dateline']);
    }

    $credits = $_G['setting']['extcredits'] ?? array();
    $rewardValue = function ($title) use ($credits, $rule) {
        if (!$rule) return 0;
        foreach ($credits as $id => $credit) {
            if (trim($credit['title'] ?? '') === $title) {
                return intval($rule['extcredits' . intval($id)] ?? 0);
            }
        }
        return 0;
    };
    $nativeMoney = $nativeCount * $rewardValue('金钱');
    $nativeCoin = $nativeCount * $rewardValue('C币');
    $nativeContribution = $nativeCount * $rewardValue('贡献');

    // 早期 APP 注册走的是独立奖励流程，和原生奖励不重复。
    $appSummary = DB::fetch_first(
        "SELECT COUNT(*) AS registered_count,
                COALESCE(SUM(money_reward), 0) AS total_money,
                COALESCE(SUM(coin_reward), 0) AS total_coin,
                COALESCE(SUM(contribution_reward), 0) AS total_contribution
         FROM pre_app_referral_log
         WHERE referrer_uid = %d AND source = 'app'",
        array($uid)
    );

    // 已记录的实际被邀请会员名单，包含 APP 和之后的网站原生注册。
    $recordedRows = DB::fetch_all(
        "SELECT l.referred_uid, l.money_reward, l.coin_reward,
                l.contribution_reward, l.dateline, m.username
         FROM pre_app_referral_log l
         LEFT JOIN pre_common_member m ON m.uid = l.referred_uid
         WHERE l.referrer_uid = %d
         ORDER BY l.id DESC LIMIT 20",
        array($uid)
    );
    $rewards = array();
    $nativeRows = 0;
    foreach ($recordedRows as $row) {
        if (($row['source'] ?? '') === 'native') $nativeRows++;
        $rewards[] = array(
            'uid' => intval($row['referred_uid']),
            'username' => !empty($row['username']) ? $row['username'] : '新会员',
            'money' => intval($row['money_reward']),
            'coin' => intval($row['coin_reward']),
            'contribution' => intval($row['contribution_reward']),
            'dateline' => intval($row['dateline'])
        );
    }

    // 旧原生推广没有可追溯成员名单时，保留一条汇总提示。
    if ($nativeCount > 0 && $nativeRows === 0) {
        $rewards[] = array(
            'uid' => 0,
            'username' => '历史网站推广注册（' . $nativeCount . '）',
            'money' => $nativeMoney,
            'coin' => $nativeCoin,
            'contribution' => $nativeContribution,
            'dateline' => $nativeLatestTime
        );
    }

    $visitCount = intval(DB::result_first(
        "SELECT COUNT(*) FROM pre_forum_promotion WHERE uid = %d",
        array($uid)
    ));

    echo json_encode(array(
        'code' => 0,
        'data' => array(
            'visit_count' => $visitCount,
            'registered_count' => $nativeCount + intval($appSummary['registered_count']),
            'total_money' => $nativeMoney + intval($appSummary['total_money']),
            'total_coin' => $nativeCoin + intval($appSummary['total_coin']),
            'total_contribution' => $nativeContribution + intval($appSummary['total_contribution']),
            'rewards' => $rewards
        )
    ), JSON_UNESCAPED_UNICODE);
} catch (Exception $error) {
    echo json_encode(array('code' => 500, 'message' => '推广统计读取失败：' . $error->getMessage()), JSON_UNESCAPED_UNICODE);
}
