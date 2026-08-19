<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



$fid=intval($_GET['fid'] ?? 0);



if(!$fid){

    echo json_encode([
        'code'=>400,
        'message'=>'fid不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}




$forum=DB::fetch_first(

"SELECT fid,name

 FROM pre_forum_forum

 WHERE fid=%d",

array($fid)

);



if(!$forum){

    echo json_encode([
        'code'=>404,
        'message'=>'版块不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}




$list=DB::fetch_all(

"SELECT

tid,
subject,
author,
views,
replies

FROM pre_forum_thread

WHERE fid=%d

ORDER BY dateline DESC

LIMIT 50",

array($fid)

);





echo json_encode([

'code'=>0,

'data'=>[

'fid'=>$forum['fid'],

'name'=>$forum['name'],

'list'=>$list

]

],JSON_UNESCAPED_UNICODE);