<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



/*
 * Token验证
 */

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



/*
 * 检查上传
 */


if(empty($_FILES['file'])){

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



/*
 * 图片类型
 */


$allow=array(
    'image/jpeg',
    'image/png',
    'image/gif',
    'image/webp'
);



if(!in_array($file['type'],$allow)){

    echo json_encode([
        'code'=>400,
        'message'=>'只允许图片'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



/*
 * 保存目录
 */


$ym=date('Ym');

$dir="/www/wwwroot/qq/wwwroot/data/attachment/forum/".$ym;



if(!is_dir($dir)){

    mkdir($dir,0755,true);

}



$ext=strtolower(pathinfo($file['name'],PATHINFO_EXTENSION));


$name=date('YmdHis').'_'.mt_rand(1000,9999).'.'.$ext;



$path=$dir.'/'.$name;



if(!move_uploaded_file($file['tmp_name'],$path)){


    echo json_encode([
        'code'=>500,
        'message'=>'保存失败'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



/*
 * URL
 */


$url='https://a3x9r3.cdlf3.com/data/attachment/forum/'.$ym.'/'.$name;



echo json_encode([

    'code'=>0,

    'message'=>'上传成功',

    'data'=>array(

        'uid'=>$uid,

        'url'=>$url,

        'img'=>'[img]'.$url.'[/img]'

    )


],JSON_UNESCAPED_UNICODE);
