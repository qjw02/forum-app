<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

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


// token

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


// 加载签到插件函数

require_once '/www/wwwroot/qq/wwwroot/source/plugin/zqlj_sign/functions.php';


// 初始化签到插件

zqlj_sign_init();


// 今天日期

$today=dgmdate(TIMESTAMP,'Ymd');


// 用户签到统计

$sign_user=DB::fetch_first(

    "SELECT
    uid,
    username,
    alldays,
    monthdays,
    allreward,
    lastreward,
    series,
    lasttime

    FROM ".DB::table('zqlj_sign_user')."

    WHERE uid=%d",

    array($uid)

);


// 今天签到记录

$today_log=DB::fetch_first(

    "SELECT
    logid,
    uid,
    day,
    money1,
    money2,
    money3,
    money4,
    money5,
    dateline

    FROM ".DB::table('zqlj_sign_logs')."

    WHERE uid=%d
    AND day=%d

    ORDER BY logid DESC
    LIMIT 1",

    array($uid,$today)

);


// 今天是否已经签到

$signed_today=$today_log ? true:false;


// 今天奖励

$today_reward=0;

if($today_log){

    $today_reward=
        intval($today_log['money1'])+
        intval($today_log['money2'])+
        intval($today_log['money3'])+
        intval($today_log['money4'])+
        intval($today_log['money5']);

}


// 返回

echo json_encode([

    'code'=>0,

    'data'=>array(

        'uid'=>$uid,

        'signed_today'=>$signed_today,

        'today'=>array(

            'date'=>$today,

            'reward'=>$today_reward,

            'dateline'=>$today_log['dateline'] ?? 0

        ),

        'stats'=>array(

            'alldays'=>intval($sign_user['alldays'] ?? 0),

            'monthdays'=>intval($sign_user['monthdays'] ?? 0),

            'allreward'=>intval($sign_user['allreward'] ?? 0),

            'lastreward'=>intval($sign_user['lastreward'] ?? 0),

            'series'=>intval($sign_user['series'] ?? 0),

            'lasttime'=>intval($sign_user['lasttime'] ?? 0)

        )

    )

],JSON_UNESCAPED_UNICODE);

