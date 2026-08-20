<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

header('Content-Type: application/json; charset=utf-8');

$token=$_SERVER['HTTP_X_TOKEN'] ?? '';

$data=C::t('app_token')->get_by_token($token);

if(!$data || $data['expire'] < TIMESTAMP){

    echo json_encode(array(
        'code'=>401,
        'message'=>'token无效'
    ),JSON_UNESCAPED_UNICODE);

    exit;

}

$uid=intval($data['uid']);
$action=$_REQUEST['action'] ?? 'list';

function friend_response($code,$message,$data=null){

    echo json_encode(array(
        'code'=>$code,
        'message'=>$message,
        'data'=>$data
    ),JSON_UNESCAPED_UNICODE);

    exit;

}

if($action=='list'){

    $friends=DB::fetch_all(

        "SELECT f.fuid,m.username
         FROM pre_home_friend f
         LEFT JOIN pre_common_member m ON m.uid=f.fuid
         WHERE f.uid=%d
         ORDER BY f.dateline DESC",

        array($uid)

    );

    $requests=DB::fetch_all(

        "SELECT r.fuid,m.username
         FROM pre_home_friend_request r
         LEFT JOIN pre_common_member m ON m.uid=r.fuid
         WHERE r.uid=%d
         ORDER BY r.dateline DESC",

        array($uid)

    );

    friend_response(0,'ok',array(
        'friends'=>$friends,
        'requests'=>$requests
    ));

}

$target_uid=intval($_REQUEST['uid'] ?? 0);

if(!$target_uid){

    friend_response(400,'用户UID不能为空');

}

if($target_uid==$uid){

    friend_response(400,'不能添加自己');

}

if($action=='add'){

    $target=DB::fetch_first(
        "SELECT uid,username FROM pre_common_member WHERE uid=%d",
        array($target_uid)
    );

    if(!$target){

        friend_response(404,'用户不存在');

    }

    $is_friend=DB::result_first(
        "SELECT fuid FROM pre_home_friend WHERE uid=%d AND fuid=%d",
        array($uid,$target_uid)
    );

    if($is_friend){

        friend_response(400,'对方已经是你的好友');

    }

    DB::query(

        "INSERT IGNORE INTO pre_home_friend_request(uid,fuid,dateline,note)
         VALUES(%d,%d,%d,%s)",

        array($target_uid,$uid,TIMESTAMP,'')

    );

    $me=DB::fetch_first(
        "SELECT username FROM pre_common_member WHERE uid=%d",
        array($uid)
    );

    DB::insert(
        'home_notification',
        array(
            'uid'=>$target_uid,
            'type'=>'friend',
            'new'=>1,
            'authorid'=>$uid,
            'author'=>$me['username'] ?? '',
            'note'=>'申请添加您为好友',
            'dateline'=>TIMESTAMP,
            'from_id'=>$uid,
            'from_idtype'=>'uid',
            'category'=>'friend'
        )
    );

    friend_response(0,'好友申请已发送');

}

if($action=='accept'){

    $request=DB::fetch_first(
        "SELECT fuid FROM pre_home_friend_request WHERE uid=%d AND fuid=%d",
        array($uid,$target_uid)
    );

    if(!$request){

        friend_response(404,'好友申请不存在或已处理');

    }

    DB::query(
        "DELETE FROM pre_home_friend_request WHERE uid=%d AND fuid=%d",
        array($uid,$target_uid)
    );

    DB::query(
        "INSERT IGNORE INTO pre_home_friend(uid,fuid,dateline,note)
         VALUES(%d,%d,%d,%s)",
        array($uid,$target_uid,TIMESTAMP,'')
    );

    DB::query(
        "INSERT IGNORE INTO pre_home_friend(uid,fuid,dateline,note)
         VALUES(%d,%d,%d,%s)",
        array($target_uid,$uid,TIMESTAMP,'')
    );

    friend_response(0,'已添加为好友');

}

if($action=='delete'){

    DB::query(
        "DELETE FROM pre_home_friend
         WHERE (uid=%d AND fuid=%d)
         OR (uid=%d AND fuid=%d)",
        array($uid,$target_uid,$target_uid,$uid)
    );

    friend_response(0,'已删除好友');

}

friend_response(400,'未知操作');
