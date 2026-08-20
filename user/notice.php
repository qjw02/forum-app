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

    if($mark_read){

    DB::query(

        "UPDATE pre_home_notification
         SET new=0
         WHERE uid=%d",

        array($uid)

    );

}


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

$mark_read=intval($_GET['read'] ?? 0);



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
    $pid=0;

    if(intval($row['new'])>0){
        $unread++;
    }

    if($row['from_idtype']=='tid'){

        $tid=intval($row['from_id']);

    }elseif($row['from_idtype']=='pid'){

        $pid=intval($row['from_id']);
        $tid=intval(DB::result_first(
            "SELECT tid FROM pre_forum_post WHERE pid=%d",
            array(intval($row['from_id']))
        ));

    }

    // 部分 Discuz 版本没有 from_idtype：回复通知的 from_id 可能是 tid 或 pid。
    if(!$tid && $row['type']=='post' && intval($row['from_id'])){

        $candidate=intval($row['from_id']);

        $thread_exists=DB::result_first(
            "SELECT tid FROM pre_forum_thread WHERE tid=%d",
            array($candidate)
        );

        if($thread_exists){

            $tid=$candidate;

        }else{

            $pid=$candidate;
            $tid=intval(DB::result_first(
                "SELECT tid FROM pre_forum_post WHERE pid=%d",
                array($candidate)
            ));

        }

    }

    // Discuz 的“回复通知”通常只保存 tid；根据通知人和时间找到对应回复。
    if(!$pid && $tid && $row['type']=='post' && $row['authorid']){

        $pid=intval(DB::result_first(

            "SELECT pid
             FROM pre_forum_post
             WHERE tid=%d
             AND authorid=%d
             AND first=0
             AND dateline<=%d
             ORDER BY dateline DESC
             LIMIT 1",

            array(
                $tid,
                intval($row['authorid']),
                intval($row['dateline'])
            )

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

        'pid'=>$pid,

        'category'=>$row['category']

    );


}



echo json_encode([

'code'=>0,

'data'=>array(

    'count'=>count($list),

    'unread'=>$unread,

    'list'=>$list

)

],JSON_UNESCAPED_UNICODE);

