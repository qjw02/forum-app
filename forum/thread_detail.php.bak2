<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';


C::app()->init();


require_once '/www/wwwroot/qq/wwwroot/api/common/sign.php';


header('Content-Type: application/json; charset=utf-8');


// 签名

if(!check_api_sign()){

    echo json_encode([
        'code'=>403,
        'message'=>'签名错误'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


// token

$token=$_SERVER['HTTP_X_TOKEN'] ?? '';

$data=C::t('app_token')->get_by_token($token);


$uid=0;

if($data && $data['expire']>=TIMESTAMP){

    $uid=intval($data['uid']);

}



// 参数

$tid=intval($_GET['tid'] ?? 0);

$page=max(1,intval($_GET['page'] ?? 1));

$limit=20;

$start=($page-1)*$limit;


if(!$tid){

    echo json_encode([
        'code'=>400,
        'message'=>'tid不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



// 主题

$thread=DB::fetch_first(

"SELECT
tid,
fid,
author,
authorid,
subject,
views,
replies,
dateline,
lastpost,
lastposter

FROM pre_forum_thread

WHERE tid=%d",

array($tid)

);



if(!$thread){

    echo json_encode([
        'code'=>404,
        'message'=>'主题不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}




// 楼主内容

$first=DB::fetch_first(

"SELECT
pid,
message,
dateline

FROM pre_forum_post

WHERE tid=%d
AND first=1

LIMIT 1",

array($tid)

);




// 回复

$posts=DB::fetch_all(

"SELECT

p.pid,
p.author,
p.authorid,
p.message,
p.dateline

FROM pre_forum_post p

WHERE p.tid=%d
AND p.first=0
ORDER BY p.pid ASC

LIMIT %d,%d",

array(
$tid,
$start,
$limit
)

);




// 用户头像函数

function app_avatar($uid){

    return 'https://a3x9r3.cdlf3.com/uc_server/avatar.php?uid='
    .$uid
    .'&size=middle';

}




$list=array();


foreach($posts as $p){

    $list[]=array(

        'pid'=>$p['pid'],

        'author'=>array(

            'uid'=>$p['authorid'],

            'username'=>$p['author'],

            'avatar'=>app_avatar($p['authorid'])

        ),

        'message'=>$p['message'],

        'dateline'=>$p['dateline']

    );

}




echo json_encode([

'code'=>0,

'data'=>array(

    'thread'=>array(

        'tid'=>$thread['tid'],

        'fid'=>$thread['fid'],

        'subject'=>$thread['subject'],

        'author'=>array(

            'uid'=>$thread['authorid'],

            'username'=>$thread['author'],

            'avatar'=>app_avatar($thread['authorid'])

        ),

        'views'=>$thread['views'],

        'replies'=>$thread['replies'],

        'dateline'=>$thread['dateline']

    ),


    'content'=>array(

        'pid'=>$first['pid'] ?? 0,

        'message'=>$first['message'] ?? '',

        'dateline'=>$first['dateline'] ?? 0

    ),


    'replies'=>$list,


    'page'=>$page


)

],JSON_UNESCAPED_UNICODE);

