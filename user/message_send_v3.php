<?php
/**
 * APP 私信发送接口（兼容版）
 *
 * 使用 Discuz / UCenter 原生 uc_pm_send()，避免直接写私信表时因
 * 不同 Discuz 版本的字段结构差异而出现数据库错误。
 */
define('IN_API', true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
C::app()->init();

header('Content-Type: application/json; charset=utf-8');
ob_start();

function app_pm_response($code, $message, $data = array()) {
    if (ob_get_length()) {
        ob_clean();
    }
    echo json_encode(array(
        'code' => intval($code),
        'message' => $message,
        'data' => $data
    ), JSON_UNESCAPED_UNICODE);
    exit;
}

$token = $_SERVER['HTTP_X_TOKEN'] ?? '';
if (!$token) {
    app_pm_response(401, '登录凭证缺失，请重新登录');
}

$tokenData = C::t('app_token')->get_by_token($token);
if (!$tokenData || intval($tokenData['expire']) < TIMESTAMP) {
    app_pm_response(401, '登录已过期，请重新登录');
}

$fromUid = intval($tokenData['uid']);
$toUid = intval($_POST['uid'] ?? 0);
$message = trim($_POST['message'] ?? '');

if ($toUid <= 0 || $message === '') {
    app_pm_response(400, '接收用户和消息内容不能为空');
}
if ($toUid === $fromUid) {
    app_pm_response(400, '不能给自己发送私信');
}

$from = DB::fetch_first(
    'SELECT uid, username FROM pre_common_member WHERE uid=%d',
    array($fromUid)
);
$to = DB::fetch_first(
    'SELECT uid, username FROM pre_common_member WHERE uid=%d',
    array($toUid)
);

if (!$from || !$to) {
    app_pm_response(404, '接收用户不存在');
}

/*
 * 使用 Discuz 网页端同一套 sendpm() 逻辑，让论坛按当前版本的
 * 私信表结构、好友限制和用户设置处理消息。
 */
$coreFunction = '/www/wwwroot/qq/wwwroot/source/function/function_core.php';
if (!function_exists('sendpm') && is_readable($coreFunction)) {
    require_once $coreFunction;
}

if (function_exists('sendpm')) {
    $pmid = intval(sendpm($toUid, '', $message, $fromUid, 0, 0, 0));
} else {
    /*
     * 旧版本的兼容备用：这里的第二个参数必须是 UID，
     * 不是用户名；第七个参数 isusername 为 0。
     */
    $ucClient = '/www/wwwroot/qq/wwwroot/uc_client/client.php';
    if (is_readable($ucClient)) {
        require_once $ucClient;
    }
    if (!function_exists('uc_pm_send')) {
        app_pm_response(500, '论坛私信服务未加载，请检查 source/function 目录');
    }
    $pmid = intval(uc_pm_send($fromUid, $toUid, '', $message, 1, 0, 0, 0));
}

if ($pmid <= 0) {
    $messages = array(
        -1 => '私信发送失败',
        -2 => '接收用户不存在或被禁用',
        -3 => '您没有发送私信的权限',
        -4 => '接收用户拒收私信'
    );
    app_pm_response(400, $messages[$pmid] ?? '私信发送失败，请稍后重试');
}

app_pm_response(0, '发送成功', array(
    'pmid' => $pmid,
    'to_uid' => $toUid
));
