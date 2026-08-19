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


// 加载签到插件
require_once '/www/wwwroot/qq/wwwroot/source/plugin/zqlj_sign/functions.php';


// 初始化
zqlj_sign_init();


// 原始签到配置
$vars=$_sign_init['vars'] ?? [];


// 基础奖励
$base_reward=trim($vars['money1'] ?? '');

if(strpos($base_reward,'-') !== false){

    $parts=explode('-', $base_reward, 2);

    $base_reward=[
        'min'=>intval(trim($parts[0])),
        'max'=>intval(trim($parts[1]))
    ];

}else{

    $base_reward=intval($base_reward);

}


// 用户组奖励
$group_reward=trim($vars['money4'] ?? '');

if(strpos($group_reward,'-') !== false){

    $parts=explode('-', $group_reward, 2);

    $group_reward=[
        'min'=>intval(trim($parts[0])),
        'max'=>intval(trim($parts[1]))
    ];

}else{

    $group_reward=intval($group_reward);

}


// 特殊日期奖励
$special_day_reward=trim($vars['money5'] ?? '');

if(strpos($special_day_reward,'-') !== false){

    $parts=explode('-', $special_day_reward, 2);

    $special_day_reward=[
        'min'=>intval(trim($parts[0])),
        'max'=>intval(trim($parts[1]))
    ];

}else{

    $special_day_reward=intval($special_day_reward);

}


// 返回规则
echo json_encode([

    'code'=>0,

    'data'=>[

        'creditid'=>$_sign_init['creditid'] ?? 0,

        'creditname'=>$_sign_init['creditname'] ?? '',


        // 每日基础奖励
        'base_reward'=>$base_reward,


        // 每日排名奖励
        'daily_rank_reward'=>$_sign_init['money2'] ?? [],


        // 连续签到奖励
        'series_reward'=>$_sign_init['money3'] ?? [],


        // 用户组奖励
        'group_reward'=>[
            'groups'=>$_sign_init['groups4'] ?? [],
            'reward'=>$group_reward
        ],


        // 特殊日期奖励
        'special_day_reward'=>$special_day_reward,


        // 签到等级
        'grade_list'=>$_sign_init['gradelist'] ?? [],


        // 公告
        'notice'=>$_sign_init['notice'] ?? '',


        // 规则说明
        'rule'=>$_sign_init['rule'] ?? ''

    ]

],JSON_UNESCAPED_UNICODE);
