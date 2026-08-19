<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';

header('Content-Type: application/json; charset=utf-8');


$username = trim($_POST['username'] ?? '');

$password = $_POST['password'] ?? '';

$email = trim($_POST['email'] ?? '');



if(!$username || !$password){

    echo json_encode([

        'code'=>400,

        'message'=>'账号密码不能为空'

    ],JSON_UNESCAPED_UNICODE);

    exit;

}



// Discuz 注册

$uid = uc_user_register(

    $username,

    $password,

    $email

);



if($uid <= 0){


    echo json_encode([

        'code'=>401,

        'message'=>'注册失败，用户名可能已存在'

    ],JSON_UNESCAPED_UNICODE);


    exit;

}



echo json_encode([

    'code'=>0,

    'message'=>'注册成功',

    'data'=>[

        'uid'=>$uid

    ]

],JSON_UNESCAPED_UNICODE);