<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


require_once '/www/wwwroot/qq/wwwroot/api/common/sign.php';


header('Content-Type: application/json; charset=utf-8');


// 签名验证

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


if(!$data || $data['expire'] < TIMESTAMP){

    echo json_encode([
        'code'=>401,
        'message'=>'token无效'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


// 用户ID

$uid=intval($_GET['uid'] ?? 0);


if(!$uid){

    echo json_encode([
        'code'=>400,
        'message'=>'uid不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


// 分页

$page=max(1,intval($_GET['page'] ?? 1));

$limit=20;

$start=($page-1)*$limit;


// 用户资料

$member=DB::fetch_first(

"SELECT
uid,
username,
groupid,
credits,
regdate

FROM pre_common_member

WHERE uid=%d",

array($uid)

);


if(!$member){

    echo json_encode([
        'code'=>404,
        'message'=>'用户不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


// 用户统计

$count=DB::fetch_first(

"SELECT
threads,
posts

FROM pre_common_member_count

WHERE uid=%d",

array($uid)

);


// 用户主题

$list=DB::fetch_all(

"SELECT
tid,
fid,
subject,
views,
replies,
dateline,
lastpost

FROM pre_forum_thread

WHERE authorid=%d

AND displayorder>=0

ORDER BY dateline DESC

LIMIT %d,%d",

array(
$uid,
$start,
$limit
)

);


// 整理主题

$threads=array();


foreach($list as $row){

    $threads[]=array(

        'tid'=>$row['tid'],

        'fid'=>$row['fid'],

        'subject'=>$row['subject'],

        'views'=>$row['views'],

        'replies'=>$row['replies'],

        'dateline'=>$row['dateline'],

        'lastpost'=>$row['lastpost']

    );

}


// 返回

echo json_encode([

    'code'=>0,

    'data'=>array(

        'user'=>array(

            'uid'=>$member['uid'],

            'username'=>$member['username'],

            'avatar'=>'https://a3x9r3.cdlf3.com/uc_server/avatar.php?uid='.$uid.'&size=middle',

            'groupid'=>$member['groupid'],

            'credits'=>$member['credits'],

            'regdate'=>$member['regdate'],

            'threads'=>$count['threads'] ?? 0,

            'posts'=>$count['posts'] ?? 0

        ),

        'threads'=>array(

            'page'=>$page,

            'list'=>$threads

        )

    )

],JSON_UNESCAPED_UNICODE);

