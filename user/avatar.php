<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



$token=$_SERVER['HTTP_X_TOKEN'] ?? '';



if(!$token){

    echo json_encode([

        'code'=>401,

        'message'=>'token缺失'

    ],JSON_UNESCAPED_UNICODE);

    exit;

}




$data=C::t('app_token')->get_by_token($token);



if(!$data){

    echo json_encode([

        'code'=>401,

        'message'=>'token无效'

    ],JSON_UNESCAPED_UNICODE);

    exit;

}



$uid=intval($data['uid']);





if(!isset($_FILES['file'])){


    echo json_encode([

        'code'=>400,

        'message'=>'没有文件'

    ],JSON_UNESCAPED_UNICODE);


    exit;

}




$file=$_FILES['file'];





// 临时文件

$tmp=$file['tmp_name'];





// 读取图片信息

$imginfo=getimagesize($tmp);



if(!$imginfo){


    echo json_encode([

        'code'=>400,

        'message'=>'不是图片'

    ],JSON_UNESCAPED_UNICODE);


    exit;


}




// UCenter头像目录

$uc_avatar=

'/www/wwwroot/qq/wwwroot/uc_server/data/avatar/';





$uidstr=sprintf("%09d",$uid);


$dir1=substr($uidstr,0,3);

$dir2=substr($uidstr,3,2);

$dir3=substr($uidstr,5,2);



$path=$uc_avatar.
$dir1.'/'.
$dir2.'/'.
$dir3.'/';




if(!is_dir($path)){

    mkdir($path,0777,true);

}





$avatar_name = substr($uidstr,7,2);


// 三种尺寸（UCenter标准格式）

$middle=$path.$avatar_name.'_avatar_middle.jpg';

$small=$path.$avatar_name.'_avatar_small.jpg';

$big=$path.$avatar_name.'_avatar_big.jpg';



move_uploaded_file(

    $tmp,

    $middle

);





// 简单复制

copy($middle,$small);

copy($middle,$big);






// 更新用户头像状态

C::t('common_member')->update(

    $uid,

    array(

        'avatarstatus'=>1

    )

);





echo json_encode([


'code'=>0,


'message'=>'头像上传成功',


'data'=>[

'avatar'=>

$_G['siteurl'].

'uc_server/avatar.php?uid='.

$uid.

'&size=middle&t='.

time()

]


],JSON_UNESCAPED_UNICODE);