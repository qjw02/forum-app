<?php


define('IN_API', true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');




// 获取所有开放板块

$forums = DB::fetch_all(

    "SELECT fid,name
     FROM ".DB::table('forum_forum')."
     WHERE status=1
     ORDER BY displayorder ASC"

);



$list=array();



foreach($forums as $forum){


    $list[]=array(

        'fid'=>intval($forum['fid']),

        'name'=>$forum['name']

    );


}





echo json_encode([

    'code'=>0,

    'message'=>'ok',

    'data'=>$list

],JSON_UNESCAPED_UNICODE);