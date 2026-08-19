<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


require_once '/www/wwwroot/qq/wwwroot/api/common/sign.php';


header('Content-Type: application/json; charset=utf-8');



if(!check_api_sign()){

    echo json_encode([
        'code'=>403,
        'message'=>'签名错误'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



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


$id=intval($_POST['id'] ?? 0);



if(!$id){

    echo json_encode([
        'code'=>400,
        'message'=>'通知ID不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



// 更新已读

DB::query(

"UPDATE pre_home_notification

 SET new=0

 WHERE id=%d

 AND uid=%d",

array(
$id,
$uid
)

);



echo json_encode([

    'code'=>0,

    'message'=>'已读'

],JSON_UNESCAPED_UNICODE);

