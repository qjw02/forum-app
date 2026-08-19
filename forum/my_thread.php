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


if(!$data || $data['expire'] < TIMESTAMP){

    echo json_encode([
        'code'=>401,
        'message'=>'token无效'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


$uid=intval($data['uid']);



// 分页

$page=max(1,intval($_GET['page'] ?? 1));

$limit=20;

$start=($page-1)*$limit;



// 我的主题

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

ORDER BY tid DESC

LIMIT %d,%d",

array(
$uid,
$start,
$limit
)

);



$result=array();


foreach($list as $row){


    $result[]=array(

        'tid'=>$row['tid'],

        'fid'=>$row['fid'],

        'subject'=>$row['subject'],

        'views'=>$row['views'],

        'replies'=>$row['replies'],

        'dateline'=>$row['dateline'],

        'lastpost'=>$row['lastpost']

    );


}



echo json_encode([

    'code'=>0,

    'data'=>array(

        'page'=>$page,

        'list'=>$result

    )

],JSON_UNESCAPED_UNICODE);

