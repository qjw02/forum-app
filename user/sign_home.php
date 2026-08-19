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


// 加载签到插件
require_once '/www/wwwroot/qq/wwwroot/source/plugin/zqlj_sign/functions.php';


// 初始化签到插件
zqlj_sign_init();


// 当前日期
$today=dgmdate(TIMESTAMP,'Ymd');

$year=intval(dgmdate(TIMESTAMP,'Y'));

$month=intval(dgmdate(TIMESTAMP,'m'));


// 今日签到记录
$todaylog=DB::fetch_first(
    "SELECT
        logid,
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
     LIMIT 1",
    array(
        $uid,
        $today
    )
);


// 签到统计
$stats=DB::fetch_first(
    "SELECT
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


// 如果没有统计记录，给默认值
if(!$stats){

    $stats=array(
        'alldays'=>0,
        'monthdays'=>0,
        'allreward'=>0,
        'lastreward'=>0,
        'series'=>0,
        'lasttime'=>0
    );

}


// 今日奖励
$todayreward=0;

if($todaylog){

    $todayreward=
        intval($todaylog['money1'])+
        intval($todaylog['money2'])+
        intval($todaylog['money3'])+
        intval($todaylog['money4'])+
        intval($todaylog['money5']);
}


// 本月签到日历
$calendar=DB::fetch_all(
    "SELECT
        day,
        money1,
        money2,
        money3,
        money4,
        money5,
        dateline
     FROM ".DB::table('zqlj_sign_logs')."
     WHERE uid=%d
     AND day BETWEEN %d AND %d
     ORDER BY day ASC",
    array(
        $uid,
        intval(sprintf('%04d%02d01',$year,$month)),
        intval(sprintf('%04d%02d31',$year,$month))
    )
);


// 整理日历
$signed_days=array();

foreach($calendar as $row){

    $date=strval($row['day']);

    $signed_days[]=array(
        'day'=>intval(substr($date,6,2)),
        'date'=>$date,
        'reward'=>
            intval($row['money1'])+
            intval($row['money2'])+
            intval($row['money3'])+
            intval($row['money4'])+
            intval($row['money5']),
        'dateline'=>$row['dateline']
    );

}


// 签到规则
$vars=$_sign_init['vars'] ?? array();

// 解析随机奖励配置，例如 5-10
function parse_reward_range($value){

    $value=trim((string)$value);

    if($value===''){
        return array(
            'min'=>0,
            'max'=>0
        );
    }

    $parts=explode('-', $value);

    $min=intval(trim($parts[0] ?? 0));
    $max=intval(trim($parts[1] ?? 0));

    if($max<=0){
        $max=$min;
    }

    if($min>$max){
        $tmp=$min;
        $min=$max;
        $max=$tmp;
    }

    return array(
        'min'=>$min,
        'max'=>$max
    );
}


$rule=array(

    'creditid'=>intval($_sign_init['creditid'] ?? 0),

    'creditname'=>$_sign_init['creditname'] ?? '',

    'base_reward'=>parse_reward_range($vars['money1'] ?? ''),

    'daily_rank_reward'=>$_sign_init['money2'] ?? array(),

    'series_reward'=>$_sign_init['money3'] ?? array(),

    'group_reward'=>array(
        'groups'=>$_sign_init['groups4'] ?? array(),
        'reward'=>parse_reward_range($vars['money4'] ?? '')
    ),

    'special_day_reward'=>parse_reward_range($vars['money5'] ?? ''),

    'grade_list'=>$_sign_init['gradelist'] ?? array(),

    'notice'=>$_sign_init['notice'] ?? '',

    'rule'=>$_sign_init['rule'] ?? ''

);


// 返回
echo json_encode([

    'code'=>0,

    'data'=>array(

        'uid'=>$uid,

        'today'=>array(

            'date'=>$today,

            'signed'=>!!$todaylog,

            'reward'=>$todayreward,

            'dateline'=>$todaylog ? $todaylog['dateline'] : 0

        ),

        'stats'=>array(

            'alldays'=>intval($stats['alldays']),

            'monthdays'=>intval($stats['monthdays']),

            'allreward'=>intval($stats['allreward']),

            'lastreward'=>intval($stats['lastreward']),

            'series'=>intval($stats['series']),

            'lasttime'=>intval($stats['lasttime'])

        ),

        'rule'=>$rule,

        'calendar'=>array(

            'year'=>$year,

            'month'=>$month,

            'signed_days'=>$signed_days

        )

    )

],JSON_UNESCAPED_UNICODE);

