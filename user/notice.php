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

    // 统一解析通知关联的主题 ID，供 APP 点击后打开主题详情
    $tid=0;

    if($row['from_idtype']=='tid'){

        $tid=intval($row['from_id']);

    }elseif($row['from_idtype']=='pid'){

        $tid=intval(DB::result_first(
            "SELECT tid FROM pre_forum_post WHERE pid=%d",
            array(intval($row['from_id']))
        ));

    }


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

        'tid'=>$tid,

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

