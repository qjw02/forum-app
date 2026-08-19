<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';


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



// 收藏列表

$list=DB::fetch_all(

"SELECT
f.id,
f.dateline,
t.subject,
t.tid,
t.author,
t.authorid,
t.views,
t.replies,
t.lastpost

FROM pre_home_favorite f

LEFT JOIN pre_forum_thread t
ON f.id=t.tid

WHERE f.uid=%d
AND f.idtype='tid'

ORDER BY f.dateline DESC

LIMIT %d,%d",

array(
$uid,
$start,
$limit
)

);





$result=array();


foreach($list as $row){


    $result[]=array(

        'tid'=>$row['tid'],

        'subject'=>$row['subject'],

        'author'=>array(

            'uid'=>$row['authorid'],

            'username'=>$row['author']

        ),

        'views'=>$row['views'],

        'replies'=>$row['replies'],

        'favorite_time'=>$row['dateline']

    );


}



echo json_encode([

    'code'=>0,

    'data'=>array(

        'page'=>$page,

        'list'=>$result

    )

],JSON_UNESCAPED_UNICODE);
