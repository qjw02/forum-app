<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

header('Content-Type: application/json; charset=utf-8');

$token=$_SERVER['HTTP_X_TOKEN'] ?? '';

if(!$token){

    echo json_encode(array(
        'code'=>401,
        'message'=>'token缺失'
    ),JSON_UNESCAPED_UNICODE);

    exit;

}

$data=C::t('app_token')->get_by_token($token);

if(!$data || $data['expire'] < TIMESTAMP){

    echo json_encode(array(
        'code'=>401,
        'message'=>'token无效'
    ),JSON_UNESCAPED_UNICODE);

    exit;

}

$uid=intval($data['uid']);
$read_id=intval($_GET['read_id'] ?? 0);

$notice=DB::fetch_all(

    "SELECT id,type,new,authorid,author,note,dateline,from_id,from_idtype,category
     FROM pre_home_notification
     WHERE uid=%d
     ORDER BY dateline DESC
     LIMIT 0,50",

    array($uid)

);

$list=array();
$unread=0;

foreach($notice as $row){

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
            array($pid)
        ));

    }

    // 兼容旧版回复通知：from_id 可能是 tid，也可能是 pid。
    if(!$tid && $row['type']=='post' && intval($row['from_id'])){

        $candidate=intval($row['from_id']);

        if(DB::result_first(
            "SELECT tid FROM pre_forum_thread WHERE tid=%d",
            array($candidate)
        )){

            $tid=$candidate;

        }else{

            $pid=$candidate;
            $tid=intval(DB::result_first(
                "SELECT tid FROM pre_forum_post WHERE pid=%d",
                array($pid)
            ));

        }

    }

    // 最后兜底：按回复人和通知时间反查。
    if(!$tid && $row['type']=='post' && $row['authorid']){

        $reply=DB::fetch_first(
            "SELECT pid,tid
             FROM pre_forum_post
             WHERE authorid=%d
             AND first=0
             AND dateline<=%d
             ORDER BY dateline DESC
             LIMIT 1",
            array(intval($row['authorid']),intval($row['dateline']))
        );

        if($reply){
            $pid=intval($reply['pid']);
            $tid=intval($reply['tid']);
        }

    }

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
            array($tid,intval($row['authorid']),intval($row['dateline']))
        ));

    }

    $list[]=array(
        'id'=>$row['id'],
        'new'=>$row['new'],
        'type'=>$row['type'],
        'author'=>array(
            'uid'=>$row['authorid'],
            'username'=>$row['author']
        ),
        'note'=>$row['note'],
        'from_id'=>$row['from_id'],
        'from_idtype'=>$row['from_idtype'],
        'tid'=>$tid,
        'pid'=>$pid,
        'category'=>$row['category']
    );

}

if($read_id){

    DB::query(
        "UPDATE pre_home_notification
         SET new=0
         WHERE id=%d
         AND uid=%d",
        array($read_id,$uid)
    );

}

echo json_encode(array(
    'code'=>0,
    'data'=>array(
        'count'=>count($list),
        'unread'=>$unread,
        'list'=>$list
    )
),JSON_UNESCAPED_UNICODE);
