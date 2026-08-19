<?php

define('IN_API',true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';

C::app()->init();

header('Content-Type: application/json; charset=utf-8');


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


$plid=intval($_GET['plid'] ?? 0);


if(!$plid){

    echo json_encode([
        'code'=>400,
        'message'=>'plid不能为空'
    ],JSON_UNESCAPED_UNICODE);

    exit;
}


$member=DB::fetch_first(

"SELECT *
 FROM pre_ucenter_pm_members
 WHERE plid=%d
 AND uid=%d",

array(
$plid,
$uid
)

);


if(!$member){

    echo json_encode([
        'code'=>403,
        'message'=>'无权查看'
    ],JSON_UNESCAPED_UNICODE);

    exit;
}



$list=array();


for($i=0;$i<=9;$i++){


    $table='pre_ucenter_pm_messages_'.$i;


    $rows=DB::fetch_all(

    "SELECT
        m.pmid,
        m.authorid,
        m.message,
        m.dateline,
        u.username

     FROM ".$table." m

     LEFT JOIN pre_common_member u

     ON m.authorid=u.uid

     WHERE m.plid=%d

     ORDER BY m.dateline ASC",

    array($plid)

    );


    foreach($rows as $row){


        $list[]=array(

            'pmid'=>$row['pmid'],

            'uid'=>$row['authorid'],

            'username'=>$row['username'],

            'message'=>htmlspecialchars_decode(strip_tags($row['message'])),

            'dateline'=>$row['dateline']

        );


    }


}



usort($list,function($a,$b){

    return $a['dateline']-$b['dateline'];

});



DB::query(

"UPDATE pre_ucenter_pm_members
 SET isnew=0
 WHERE plid=%d
 AND uid=%d",

array(
$plid,
$uid
)

);



echo json_encode([

'code'=>0,

'data'=>array(

    'plid'=>$plid,

    'list'=>$list

)

],JSON_UNESCAPED_UNICODE);
