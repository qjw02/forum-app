<?php
error_reporting(0);

if(!defined('IN_API')){
    define('IN_API',true);
}


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();


require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';


header('Content-Type: application/json; charset=utf-8');



$tid = intval($_POST['tid'] ?? 0);

$optionid = intval($_POST['optionid'] ?? 0);



if(!$tid || !$optionid){

    echo json_encode([

        'code'=>400,

        'message'=>'参数错误'

    ],JSON_UNESCAPED_UNICODE);

    exit;

}




// APP token 登录验证

$headers = getallheaders();

$token = '';

if(isset($headers['Authorization'])){

    $token = str_replace(
        'Bearer ',
        '',
        $headers['Authorization']
    );

}


if(!$token && isset($_POST['token'])){

    $token = $_POST['token'];

}



if(!$token){

    echo json_encode([

        'code'=>401,

        'message'=>'缺少token'

    ],JSON_UNESCAPED_UNICODE);

    exit;

}





$appUser = DB::fetch_first(

    "SELECT uid,expire
     FROM pre_app_token
     WHERE token=%s",

    array($token)

);



if(!$appUser){

    echo json_encode([

        'code'=>401,

        'message'=>'token无效'

    ],JSON_UNESCAPED_UNICODE);

    exit;

}




if($appUser['expire'] < TIMESTAMP){

    echo json_encode([

        'code'=>401,

        'message'=>'token过期'

    ],JSON_UNESCAPED_UNICODE);

    exit;

}



$uid = intval($appUser['uid']);




// 查询帖子

$thread = DB::fetch_first(

    "SELECT fid FROM pre_forum_thread WHERE tid=%d",

    array($tid)

);



if(!$thread){

    echo json_encode([

        'code'=>404,

        'message'=>'帖子不存在'

    ],JSON_UNESCAPED_UNICODE);

    exit;

}





// 查询价格

$price = DB::result_first(

    "SELECT price
     FROM pre_app_thread_field_price
     WHERE tid=%d
     AND optionid=%d",

    array(

        $tid,

        $optionid

    )

);


if(!$price){

    echo json_encode([

        'code'=>400,

        'message'=>'该内容不可购买'

    ],JSON_UNESCAPED_UNICODE);

    exit;

}





// 查询是否购买

$buy = DB::fetch_first(

    "SELECT id
     FROM pre_app_field_buy
     WHERE tid=%d
     AND uid=%d
     AND optionid=%d",

    array(

        $tid,

        $uid,

        $optionid

    )

);



if($buy){


    echo getContact($tid);


    exit;

}





// 查询C币

$coin = DB::result_first(

    "SELECT extcredits4
     FROM pre_common_member_count
     WHERE uid=%d",

    array($uid)

);



if($coin < $price){

    echo json_encode([

        'code'=>402,

        'message'=>'C币不足'

    ],JSON_UNESCAPED_UNICODE);

    exit;

}





// 扣C币

DB::query(

    "UPDATE pre_common_member_count
     SET extcredits4=extcredits4-%d
     WHERE uid=%d",

    array(

        $price,

        $uid

    )

);






// 保存购买记录

DB::insert(

    'app_field_buy',

    array(

        'tid'=>$tid,

        'uid'=>$uid,

        'optionid'=>$optionid,

        'price'=>$price,

        'dateline'=>TIMESTAMP

    )

);






echo getContact($tid);








function getContact($tid){


    $row = DB::fetch_first(

        "SELECT value
         FROM pre_forum_typeoptionvar
         WHERE tid=%d
         AND optionid=7",

        array($tid)

    );



    return json_encode([

        'code'=>0,

        'data'=>[

            'contact'=>$row['value'] ?? ''

        ]

    ],JSON_UNESCAPED_UNICODE);



}