<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

header('Content-Type: application/json; charset=utf-8');


// token

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


$from_uid=intval($data['uid']);


// 参数

$to_uid=intval($_POST['uid'] ?? 0);

$message=trim($_POST['message'] ?? '');



if(!$to_uid){

    echo json_encode([
        'code'=>400,
        'message'=>'接收用户不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}


if(!$message){

    echo json_encode([
        'code'=>400,
        'message'=>'消息不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



// 检查用户

$user=DB::fetch_first(

"SELECT uid,username
 FROM pre_common_member
 WHERE uid=%d",

array($to_uid)

);


if(!$user){

    echo json_encode([
        'code'=>404,
        'message'=>'用户不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



$min=min($from_uid,$to_uid);

$max=max($from_uid,$to_uid);

$minmax=$min.'_'.$max;



// 查找已有会话

$list=DB::fetch_first(

"SELECT plid
 FROM pre_ucenter_pm_lists
 WHERE min_max=%s
 LIMIT 1",

array($minmax)

);



if($list){

    $plid=$list['plid'];


}else{


    // 创建会话

    DB::insert(
    'ucenter_pm_lists',
    array(

        'authorid'=>$from_uid,

        'pmtype'=>1,

        'subject'=>mb_substr($message,0,80,'utf-8'),

        'members'=>2,

        'min_max'=>$minmax,

        'dateline'=>TIMESTAMP,

        'lastmessage'=>$message

    )
    );


    $plid=DB::insert_id();



    // 创建双方成员


    DB::insert(
    'ucenter_pm_members',
    array(

        'plid'=>$plid,

        'uid'=>$from_uid,

        'isnew'=>0,

        'pmnum'=>1,

        'lastupdate'=>TIMESTAMP,

        'lastdateline'=>TIMESTAMP

    )
    );


    DB::insert(
    'ucenter_pm_members',
    array(

        'plid'=>$plid,

        'uid'=>$to_uid,

        'isnew'=>1,

        'pmnum'=>1,

        'lastupdate'=>TIMESTAMP,

        'lastdateline'=>TIMESTAMP

    )
    );


}



// 根据plid选择消息分表

$table='ucenter_pm_messages_'.($plid%10);



// 写入消息

DB::insert(

$table,

array(

    'plid'=>$plid,

    'authorid'=>$from_uid,

    'message'=>$message,

    'delstatus'=>0,

    'dateline'=>TIMESTAMP

)

);



// 更新会话

DB::query(

"UPDATE pre_ucenter_pm_lists
 SET lastmessage=%s,
 dateline=%d
 WHERE plid=%d",

array(

$message,

TIMESTAMP,

$plid

)

);



// 更新接收者状态

DB::query(

"UPDATE pre_ucenter_pm_members
 SET isnew=1,
 lastupdate=%d,
 lastdateline=%d,
 pmnum=pmnum+1

 WHERE plid=%d
 AND uid=%d",

array(

TIMESTAMP,

TIMESTAMP,

$plid,

$to_uid

)

);



// Firebase 离线推送：推送失败不能影响私信发送本身。
$firebasePush=__DIR__.'/firebase_push.php';
if(is_readable($firebasePush)){
    require_once $firebasePush;
    $senderName=DB::result_first(
        "SELECT username FROM pre_common_member WHERE uid=%d",
        array($from_uid)
    );
    @fcm_send_to_user(
        $to_uid,
        ($senderName ? $senderName : '用户').' 发来一条私信',
        mb_substr(strip_tags($message), 0, 100, 'utf-8'),
        array('type'=>'private_message', 'plid'=>$plid, 'from_uid'=>$from_uid)
    );
}


echo json_encode([

'code'=>0,

'message'=>'发送成功',

'data'=>array(

    'plid'=>$plid,

    'to_uid'=>$to_uid,

    'message'=>$message,

    'time'=>TIMESTAMP

)

],JSON_UNESCAPED_UNICODE);

