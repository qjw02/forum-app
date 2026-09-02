<?php

/*
 * 论坛原生注册推广记录器。
 * 由 source/class/class_member.php 在原生推广积分发放后调用。
 * 本文件只写入统计记录，绝不再次增加任何积分。
 */

function app_native_referral_ensure_table() {
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

function app_native_referral_reward($title) {
    global $_G;
    $rule = DB::fetch_first(
        "SELECT extcredits1, extcredits2, extcredits3, extcredits4,
                extcredits5, extcredits6, extcredits7, extcredits8
         FROM pre_common_credit_rule
         WHERE action = 'promotion_register'
         LIMIT 1"
    );

    if (!$rule) return 0;
    foreach (($_G['setting']['extcredits'] ?? array()) as $id => $credit) {
        if (trim($credit['title'] ?? '') === $title) {
            return intval($rule['extcredits' . intval($id)] ?? 0);
        }
    }
    return 0;
}

function app_record_native_referral($referrerUid, $referredUid) {
    $referrerUid = intval($referrerUid);
    $referredUid = intval($referredUid);
    if ($referrerUid <= 0 || $referredUid <= 0 || $referrerUid === $referredUid) {
        return false;
    }

    app_native_referral_ensure_table();

    // 每个新会员只记一次；不会影响 Discuz 已完成的原生积分发放。
    $exists = intval(DB::result_first(
        "SELECT id FROM pre_app_referral_log WHERE referred_uid = %d",
        array($referredUid)
    ));
    if ($exists > 0) {
        return false;
    }

    DB::insert('app_referral_log', array(
        'referrer_uid' => $referrerUid,
        'referred_uid' => $referredUid,
        'money_reward' => app_native_referral_reward('金钱'),
        'coin_reward' => app_native_referral_reward('C币'),
        'contribution_reward' => app_native_referral_reward('贡献'),
        'source' => 'native',
        'dateline' => TIMESTAMP
    ));
    return true;
}
