<?php
error_reporting(0);
if(!defined('IN_API')){
    define('IN_API',true);
}


require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';


C::app()->init();



require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_forum.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_post.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_discuzcode.php';


C::app()->init();


header('Content-Type: application/json; charset=utf-8');





function avatar_url($uid){

    return
    'https://a3x9r3.cdlf3.com/uc_server/avatar.php?uid='
    .$uid
    .'&size=middle';

}





/*
 * 图片地址统一
 */

function fix_image_url($message){


    if(!$message){

        return '';

    }



    $message=preg_replace(

        '/\[img\](\/data\/attachment\/forum\/.*?)\[\/img\]/i',

        '[img]https://a3x9r3.cdlf3.com$1[/img]',

        $message

    );



    return $message;

}





/*
 * 提取图片
 */

function parse_images($message){


    $images=array();



    preg_match_all(

        '/\[img\](.*?)\[\/img\]/i',

        $message,

        $match

    );



    if(!empty($match[1])){


        foreach($match[1] as $img){


            if(strpos($img,'http')!==0){


                $img='https://a3x9r3.cdlf3.com'.$img;


            }


            $images[]=$img;


        }


    }



    return $images;

}





/*
 * Discuz BBCode解析
 */

function parse_message_html($message){


    if(!$message){

        return '';

    }



    return discuzcode(

        $message,

        0,

        0,

        0,

        0,

        1,

        1

    );

}





/*
 * 参数
 */


$tid=intval($_GET['tid'] ?? 0);


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


/*
 * 指定回复定位：自动请求该回复所在页
 */
$focus_pid=intval($_GET['pid'] ?? 0);

if($focus_pid && $tid){

    $before=DB::result_first(

        "SELECT COUNT(*)
         FROM pre_forum_post
         WHERE tid=%d
         AND first=0
         AND pid<%d",

        array($tid,$focus_pid)

    );

    $page=intval(floor(intval($before)/$page_size))+1;

}


$start=($page-1)*$page_size;





if(!$tid){


    echo json_encode([

        'code'=>400,

        'message'=>'tid不能为空'

    ],JSON_UNESCAPED_UNICODE);


    exit;

}






/*
 * 增加浏览量
 */

DB::query(

"UPDATE pre_forum_thread
 SET views=views+1
 WHERE tid=%d",

array($tid)

);







/*
 * 主题
 */


$thread=DB::fetch_first(

"SELECT

tid,
fid,
author,
authorid,
subject,
views,
replies,
dateline

FROM pre_forum_thread

WHERE tid=%d",

array($tid)

);






if(!$thread){


    echo json_encode([

        'code'=>404,

        'message'=>'帖子不存在'

    ],JSON_UNESCAPED_UNICODE);



    exit;

}









/*
 * 联系方式收费检测
 */

$contact=array(
    'locked'=>false
);


$contact_row=DB::fetch_first(

    "SELECT value
     FROM pre_forum_typeoptionvar
     WHERE tid=%d
     AND optionid=7",

    array($tid)

);


if($contact_row){

    $price=DB::result_first(

        "SELECT price
         FROM pre_app_thread_field_price
         WHERE tid=%d
         AND optionid=7",

        array($tid)

    );


 if($price){


    // 默认隐藏

    $contact=array(

        'locked'=>true,

        'optionid'=>7,

        'price'=>intval($price)

    );



    // 检查APP登录token

    $token=$_GET['token'] ?? '';



    if($token){


        $uid=DB::result_first(

            "SELECT uid
             FROM pre_app_token
             WHERE token=%s",

            array($token)

        );



        if($uid){


            // 查询是否购买过

            $buy=DB::fetch_first(

                "SELECT id
                 FROM pre_app_field_buy
                 WHERE tid=%d
                 AND uid=%d
                 AND optionid=7",

                array(

                    $tid,

                    $uid

                )

            );



            if($buy){


                $contact=array(

                    'locked'=>false,

                    'optionid'=>7,

                    'value'=>$contact_row['value']

                );


            }


        }


    }


}

}


/*
 * 首帖
 */


$first=DB::fetch_first(

"SELECT message

FROM pre_forum_post

WHERE tid=%d

AND first=1",

array($tid)

);








/*
 * 回复数量
 */


$total=DB::result_first(

"SELECT COUNT(*)

FROM pre_forum_post

WHERE tid=%d

AND first=0",

array($tid)

);








/*
 * 回复列表
 */


$posts=DB::fetch_all(

"SELECT

pid,
author,
authorid,
message,
dateline

FROM pre_forum_post

WHERE tid=%d

AND first=0

ORDER BY dateline ASC

LIMIT %d,%d",

array(

$tid,

$start,

$page_size

)

);






$reply_list=array();






foreach($posts as $row){



    $raw=fix_image_url(

        $row['message']

    );



    $reply_list[]=array(


        'pid'=>$row['pid'],



        'author'=>array(


            'uid'=>$row['authorid'],

            'username'=>$row['author'],

            'avatar'=>avatar_url($row['authorid'])


        ),




        'message'=>parse_message_html($raw),



        'images'=>parse_images($raw),



        'dateline'=>$row['dateline']


    );



}




/*
 * 获取帖子图片附件
 */
function get_thread_images($tid){


    $images=array();



    $attachments=DB::fetch_all(

        "SELECT aid,tableid
         FROM pre_forum_attachment
         WHERE tid=%d",

        array($tid)

    );



    foreach($attachments as $att){


        $table='pre_forum_attachment_'.$att['tableid'];



        $row=DB::fetch_first(

            "SELECT attachment,isimage
             FROM ".$table."
             WHERE aid=%d",

            array($att['aid'])

        );



        if($row && $row['isimage']){


            $images[] =
                'https://a3x9r3.cdlf3.com/data/attachment/forum/'
                .$row['attachment'];


        }


    }



    return $images;

}




/*
 * 首帖内容
 */


$raw_content=fix_image_url(

    $first['message'] ?? ''

);



$content=parse_message_html(

    $raw_content

);







echo json_encode([



'code'=>0,



'data'=>array(



'thread'=>array(


    'tid'=>$thread['tid'],



    'fid'=>$thread['fid'],



    'subject'=>$thread['subject'],



    'author'=>array(


        'uid'=>$thread['authorid'],

        'username'=>$thread['author'],

        'avatar'=>avatar_url($thread['authorid'])


    ),




    'views'=>$thread['views']+1,



    'replies'=>$thread['replies'],



    'dateline'=>$thread['dateline'],



    'content'=>$content,



    'images'=>array_merge(

    parse_images($raw_content),

    get_thread_images($tid)

),

'contact'=>$contact

),






'replies'=>array(


    'page'=>$page,


    'page_size'=>$page_size,


    'total'=>intval($total),


    'list'=>$reply_list


)




)



],JSON_UNESCAPED_UNICODE);