<?php

error_reporting(0);
ini_set('display_errors', '0');

define('IN_API', true);
require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();
header('Content-Type: application/json; charset=utf-8');

$fid = intval($_GET['fid'] ?? 0);

if (!$fid) {
    echo json_encode(array('code' => 400, 'message' => 'fid不能为空'), JSON_UNESCAPED_UNICODE);
    exit;
}

$forum = DB::fetch_first(
    "SELECT fid, name FROM pre_forum_forum WHERE fid=%d",
    array($fid)
);

if (!$forum) {
    echo json_encode(array('code' => 404, 'message' => '版块不存在'), JSON_UNESCAPED_UNICODE);
    exit;
}

function forum_api_site_url() {
    global $_G;
    if (!empty($_G['siteurl'])) {
        return rtrim($_G['siteurl'], '/');
    }
    $scheme = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https://' : 'http://';
    return $scheme . ($_SERVER['HTTP_HOST'] ?? '');
}

/* 只读取首帖 [img] 图片，避免旧附件分表异常影响整个板块列表。 */
function forum_thread_cover($tid) {
    $message = DB::result_first(
        "SELECT message FROM pre_forum_post WHERE tid=%d AND first=1",
        array($tid)
    );

    if ($message && preg_match('/\[img\](.*?)\[\/img\]/i', $message, $match)) {
        $image = trim($match[1]);
        if (strpos($image, 'http://') === 0 || strpos($image, 'https://') === 0) {
            return $image;
        }
        return forum_api_site_url() . '/' . ltrim($image, '/');
    }

    // 兼容网页编辑器保存的 HTML 图片。
    if ($message && preg_match('/<img[^>]+src=[\'"]([^\'"]+)/i', $message, $match)) {
        $image = trim($match[1]);
        if (strpos($image, 'http://') === 0 || strpos($image, 'https://') === 0) {
            return $image;
        }
        return forum_api_site_url() . '/' . ltrim($image, '/');
    }

    // 网页端和旧主题通常是附件。先读取附件分表的真实图片路径。
    $attachments = DB::fetch_all(
        "SELECT aid, tableid FROM pre_forum_attachment WHERE tid=%d ORDER BY aid ASC",
        array($tid)
    );
    foreach ($attachments as $attachment) {
        $tableid = intval($attachment['tableid']);
        if ($tableid < 0 || $tableid > 9) {
            continue;
        }

        $file = DB::fetch_first(
            "SELECT attachment, isimage FROM pre_forum_attachment_" . $tableid . " WHERE aid=%d",
            array($attachment['aid'])
        );
        if ($file && intval($file['isimage']) && !empty($file['attachment'])) {
            return forum_api_site_url() . '/data/attachment/forum/' . ltrim($file['attachment'], '/');
        }
    }

    return '';
}

$list = DB::fetch_all(
    "SELECT tid, subject, author, views, replies, displayorder
     FROM pre_forum_thread
     WHERE fid=%d
     ORDER BY displayorder DESC, dateline DESC
     LIMIT 50",
    array($fid)
);

foreach ($list as &$thread) {
    $thread['image'] = forum_thread_cover(intval($thread['tid']));
}
unset($thread);

echo json_encode(array(
    'code' => 0,
    'data' => array(
        'fid' => $forum['fid'],
        'name' => $forum['name'],
        'list' => $list
    )
), JSON_UNESCAPED_UNICODE);
