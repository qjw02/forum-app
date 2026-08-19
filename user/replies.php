<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



/*
 * 图片地址统一
 */

function fix_image_url($message){

    if(!$message){

        return '';

    }


    $message=preg_replace(
        '/\[img\](\/data\/attachment\/forum\/.*?)\[\/img\]/i',
        '[img]https://a3x9r3.cdlf3.com$1[/img]',
        $message
    );


    return $message;

}



/*
 * 图片提取
 */

function parse_images($message){

    $images=array();


    preg_match_all(
        '/\[img\](.*?)\[\/img\]/i',
        $message,
        $match
    );


    if(!empty($match[1])){


        foreach($match[1] as $img){


            if(strpos($img,'http')!==0){

                $img='https://a3x9r3.cdlf3.com'.$img;

            }


            $images[]=$img;

        }

    }


    return $images;

}



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

 FROM pre_forum_post

 WHERE authorid=%d

 AND first=0",

array($uid)

);



/*
 * 我的回复
 */

$posts=DB::fetch_all(

"SELECT

 p.pid,
 p.tid,
 p.message,
 p.dateline,

 t.subject

 FROM pre_forum_post p

 LEFT JOIN pre_forum_thread t

 ON p.tid=t.tid


 WHERE p.authorid=%d

 AND p.first=0


 ORDER BY p.dateline DESC


 LIMIT %d,%d",

array(

$uid,

$start,

$page_size

)

);



$list=array();



foreach($posts as $row){


    $message=fix_image_url($row['message']);


    $list[]=array(

        'pid'=>$row['pid'],

        'tid'=>$row['tid'],

        'subject'=>$row['subject'],


        'message'=>$message,


        'images'=>parse_images($message),


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
