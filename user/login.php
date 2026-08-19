<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';

header('Content-Type: application/json; charset=utf-8');


$username=trim($_POST['username'] ?? '');

$password=$_POST['password'] ?? '';


if(!$username || !$password){

    echo json_encode([
        'code'=>400,
        'message'=>'账号密码不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;
}


$result=userlogin(
    $username,
    $password,
    0,
    '',
    'username'
);


if($result['status'] != 1){

    echo json_encode([
        'code'=>401,
        'message'=>'账号或密码错误'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


$uid=intval($result['ucresult']['uid'] ?? 0);


if(!$uid){

    echo json_encode([
        'code'=>401,
        'message'=>'用户ID获取失败'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


$member=getuserbyuid($uid,1);


DB::query(
"DELETE FROM pre_app_token WHERE uid=%d",
array($uid)
);


$token=sha1(
    $uid.
    $username.
    TIMESTAMP.
    mt_rand()
);


$expire=TIMESTAMP+86400*30;


DB::insert(
'app_token',
array(
    'uid'=>$uid,
    'token'=>$token,
    'expire'=>$expire
)
);


echo json_encode([

'code'=>0,

'message'=>'登录成功',

'data'=>array(

    'uid'=>$uid,

    'username'=>$member['username'],

    'avatar'=>
    'https://a3x9r3.cdlf3.com/uc_server/avatar.php?uid='.$uid.'&size=middle',

    'token'=>$token,

    'expire'=>$expire

)

],JSON_UNESCAPED_UNICODE);
