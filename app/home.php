<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

header('Content-Type: application/json; charset=utf-8');


// token 可选
$token=$_SERVER['HTTP_X_TOKEN'] ?? '';

$uid=0;


if($token){

    $data=C::t('app_token')->get_by_token($token);

    if($data && $data['expire'] >= TIMESTAMP){

        $uid=intval($data['uid']);

    }

}



// 用户信息

$user=array();


if($uid){


    $member=DB::fetch_first(

    "SELECT uid,username,credits
     FROM pre_common_member
     WHERE uid=%d",

    array($uid)

    );


    if($member){

        $user=array(

            'uid'=>$member['uid'],

            'username'=>$member['username'],

            'credits'=>$member['credits'],

            'avatar'=>
            'https://a3x9r3.cdlf3.com/uc_server/avatar.php?uid='
            .$member['uid']
            .'&size=middle'

        );

    }

}



// 未读通知

$notice_unread=0;


if($uid){

    $notice_unread=DB::result_first(

    "SELECT COUNT(*)
     FROM pre_home_notification
     WHERE uid=%d
     AND new=1",

    array($uid)

    );

}



// 未读私信

$message_unread=0;


if($uid){

    $message_unread=DB::result_first(

    "SELECT SUM(isnew)
     FROM pre_ucenter_pm_members
     WHERE uid=%d",

    array($uid)

    );

}



// 分类

$category=DB::fetch_all(

"SELECT fid,name,threads
 FROM pre_forum_forum
 WHERE status=1
 ORDER BY displayorder ASC"

);



$cats=array();


foreach($category as $c){

    $cats[]=array(

        'fid'=>$c['fid'],

        'name'=>$c['name'],

        'threads'=>$c['threads']

    );

}



// 最新帖子

$threads=DB::fetch_all(

"SELECT
 tid,
 fid,
 subject,
 author,
 authorid,
 views,
 replies,
 dateline

 FROM pre_forum_thread

 ORDER BY dateline DESC

 LIMIT 0,10"

);



$list=array();


foreach($threads as $t){


    $list[]=array(

        'tid'=>$t['tid'],

        'fid'=>$t['fid'],

        'subject'=>$t['subject'],

        'author'=>array(

            'uid'=>$t['authorid'],

            'username'=>$t['author'],

            'avatar'=>
            'https://a3x9r3.cdlf3.com/uc_server/avatar.php?uid='
            .$t['authorid']
            .'&size=middle'

        ),

        'views'=>$t['views'],

        'replies'=>$t['replies'],

        'dateline'=>$t['dateline']

    );


}



echo json_encode([


'code'=>0,


'data'=>array(


    'user'=>$user,


    'notice'=>array(

        'unread'=>intval($notice_unread)

    ),


    'message'=>array(

        'unread'=>intval($message_unread)

    ),


    'category'=>$cats,


    'threads'=>$list


)


],JSON_UNESCAPED_UNICODE);

