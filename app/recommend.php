<?php


define('IN_API',true);


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');



$page=intval($_GET['page'] ?? 1);

$page_size=intval($_GET['page_size'] ?? 10);



if($page<1){

    $page=1;

}


if($page_size>20){

    $page_size=20;

}



$start=($page-1)*$page_size;



$list=C::t('app_recommend')->get_list(

    $start,

    $page_size

);



$result=array();



foreach($list as $row){


    $result[]=array(

        'id'=>$row['id'],

        'tid'=>$row['tid'],

        'title'=>$row['title'],

        'cover'=>$row['cover'],

        'description'=>$row['description'],

        'subject'=>$row['subject'],

        'author'=>$row['author'],

        'views'=>$row['views'],

        'replies'=>$row['replies'],

        'sort'=>$row['sort']

    );


}



echo json_encode([


    'code'=>0,


    'data'=>array(

        'list'=>$result

    )


],JSON_UNESCAPED_UNICODE);

