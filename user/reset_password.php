<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';

header('Content-Type: application/json; charset=utf-8');


$username = trim($_POST['username'] ?? '');

$password = $_POST['password'] ?? '';



if(!$username || !$password){


    echo json_encode([

        'code'=>400,

        'message'=>'参数不能为空'

    ],JSON_UNESCAPED_UNICODE);


    exit;

}




$user = C::t('common_member')->fetch_by_username($username);



if(!$user){


    echo json_encode([

        'code'=>404,

        'message'=>'用户不存在'

    ],JSON_UNESCAPED_UNICODE);


    exit;

}




$uid = intval($user['uid']);



// 使用 Discuz 修改密码

uc_user_edit(

    $username,

    '',

    $password,

    '',

    1

);



echo json_encode([

    'code'=>0,

    'message'=>'密码修改成功'

],JSON_UNESCAPED_UNICODE);