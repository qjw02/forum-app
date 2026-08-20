<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



// 签名验证
file_put_contents(
    '/tmp/sign_debug.txt',
    prin// token

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



// 用户资料

$member=DB::fetch_first(

"SELECT

m.uid,
m.username,
m.groupid,
m.credits,
c.extcredits4 AS money,
c.extcredits2,
c.posts,
c.threads

FROM pre_common_member m

LEFT JOIN pre_common_member_count c

ON m.uid=c.uid

WHERE m.uid=%d",

array($uid)

);



if(!$member){

    echo json_encode([
        'code'=>404,
        'message'=>'用户不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



// 用户统计

$count=array(

'threads'=>$member['threads'] ?? 0,

'posts'=>$member['posts'] ?? 0

);



// 头像

$avatar=$_G['siteurl']."uc_server/avatar.php?uid=".$uid."&size=middle";



echo json_encode([

'code'=>0,

'data'=>array(

    'uid'=>$member['uid'],

    'username'=>$member['username'],

    'avatar'=>$avatar,

    'groupid'=>$member['groupid'],

    'credits'=>$member['credits'],

    'money'=>$member['money'],

    'threads'=>$count['threads'] ?? 0,

    'posts'=>$count['posts'] ?? 0

)

],JSON_UNESCAPED_UNICODE);

