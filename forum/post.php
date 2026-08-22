<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_forum.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



/*
 * TOKEN验证
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
 * 参数
 */

$fid=intval($_POST['fid'] ?? 0);

$subject=trim($_POST['subject'] ?? '');

$message=trim($_POST['message'] ?? '');
$contact=trim($_POST['contact'] ?? '');
$price=intval($_POST['price'] ?? 0);




if(!$fid || !$subject || !$message){

    echo json_encode([
        'code'=>400,
        'message'=>'参数不完整'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}





/*
 * 用户
 */

$member=getuserbyuid($uid,1);



if(!$member){

    echo json_encode([
        'code'=>401,
        'message'=>'用户不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}





/*
 * 检查版块
 */


$forum=DB::fetch_first(

    "SELECT fid,name
     FROM ".DB::table('forum_forum')."
     WHERE fid=%d
     AND type='forum'",

    array($fid)

);



if(!$forum){

    echo json_encode([
        'code'=>404,
        'message'=>'版块不存在'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}

/*
 * 使用 Discuz 用户组和板块权限作为唯一来源。
 * APP 端的按钮只是提示，服务器仍在这里做最终拦截，防止绕过客户端直接发帖。
 */
$group = DB::fetch_first(
    "SELECT f.allowpost
     FROM ".DB::table('common_usergroup_field')." f
     WHERE f.groupid=%d",
    array(intval($member['groupid']))
);
$access = DB::fetch_first(
    "SELECT allowpost FROM ".DB::table('forum_access')." WHERE fid=%d AND uid=%d",
    array($fid, $uid)
);

if(!$group || intval($group['allowpost']) <= 0 || ($access && intval($access['allowpost']) <= 0)){
    echo json_encode([
        'code'=>403,
        'message'=>'当前用户组无权在此板块发布主题'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

/* 只有“高级报告”板块允许出售联系方式。 */
if($contact !== '' || $price > 0){

    if($forum['name'] !== '高级报告'){
        echo json_encode([
            'code'=>403,
            'message'=>'只有高级报告板块可以出售联系方式'
        ],JSON_UNESCAPED_UNICODE);
        exit;
    }

    if($contact === '' || $price <= 0){
        echo json_encode([
            'code'=>400,
            'message'=>'请填写联系方式和有效的 C币价格'
        ],JSON_UNESCAPED_UNICODE);
        exit;
    }
}




/*
 * 内容处理
 *
 * 保留Discuz BBCode
 */

$message=dhtmlspecialchars($message);



/*
 * 恢复图片BBCode
 *
 * 防止htmlspecialchars影响
 */

$message=str_replace(

    '[img]',

    '[img]',

    $message

);



$message=str_replace(

    '[/img]',

    '[/img]',

    $message

);





/*
 * 创建主题
 */


$thread=array(

    'fid'=>$fid,

    'posttableid'=>0,

    'author'=>$member['username'],

    'authorid'=>$uid,

    'subject'=>$subject,

    'dateline'=>TIMESTAMP,

    'lastpost'=>TIMESTAMP,

    'lastposter'=>$member['username'],

    'views'=>0,

    'replies'=>0,

    'digest'=>0,

    'displayorder'=>0

);




$tid=DB::insert(

    'forum_thread',

    $thread,

    true

);



if(!$tid){

    echo json_encode([
        'code'=>500,
        'message'=>'创建主题失败'
    ],JSON_UNESCAPED_UNICODE);

    exit;

}




/*
 * 创建帖子
 */



$maxpid=DB::result_first(

    "SELECT MAX(pid)
     FROM ".DB::table('forum_post')

);



$pid=intval($maxpid)+1;



$post=array(

    'pid'=>$pid,

    'tid'=>$tid,

    'fid'=>$fid,

    'first'=>1,

    'author'=>$member['username'],

    'authorid'=>$uid,

    'subject'=>$subject,

    'dateline'=>TIMESTAMP,

    'message'=>$message,

    'useip'=>$_SERVER['REMOTE_ADDR']

);





$newpid=DB::insert(

    'forum_post',

    $post,

    true

);





if(!$newpid){


    echo json_encode([
        'code'=>500,
        'message'=>'创建帖子失败'
    ],JSON_UNESCAPED_UNICODE);


    exit;

}





/* 保存收费联系方式：复用主题详情和购买接口的现有数据表。 */
if($contact !== '' && $price > 0){

    DB::insert(
        'forum_typeoptionvar',
        array(
            'tid'=>$tid,
            'optionid'=>7,
            'value'=>$contact
        )
    );

    DB::insert(
        'app_thread_field_price',
        array(
            'tid'=>$tid,
            'optionid'=>7,
            'price'=>$price
        )
    );
}


/*
 * 更新统计
 */


DB::query(

    "UPDATE ".DB::table('forum_thread')."

     SET attachment=0

     WHERE tid=%d",

    array($tid)

);





/*
 * 返回
 */


echo json_encode([

    'code'=>0,

    'message'=>'发布成功',

    'data'=>array(

        'tid'=>$tid,

        'pid'=>$newpid,

        'fid'=>$fid

    )


],JSON_UNESCAPED_UNICODE);