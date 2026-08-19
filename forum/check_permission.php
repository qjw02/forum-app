<?php


define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



$fid=intval($_GET['fid'] ?? 0);


$uid=intval($_GET['uid'] ?? 0);




if(!$fid){


    echo json_encode([

        'code'=>400,

        'message'=>'fid不能为空'

    ],JSON_UNESCAPED_UNICODE);


    exit;

}






/*
 * 用户信息
 */


if($uid){


    $member=getuserbyuid($uid,1);



    if(!$member){


        echo json_encode([

            'code'=>401,

            'message'=>'用户不存在'

        ],JSON_UNESCAPED_UNICODE);


        exit;


    }



    $groupid=$member['groupid'];



    $credits=intval($member['credits']);



}else{


    // 游客

    $groupid=7;


    $credits=0;


}






/*
 * 用户组是否允许访问
 */


$group=DB::fetch_first(

"SELECT *

FROM pre_common_usergroup

WHERE groupid=%d",

array($groupid)

);



if(!$group){


    echo json_encode([

        'code'=>403,

        'message'=>'用户组不存在'

    ],JSON_UNESCAPED_UNICODE);


    exit;


}







/*
 * 检查版块访问权限
 *
 * 如果没有特殊设置
 * 默认允许
 */


$access=DB::fetch_first(

"SELECT *

FROM pre_forum_forum_access

WHERE fid=%d

AND groupid=%d",

array(
$fid,
$groupid
)

);






if($access){



    if($access['allowview']==0){


        echo json_encode([

            'code'=>403,

            'message'=>'无权访问该板块'

        ],JSON_UNESCAPED_UNICODE);


        exit;


    }



}







/*
 * 版块积分限制
 *
 * 这里读取 Discuz 版块字段
 */


$forum=DB::fetch_first(

"SELECT *

FROM pre_forum_forum

WHERE fid=%d",

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
 * 如果以后增加字段
 * 可在这里扩展
 *
 * need_credit
 */


$need_credit=0;





if($credits < $need_credit){


    echo json_encode([

        'code'=>403,

        'message'=>'积分不足'

    ],JSON_UNESCAPED_UNICODE);


    exit;


}







echo json_encode([


'code'=>0,


'data'=>[


    'allow'=>true,


    'groupid'=>$groupid,


    'credits'=>$credits


]


],JSON_UNESCAPED_UNICODE);
