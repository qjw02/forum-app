<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';


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
        'message'=>'token无效或过期'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}



$uid=intval($data['uid']);



/*
 * 查询别人回复我的主题
 *
 * 逻辑：
 * 我的主题
 * +
 * 别人的回复
 */

$replies=DB::fetch_all(

"SELECT

 p.pid,
 p.tid,
 p.author,
 p.authorid,
 p.message,
 p.dateline,

 t.subject

FROM pre_forum_post p

LEFT JOIN pre_forum_thread t

ON p.tid=t.tid


WHERE 

p.first=0

AND t.authorid=%d

AND p.authorid<>%d


ORDER BY p.dateline DESC

LIMIT 0,50",


array(

$uid,

$uid

)

);



$list=array();



foreach($replies as $row){


    $list[]=array(

        'pid'=>$row['pid'],

        'tid'=>$row['tid'],

        'subject'=>$row['subject'],

        'author'=>array(

            'uid'=>$row['authorid'],

            'username'=>$row['author'],

            'avatar'=>
            'https://a3x9r3.cdlf3.com/uc_server/avatar.php?uid='
            .$row['authorid']
            .'&size=middle'

        ),


        'message'=>$row['message'],

        'dateline'=>$row['dateline']


    );


}



echo json_encode([


'code'=>0,


'data'=>array(

    'count'=>count($list),

    'list'=>$list

)


],JSON_UNESCAPED_UNICODE);

