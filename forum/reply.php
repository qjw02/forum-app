<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_forum.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');


// APP签名验证

if(!check_api_sign()){

    echo json_encode([
   // Token

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


// 获取用户

$member=DB::fetch_first(
"SELECT uid,username
 FROM pre_common_member
 WHERE uid=%d",
array($uid)
);


if(!$member){

    echo json_encode([
        'code'=>404,
        'message'=>'用户不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


// 参数

$tid=intval($_POST['tid'] ?? 0);

$message=trim($_POST['message'] ?? '');


if(!$tid){

    echo json_encode([
        'code'=>400,
        'message'=>'tid不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


if(!$message){

    echo json_encode([
        'code'=>400,
        'message'=>'内容不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


// 检查主题

$thread=DB::fetch_first(
"SELECT tid,fid,subject
 FROM pre_forum_thread
 WHERE tid=%d",
array($tid)
);


if(!$thread){

    echo json_encode([
        'code'=>404,
        'message'=>'主题不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


// 生成pid

$pid=DB::result_first(
"SELECT MAX(pid)+1
 FROM pre_forum_post"
);


if(!$pid){
    $pid=1;
}


// 插入回复

DB::insert(
'forum_post',
array(

    'pid'=>$pid,

    'fid'=>$thread['fid'],

    'tid'=>$tid,

    'first'=>0,

    'author'=>$member['username'],

    'authorid'=>$uid,

    'subject'=>'',

    'dateline'=>TIMESTAMP,

    'message'=>$message,

    'useip'=>$_SERVER['REMOTE_ADDR'],

    'port'=>0,

    'invisible'=>0,

    'anonymous'=>0,

    'usesig'=>1,

    'htmlon'=>0,

    'bbcodeoff'=>0,

    'smileyoff'=>0,

    'parseurloff'=>0,

    'attachment'=>0,

    'status'=>0,

    'tags'=>'0',

    'comment'=>0,

    'replycredit'=>0
)

);


// 更新主题

DB::query(
"UPDATE pre_forum_thread
 SET replies=replies+1,
 lastpost=%d,
 lastposter=%s
 WHERE tid=%d",
array(
    TIMESTAMP,
    $member['username'],
    $tid
)

);


// 返回

echo json_encode([

    'code'=>0,

    'message'=>'回复成功',

    'data'=>array(

        'pid'=>$pid,

        'tid'=>$tid,

        'message'=>$message,

        'time'=>TIMESTAMP

    )

],JSON_UNESCAPED_UNICODE);

