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



$count=DB::result_first(

"SELECT COUNT(*)

FROM pre_home_notification

WHERE uid=%d

AND new=1",

array($uid)

);



echo json_encode([

    'code'=>0,

    'data'=>array(

        'count'=>intval($count)

    )

],JSON_UNESCAPED_UNICODE);

