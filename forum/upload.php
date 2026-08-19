<?php

define('IN_API', true);

require_once '/www/wwwroot/qq/wwwroot/source/class/class_core.php';
require_once '/www/wwwroot/qq/wwwroot/source/function/function_member.php';

C::app()->init();

header('Content-Type: application/json; charset=utf-8');


/*
 * Token
 */

$token = '';

if(isset($_SERVER['HTTP_X_TOKEN'])) {
    $token = trim($_SERVER['HTTP_X_TOKEN']);
}

if(!$token) {

    echo json_encode([
        'code' => 401,
        'message' => 'token缺失'
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


/*
 * 验证 Token
 */

$data = C::t('app_token')->get_by_token($token);

if(!$data) {

    echo json_encode([
        'code' => 401,
        'message' => 'token无效'
    ], JSON_UNESCAPED_UNICODE);

    exit;
}

if($data['expire'] < TIMESTAMP) {

    echo json_encode([
        'code' => 401,
        'message' => 'token过期'
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


$uid = intval($data['uid']);


/*
 * 获取用户
 */

$member = getuserbyuid($uid, 1);

if(!$member) {

    echo json_encode([
        'code' => 404,
        'message' => '用户不存在'
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


/*
 * 检查上传文件
 */

if(!isset($_FILES['file'])) {

    echo json_encode([
        'code' => 400,
        'message' => '没有上传文件'
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


$file = $_FILES['file'];


/*
 * PHP 上传错误
 */

if($file['error'] !== UPLOAD_ERR_OK) {

    echo json_encode([
        'code' => 400,
        'message' => '文件上传失败',
        'error' => intval($file['error'])
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


/*
 * 文件大小
 */

if($file['size'] <= 0) {

    echo json_encode([
        'code' => 400,
        'message' => '文件为空'
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


/*
 * 限制图片格式
 *
 * Discuz X3.5 当前上传类支持：
 * jpg / jpeg / gif / png / bmp
 */

$ext = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));

$allow_ext = array(
    'jpg',
    'jpeg',
    'png',
    'gif',
    'bmp'
);

if(!in_array($ext, $allow_ext)) {

    echo json_encode([
        'code' => 400,
        'message' => '不支持的图片格式'
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


/*
 * 使用 Discuz 原生上传类
 */

$upload = new discuz_upload();


/*
 * 初始化
 */

if(!$upload->init($file, 'forum')) {

    echo json_encode([
        'code' => 400,
        'message' => '图片初始化失败',
        'error' => $upload->error()
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


/*
 * 保存文件
 */

if(!$upload->save()) {

    echo json_encode([
        'code' => 400,
        'message' => '图片保存失败',
        'error' => $upload->error()
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


/*
 * 获取上传结果
 */

$attach = $upload->attach;

$attachment = $attach['attachment'];
$filename   = $attach['name'];
$filesize   = intval($attach['size']);
$isimage    = intval($attach['isimage']);
$width      = 0;

if(!empty($attach['imageinfo'])) {

    $width = intval($attach['imageinfo'][0]);

}


/*
 * 生成附件 ID
 *
 * 先进入 Discuz 未使用附件表。
 * 等 APP 发帖时，再把它绑定到 tid / pid。
 */

$aid = getattachnewaid($uid);


C::t('forum_attachment_unused')->insert(array(

    'aid'        => $aid,
    'dateline'   => TIMESTAMP,
    'filename'   => strip_tags(str_replace('"', '', $filename)),
    'filesize'   => $filesize,
    'attachment' => $attachment,
    'isimage'    => $isimage,
    'uid'        => $uid,
    'thumb'      => 0,
    'remote'     => 0,
    'width'      => $width

));


/*
 * 返回
 */

echo json_encode([

    'code' => 0,

    'message' => '上传成功',

    'data' => array(

        'aid'        => $aid,
        'filename'   => $filename,
        'filesize'   => $filesize,
        'attachment' => $attachment,
        'isimage'    => $isimage,
        'width'      => $width

    )

], JSON_UNESCAPED_UNICODE);

