<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');





/*
 * 幻灯片
 *
 * 后期可以改数据库
 * 目前读取置顶帖子
 */


$banner = DB::fetch_all(

"SELECT

tid,
subject

FROM pre_forum_thread

WHERE displayorder>0

ORDER BY dateline DESC

LIMIT 5"

);







/*
 * 板块
 */


$forums = DB::fetch_all(

"SELECT

fid,
name

FROM pre_forum_forum

WHERE type='forum'

AND status=1

ORDER BY displayorder ASC

LIMIT 8"

);







/*
 * 热门帖子
 */


$hot = DB::fetch_all(

"SELECT

tid,
subject,
author,
views,
replies

FROM pre_forum_thread

ORDER BY views DESC

LIMIT 10"

);







/*
 * 最新帖子
 */


$new = DB::fetch_all(

"SELECT

tid,
subject,
author,
views,
replies

FROM pre_forum_thread

ORDER BY dateline DESC

LIMIT 20"

);








echo json_encode([


'code'=>0,


'data'=>[


'banner'=>$banner,


'forums'=>$forums,


'hot'=>$hot,


'new'=>$new



]


],JSON_UNESCAPED_UNICODE);