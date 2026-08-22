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
 * 本站的 UCenter 私信远程接口会拒绝 API 调用，因此在论坛本地
 * 私信表中写入。查找会话只使用当前数据库已经存在的 plid / uid
 * 字段，不使用某些版本没有的 min_max 字段。
 */
$conversation = DB::fetch_first(
    'SELECT l.plid
     FROM pre_ucenter_pm_lists l
     INNER JOIN pre_ucenter_pm_members sender ON sender.plid=l.plid AND sender.uid=%d
     INNER JOIN pre_ucenter_pm_members receiver ON receiver.plid=l.plid AND receiver.uid=%d
     WHERE l.pmtype=1
     ORDER BY l.dateline DESC
     LIMIT 1',
    array($fromUid, $toUid)
);

if ($conversation) {
    $plid = intval($conversation['plid']);
} else {
    DB::insert('ucenter_pm_lists', array(
        'authorid' => $fromUid,
        'pmtype' => 1,
        'subject' => mb_substr($message, 0, 80, 'UTF-8'),
        'members' => 2,
        'dateline' => TIMESTAMP,
        'lastmessage' => $message
    ));
    $plid = intval(DB::insert_id());

    DB::insert('ucenter_pm_members', array(
        'plid' => $plid,
        'uid' => $fromUid,
        'isnew' => 0,
        'pmnum' => 1,
        'lastupdate' => TIMESTAMP,
        'lastdateline' => TIMESTAMP
    ));
    DB::insert('ucenter_pm_members', array(
        'plid' => $plid,
        'uid' => $toUid,
        'isnew' => 1,
        'pmnum' => 1,
        'lastupdate' => TIMESTAMP,
        'lastdateline' => TIMESTAMP
    ));
}

$messageTable = 'ucenter_pm_messages_'.($plid % 10);

/*
 * 当前论坛的私信分表 pmid 没有自动递增属性，未传入时会默认为 0，
 * 从而触发主键重复。按当前分表的最大编号生成下一条编号。
 */
$nextPmid = intval(DB::result_first(
    'SELECT IFNULL(MAX(pmid), 0) + 1 FROM pre_'.$messageTable
));

DB::insert($messageTable, array(
    'pmid' => $nextPmid,
    'plid' => $plid,
    'authorid' => $fromUid,
    'message' => $message,
    'delstatus' => 0,
    'dateline' => TIMESTAMP
));

DB::query(
    'UPDATE pre_ucenter_pm_lists SET lastmessage=%s, dateline=%d WHERE plid=%d',
    array($message, TIMESTAMP, $plid)
);
DB::query(
    'UPDATE pre_ucenter_pm_members
     SET isnew=1, lastupdate=%d, lastdateline=%d, pmnum=pmnum+1
     WHERE plid=%d AND uid=%d',
    array(TIMESTAMP, TIMESTAMP, $plid, $toUid)
);

$pmid = $plid;

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
