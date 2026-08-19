<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


require_once '/www/wwwroot/qq/wwwroot/api/common/sign.php';


header('Content-Type: application/json; charset=utf-8');


// APP签名验证

if(!check_api_sign()){

    echo json_encode([
        'code'=>403,
        'message'=>'签名错误'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



$token=$_SERVER['HTTP_X_TOKEN'] ?? '';



if(!$token){

    echo json_encode([
        'code'=>401,
        'message'=>'token缺失'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



$data=C::t('app_token')->get_by_token($token);



if(!$data || $data['expire'] < TIMESTAMP){

    echo json_encode([
        'code'=>401,
        'message'=>'token无效'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



$uid=intval($data['uid']);



$tid=intval($_GET['tid'] ?? 0);



if(!$tid){

    echo json_encode([
        'code'=>400,
        'message'=>'tid不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



$row=DB::fetch_first(

"SELECT favid

 FROM pre_home_favorite

 WHERE uid=%d

 AND id=%d

 AND idtype='tid'",

array(
$uid,
$tid
)

);



echo json_encode([

'code'=>0,

'data'=>array(

    'favorite'=>$row ? true:false

)

],JSON_UNESCAPED_UNICODE);
