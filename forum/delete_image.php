<?php
define('IN_API', true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
C::app()->init();

header('Content-Type: application/json; charset=utf-8');

function app_response($code, $message, $data = array()) {
    echo json_encode(array('code' => $code, 'message' => $message, 'data' => $data), JSON_UNESCAPED_UNICODE);
    exit;
}

function normalized_image_path($url) {
    $path = parse_url($url, PHP_URL_PATH);
    return $path ? $path : $url;
}

$token = trim($_SERVER['HTTP_X_TOKEN'] ?? '');
if ($token === '') app_response(401, 'token缺失');

$tokenRow = C::t('app_token')->get_by_token($token);
if (!$tokenRow || intval($tokenRow['expire']) < TIMESTAMP) app_response(401, 'token无效或已过期');

$uid = intval($tokenRow['uid']);
$tid = intval($_POST['tid'] ?? 0);
$image = trim($_POST['image'] ?? '');
if ($tid <= 0 || $image === '') app_response(400, '图片参数错误');

$thread = DB::fetch_first(
    'SELECT tid, authorid FROM '.DB::table('forum_thread').' WHERE tid=%d',
    array($tid)
);
if (!$thread) app_response(404, '主题不存在');
if (intval($thread['authorid']) !== $uid) app_response(403, '只能删除自己主题中的图片');

$targetPath = normalized_image_path($image);
$targetAttachment = '';
$prefix = '/data/attachment/forum/';
$position = strpos($targetPath, $prefix);
if ($position !== false) {
    $targetAttachment = substr($targetPath, $position + strlen($prefix));
}

/* 从主题 BBCode 中移除该图片。 */
$post = DB::fetch_first(
    'SELECT pid, message FROM '.DB::table('forum_post').' WHERE tid=%d AND first=1',
    array($tid)
);
$changed = false;
if ($post) {
    $newMessage = preg_replace_callback(
        '/\[img\](.*?)\[\/img\]/is',
        function($match) use ($targetPath) {
            return normalized_image_path(trim($match[1])) === $targetPath ? '' : $match[0];
        },
        $post['message']
    );
    if ($newMessage !== $post['message']) {
        DB::update('forum_post', array('message' => $newMessage), array('pid' => intval($post['pid'])));
        $changed = true;
    }
}

/* 如果是 Discuz 附件，也删除该附件记录和物理文件。 */
if ($targetAttachment !== '') {
    $attachments = DB::fetch_all(
        'SELECT aid, tableid FROM '.DB::table('forum_attachment').' WHERE tid=%d',
        array($tid)
    );

    foreach ($attachments as $attachment) {
        $tableId = intval($attachment['tableid']);
        $detailTable = 'forum_attachment_'.$tableId;
        $detail = DB::fetch_first(
            'SELECT aid, attachment FROM '.DB::table($detailTable).' WHERE aid=%d',
            array(intval($attachment['aid']))
        );
        if (!$detail || $detail['attachment'] !== $targetAttachment) continue;

        DB::delete($detailTable, array('aid' => intval($attachment['aid'])));
        DB::delete('forum_attachment', array('aid' => intval($attachment['aid'])));
        $changed = true;

        /* 只删除数据库已经匹配到的论坛附件，禁止路径穿越。 */
        if (strpos($targetAttachment, '..') === false) {
            $file = DISCUZ_ROOT.'./data/attachment/forum/'.$targetAttachment;
            if (is_file($file)) @unlink($file);
        }
        break;
    }
}

if (!$changed) app_response(404, '未找到该图片或图片已删除');
app_response(0, '图片已删除', array('tid' => $tid));
