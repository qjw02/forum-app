<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

header('Content-Type: application/json; charset=utf-8');


$token = '';


// 读取 X-Token
if(isset($_SERVER['HTTP_X_TOKEN'])){

    $token = $_SERVER['HTTP_X_TOKEN'];

}


if(!$token){

    echo json_encode([
        'code'=>401,
        'message'=>'token缺失'
    ],JSON_UNESCAPED_UNICODE);

    exit;
}



$data = C::t('app_token')->get_by_token($token);



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



$member = getuserbyuid($data['uid'],1);



echo json_encode([

    'code'=>0,

    'user'=>[

        'uid'=>$member['uid'],

        'username'=>$member['username'],

        'email'=>$member['email'],

        'credits'=>$member['credits']

    ]

],JSON_UNESCAPED_UNICODE);

