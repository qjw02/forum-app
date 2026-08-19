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
 FROM pre_forum_thread"

);



/*
 * 帖子列表
 */

$threads=DB::fetch_all(

"SELECT

 t.tid,
 t.fid,
 t.author,
 t.authorid,
 t.subject,
 t.views,
 t.replies,
 t.dateline,

 m.avatarstatus

 FROM pre_forum_thread t

 LEFT JOIN pre_common_member m

 ON t.authorid=m.uid

 ORDER BY t.dateline DESC

 LIMIT %d,%d",

array(
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


        'author'=>array(

            'uid'=>$row['authorid'],

            'username'=>$row['author'],

            'avatar'=>
            'https://a3x9r3.cdlf3.com/uc_server/avatar.php?uid='
            .$row['authorid']
            .'&size=middle'

        ),


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

