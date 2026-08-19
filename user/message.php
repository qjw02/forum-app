<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



/*
 * Token验证
 */

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



/*
 * 未读数量
 */

$unread=DB::result_first(

"SELECT COUNT(*)
 FROM pre_ucenter_pm_members
 WHERE uid=%d
 AND isnew>0",

array($uid)

);



/*
 * 私信列表
 */

$rows=DB::fetch_all(

"SELECT

 p.plid,
 p.subject,
 p.lastmessage,
 p.dateline,

 m.isnew,
 m.pmnum

 FROM pre_ucenter_pm_lists p

 LEFT JOIN pre_ucenter_pm_members m

 ON p.plid=m.plid

 WHERE m.uid=%d

 ORDER BY p.dateline DESC

 LIMIT 50",

array($uid)

);



$list=array();



foreach($rows as $row){


    $message='';


    /*
     * Discuz序列化消息解析
     */

    $tmp=@unserialize($row['lastmessage']);


    if(is_array($tmp) && isset($tmp['lastsummary'])){

        $message=strip_tags($tmp['lastsummary']);

    }else{

        $message=$row['lastmessage'];

    }



    $list[]=array(

        'plid'=>$row['plid'],

        'subject'=>$row['subject'],

        'message'=>$message,

        'unread'=>$row['isnew'],

        'count'=>$row['pmnum'],

        'dateline'=>$row['dateline']

    );


}



echo json_encode([

    'code'=>0,

    'data'=>array(

        'unread'=>intval($unread),

        'count'=>count($list),

        'list'=>$list

    )

],JSON_UNESCAPED_UNICODE);

