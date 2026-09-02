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

try {
    // 直接读取 Discuz 原生推广规则，确保 APP 与网页读取同一份奖励数据。
    $rule = DB::fetch_first(
        "SELECT rid, extcredits1, extcredits2, extcredits3, extcredits4,
                extcredits5, extcredits6, extcredits7, extcredits8
         FROM pre_common_credit_rule
         WHERE rulename = 'promotion_register'
         LIMIT 1"
    );

    $registeredCount = 0;
    $latestRewardTime = 0;
    if ($rule && intval($rule['rid']) > 0) {
        $nativeLog = DB::fetch_first(
            "SELECT COALESCE(SUM(total), 0) AS registered_count,
                    COALESCE(MAX(dateline), 0) AS latest_dateline
             FROM pre_common_credit_rule_log
             WHERE uid = %d AND rid = %d",
            array($uid, intval($rule['rid']))
        );
        $registeredCount = intval($nativeLog['registered_count']);
        $latestRewardTime = intval($nativeLog['latest_dateline']);
    }

    $visitCount = intval(DB::result_first(
        "SELECT COUNT(*) FROM pre_forum_promotion WHERE uid = %d",
        array($uid)
    ));

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

    $totalMoney = $registeredCount * $rewardValue('金钱');
    $totalCoin = $registeredCount * $rewardValue('C币');
    $totalContribution = $registeredCount * $rewardValue('贡献');

    // Discuz 原生只保存奖励次数，不保存被推广人的名单；返回一条汇总记录给 APP。
    $rewards = array();
    if ($registeredCount > 0) {
        $rewards[] = array(
            'uid' => 0,
            'username' => '论坛原生推广注册（' . $registeredCount . '）',
            'money' => $totalMoney,
            'coin' => $totalCoin,
            'contribution' => $totalContribution,
            'dateline' => $latestRewardTime
        );
    }

    echo json_encode(array(
        'code' => 0,
        'data' => array(
            'visit_count' => $visitCount,
            'registered_count' => $registeredCount,
            'total_money' => $totalMoney,
            'total_coin' => $totalCoin,
            'total_contribution' => $totalContribution,
            'rewards' => $rewards
        )
    ), JSON_UNESCAPED_UNICODE);
} catch (Exception $error) {
    echo json_encode(array('code' => 500, 'message' => '推广统计读取失败：' . $error->getMessage()), JSON_UNESCAPED_UNICODE);
}
