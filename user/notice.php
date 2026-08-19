<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



/*
 * Token
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
 * 通知列表
 */

$notice=DB::fetch_all(

"SELECT
 id,
 type,
 new,
 authorid,
 author,
 note,
 dateline,
 from_id,
 from_idtype,
 from_num,
 category

 FROM pre_home_notification

 WHERE uid=%d

 ORDER BY dateline DESC

 LIMIT 0,50",

array($uid)

);



$list=array();


foreach($notice as $row){


    $list[]=array(

        'id'=>$row['id'],

        'type'=>$row['type'],

        'new'=>$row['new'],

        'author'=>array(

            'uid'=>$row['authorid'],

            'username'=>$row['author']

        ),

        'note'=>$row['note'],

        'dateline'=>$row['dateline'],

        'from_id'=>$row['from_id'],

        'from_idtype'=>$row['from_idtype'],

        'category'=>$row['category']

    );


}



echo json_encode([

'code'=>0,

'data'=>array(

    'count'=>count($list),

    'list'=>$list

)

],JSON_UNESCAPED_UNICODE);

