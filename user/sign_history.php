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


// 分页

$page=max(1,intval($_GET['page'] ?? 1));

$limit=20;

$start=($page-1)*$limit;


// 签到历史

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

    ORDER BY day DESC

    LIMIT %d,%d",

    array(
        $uid,
        $start,
        $limit
    )

);


// 整理数据

$result=array();

foreach($list as $row){

    $reward=
        intval($row['money1'])+
        intval($row['money2'])+
        intval($row['money3'])+
        intval($row['money4'])+
        intval($row['money5']);

    $result[]=array(

        'logid'=>$row['logid'],

        'date'=>$row['day'],

        'reward'=>$reward,

        'money1'=>intval($row['money1']),
        'money2'=>intval($row['money2']),
        'money3'=>intval($row['money3']),
        'money4'=>intval($row['money4']),
        'money5'=>intval($row['money5']),

        'dateline'=>$row['dateline']

    );

}


// 返回

echo json_encode([

    'code'=>0,

    'data'=>array(

        'page'=>$page,

        'limit'=>$limit,

        'list'=>$result

    )

],JSON_UNESCAPED_UNICODE);

