<?php

define('IN_API', true);
require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();
header('Content-Type: application/json; charset=utf-8');

function api_site_url() {
    global $_G;
    return rtrim($_G['siteurl'], '/');
}

/* 返回主题首张正文图片：优先 [img] 标签，再读取图片附件。 */
function thread_cover_image($tid) {
    $message = DB::result_first(
        "SELECT message FROM pre_forum_post WHERE tid=%d AND first=1",
        array($tid)
    );

    if ($message && preg_match('/\[img\](.*?)\[\/img\]/i', $message, $match)) {
        $image = trim($match[1]);
        if (strpos($image, 'http://') === 0 || strpos($image, 'https://') === 0) {
            return $image;
        }
        return api_site_url() . '/' . ltrim($image, '/');
    }

    $attachments = DB::fetch_all(
        "SELECT aid, tableid FROM pre_forum_attachment WHERE tid=%d",
        array($tid)
    );

    foreach ($attachments as $attachment) {
        $tableid = intval($attachment['tableid']);
        $row = DB::fetch_first(
            "SELECT attachment, isimage FROM pre_forum_attachment_" . $tableid . " WHERE aid=%d",
            array($attachment['aid'])
        );

        if ($row && intval($row['isimage'])) {
            return api_site_url() . '/data/attachment/forum/' . $row['attachment'];
        }
    }

    return '';
}

function add_cover_images($threads) {
    foreach ($threads as &$thread) {
        $thread['image'] = thread_cover_image(intval($thread['tid']));
    }
    unset($thread);
    return $threads;
}

/* 幻灯片：精华主题 + 正文首图 */
$banner = DB::fetch_all(
    "SELECT tid, subject
     FROM pre_forum_thread
     WHERE digest>0 AND displayorder>=0
     ORDER BY digest DESC, dateline DESC
     LIMIT 5"
);
$banner = add_cover_images($banner);

/* 板块 */
$forums = DB::fetch_all(
    "SELECT fid, name
     FROM pre_forum_forum
     WHERE type='forum' AND status=1
     ORDER BY displayorder ASC
     LIMIT 8"
);

/* 热门区：置顶主题 + 正文首图 */
$hot = DB::fetch_all(
    "SELECT tid, subject, author, views, replies, displayorder
     FROM pre_forum_thread
     WHERE displayorder>0
     ORDER BY displayorder DESC, lastpost DESC
     LIMIT 10"
);
$hot = add_cover_images($hot);

/* 最新帖子 */
$new = DB::fetch_all(
    "SELECT tid, subject, author, views, replies, displayorder
     FROM pre_forum_thread
     ORDER BY dateline DESC
     LIMIT 20"
);

echo json_encode(array(
    'code' => 0,
    'data' => array(
        'banner' => $banner,
        'forums' => $forums,
        'hot' => $hot,
        'new' => $new
    )
), JSON_UNESCAPED_UNICODE);
