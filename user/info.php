<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



/*
 * 获取Token
 */

$token='';


if(isset($_SERVER['HTTP_X_TOKEN'])){

    $token=$_SERVER['HTTP_X_TOKEN'];

}



if(!$token){

    echo json_encode([
        'code'=>401,
        'message'=>'token缺失'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}




/*
 * 验证token
 */

$data=C::t('app_token')->get_by_token($token);



if(!$data){

    echo json_encode([
        'code'=>401,
        'message'=>'token无效'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}




if($data['expire'] < TIMESTAMP){

    echo json_encode([
        'code'=>401,
        'message'=>'token过期'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}




/*
 * 获取用户
 */

$member=getuserbyuid($data['uid'],1);



if(!$member){

    echo json_encode([
        'code'=>404,
        'message'=>'用户不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



/*
 * 获取头像
 */

$avatar='';


if($member['avatarstatus']){

    $avatar=$_G['setting']['ucenterurl'].'/avatar.php?uid='.$member['uid'].'&size=middle';

}



/*
 * 返回数据
 */

echo json_encode([

    'code'=>0,

    'user'=>[

        'uid'=>$member['uid'],

        'username'=>$member['username'],

        'email'=>$member['email'],

        'credits'=>$member['credits'],

        'avatar'=>$avatar,

        'regdate'=>$member['regdate']

    ]

],JSON_UNESCAPED_UNICODE);

