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
 * 分页
 */

$page=intval($_GET['page'] ?? 1);

$page_size=intval($_GET['page_size'] ?? 20);



if($page<1){

    $page=1;

}


if($page_size<1){

    $page_size=20;

}


if($page_size>50){

    $page_size=50;

}


$start=($page-1)*$page_size;



/*
 * 总数
 */

$total=DB::result_first(

"SELECT COUNT(*)

 FROM pre_forum_thread

 WHERE authorid=%d",

array($uid)

);



/*
 * 我的主题
 */

$threads=DB::fetch_all(

"SELECT

 tid,
 fid,
 subject,
 views,
 replies,
 dateline

 FROM pre_forum_thread

 WHERE authorid=%d

 ORDER BY dateline DESC

 LIMIT %d,%d",

array(
    $uid,
    $start,
    $page_size
)

);



$list=array();


foreach($threads as $row){


    $list[]=array(

        'tid'=>$row['tid'],

        'fid'=>$row['fid'],

        'subject'=>$row['subject'],

        'views'=>$row['views'],

        'replies'=>$row['replies'],

        'dateline'=>$row['dateline']

    );


}



echo json_encode([

'code'=>0,

'data'=>array(

    'page'=>$page,

    'page_size'=>$page_size,

    'total'=>intval($total),

    'list'=>$list

)

],JSON_UNESCAPED_UNICODE);

