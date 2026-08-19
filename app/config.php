<?php

define('IN_API',true);


header('Content-Type: application/json; charset=utf-8');


// 当前主域名

$domain='https://a3x9r3.cdlf3.com';



echo json_encode([

'code'=>0,

'data'=>array(

    'api'=>$domain,

    'version'=>'1.0.0',

    'update'=>false,

    'notice'=>'',

    'features'=>array(

        'post'=>true,

        'reply'=>true,

        'image'=>true,

        'message'=>true,

        'sign'=>true

    )

)

],JSON_UNESCAPED_UNICODE);
