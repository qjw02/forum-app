<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

require_once '/www/wwwroot/qq/wwwroot/api/common/sign.php';

header('Content-Type: application/json; charset=utf-8');


// 签名验证

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


// 年月

$year=intval($_GET['year'] ?? intval(dgmdate(TIMESTAMP,'Y')));

$month=intval($_GET['month'] ?? intval(dgmdate(TIMESTAMP,'n')));


// 参数检查

if($year<2000 || $year>2100 || $month<1 || $month>12){

    echo json_encode([
        'code'=>400,
        'message'=>'年月参数错误'
    ],JSON_UNESCAPED_UNICODE);

    exit;
}


// 月份范围

$start_day=sprintf('%04d%02d01',$year,$month);

if($month==12){

    $next_year=$year+1;
    $next_month=1;

}else{

    $next_year=$year;
    $next_month=$month+1;

}

$end_day=sprintf('%04d%02d01',$next_year,$next_month);


// 查询本月签到

$list=DB::fetch_all(

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
    AND day>=%d
    AND day<%d

    ORDER BY day ASC",

    array(
        $uid,
        intval($start_day),
        intval($end_day)
    )

);


// 整理签到日期

$signed_days=array();

foreach($list as $row){

    $date=strval($row['day']);

    $day=intval(substr($date,6,2));

    $reward=
        intval($row['money1'])+
        intval($row['money2'])+
        intval($row['money3'])+
        intval($row['money4'])+
        intval($row['money5']);

    $signed_days[]=array(

        'day'=>$day,

        'date'=>$date,

        'reward'=>$reward,

        'dateline'=>$row['dateline']

    );

}


// 返回

echo json_encode([

    'code'=>0,

    'data'=>array(

        'year'=>$year,

        'month'=>$month,

        'signed_days'=>$signed_days

    )

],JSON_UNESCAPED_UNICODE);

