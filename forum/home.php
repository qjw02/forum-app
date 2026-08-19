<?php

define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



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
 * 最新帖子
 */


$total=DB::result_first(

"SELECT COUNT(*)
 FROM %t
 WHERE displayorder>=0",

array(
'forum_thread'
)

);



$threads=DB::fetch_all(

"SELECT

tid,
fid,
author,
authorid,
subject,
views,
replies,
dateline,
lastpost,
lastposter


FROM %t


WHERE displayorder>=0


ORDER BY dateline DESC


LIMIT %d,%d",


array(

'forum_thread',

$start,

$page_size

)

);



$list=array();



foreach($threads as $row){


$list[]=array(

'tid'=>$row['tid'],

'fid'=>$row['fid'],

'author'=>$row['author'],

'authorid'=>$row['authorid'],

'subject'=>$row['subject'],

'views'=>$row['views'],

'replies'=>$row['replies'],

'dateline'=>$row['dateline'],

'lastposter'=>$row['lastposter']

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

