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
 * 获取收藏帖子
 */


$favorites=DB::fetch_all(

"SELECT

 f.id AS tid,
 f.title,
 f.description,
 f.dateline,

 t.subject,
 t.author,
 t.authorid,
 t.views,
 t.replies,
 t.dateline AS thread_time


FROM pre_home_favorite f


LEFT JOIN pre_forum_thread t

ON f.id=t.tid


WHERE f.uid=%d

AND f.idtype='tid'


ORDER BY f.dateline DESC


LIMIT 0,50",

array($uid)

);





$list=array();




foreach($favorites as $row){


    /*
     * 删除的帖子跳过
     */

    if(!$row['tid'] || !$row['subject']){

        continue;

    }



    $list[]=array(

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



        'views'=>$row['views'],


        'replies'=>$row['replies'],


        'dateline'=>$row['thread_time']


    );


}





echo json_encode([


    'code'=>0,


    'data'=>array(

        'count'=>count($list),

        'list'=>$list

    )


],JSON_UNESCAPED_UNICODE);

