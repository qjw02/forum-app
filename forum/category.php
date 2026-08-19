<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



/*
 * 获取公开版块
 */


$forums=DB::fetch_all(

"SELECT
 f.fid,
 f.name,
 f.status,
 f.type,
 f.threads

 FROM pre_forum_forum f

 WHERE f.status=1

 AND f.type='forum'

 ORDER BY f.displayorder ASC",

array()

);



$list=array();


foreach($forums as $row){


    $list[]=array(

        'fid'=>$row['fid'],

        'name'=>$row['name'],

        'threads'=>$row['threads']

    );


}



echo json_encode([


'code'=>0,


'data'=>array(

    'count'=>count($list),

    'list'=>$list

)


],JSON_UNESCAPED_UNICODE);

