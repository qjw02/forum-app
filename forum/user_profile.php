<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';


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



// 用户ID

$uid=intval($_GET['uid'] ?? 0);


if(!$uid){

    echo json_encode([
        'code'=>400,
        'message'=>'uid不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



// 用户信息

$user=DB::fetch_first(

"SELECT
uid,
username,
regdate

FROM pre_common_member

WHERE uid=%d",

array($uid)

);



if(!$user){

    echo json_encode([
        'code'=>404,
        'message'=>'用户不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



// 主题数量

$threads=DB::result_first(

"SELECT COUNT(*)

FROM pre_forum_thread

WHERE authorid=%d",

array($uid)

);



// 回复数量

$replies=DB::result_first(

"SELECT COUNT(*)

FROM pre_forum_post

WHERE authorid=%d
AND first=0",

array($uid)

);



// 收藏数量

$favorites=DB::result_first(

"SELECT COUNT(*)

FROM pre_home_favorite

WHERE uid=%d
AND idtype='tid'",

array($uid)

);



// 头像

$avatar='https://a3x9r3.cdlf3.com/uc_server/avatar.php?uid='
.$uid
.'&size=middle';



echo json_encode([

    'code'=>0,

    'data'=>array(

        'uid'=>$user['uid'],

        'username'=>$user['username'],

        'avatar'=>$avatar,

        'regdate'=>$user['regdate'],

        'threads'=>intval($threads),

        'replies'=>intval($replies),

        'favorites'=>intval($favorites)

    )

],JSON_UNESCAPED_UNICODE);
