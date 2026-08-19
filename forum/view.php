<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();


header('Content-Type: application/json; charset=utf-8');


$tid = intval($_GET['tid']);


if(!$tid){

    echo json_encode([
        'code'=>400,
        'message'=>'缺少tid'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



/*
 * 获取主题
 */

$thread = DB::fetch_first(
    "SELECT 
        tid,
        subject,
        author,
        views,
        replies,
        dateline
     FROM %t
     WHERE tid=%d",
     array(
        'forum_thread',
        $tid
     )
);



if(!$thread){

    echo json_encode([
        'code'=>404,
        'message'=>'帖子不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



/*
 * 获取回复内容
 */

$posts = DB::fetch_all(
    "SELECT
        pid,
        tid,
        author,
        message,
        dateline
     FROM %t
     WHERE tid=%d
     ORDER BY pid ASC
     LIMIT 50",
     array(
        'forum_post',
        $tid
     )
);



echo json_encode([

    'code'=>0,

    'thread'=>$thread,

    'posts'=>$posts

],JSON_UNESCAPED_UNICODE);


