<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';


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



// Token验证

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



// 文件检查

if(!isset($_FILES['file'])){

    echo json_encode([
        'code'=>400,
        'message'=>'没有上传文件'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



$file=$_FILES['file'];



if($file['error']!=0){

    echo json_encode([
        'code'=>400,
        'message'=>'上传失败'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



// 上传目录

$dir='/www/wwwroot/qq/wwwroot/data/attachment/forum/'.date('Ym').'/';


if(!is_dir($dir)){

    mkdir($dir,0777,true);

}



$ext=strtolower(pathinfo($file['name'],PATHINFO_EXTENSION));


$filename=date('YmdHis').'_'.rand(1000,9999).'.'.$ext;


$path=$dir.$filename;



if(!move_uploaded_file($file['tmp_name'],$path)){


    echo json_encode([
        'code'=>500,
        'message'=>'保存失败'
    ],JSON_UNESCAPED_UNICODE);


    exit;

}



$url='https://a3x9r3.cdlf3.com/data/attachment/forum/'
    .date('Ym')
    .'/'
    .$filename;



echo json_encode([

    'code'=>0,

    'message'=>'上传成功',

    'data'=>array(

        'uid'=>$uid,

        'url'=>$url,

        'img'=>'[img]'.$url.'[/img]'

    )

],JSON_UNESCAPED_UNICODE);
