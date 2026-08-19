<?php error_reporting(E_ALL); ini_set('display_errors','1'); 
define('IN_API',true); require_once 
'/www/wwwroot/qq/wwwroot/source/class/class_core.php'; require_once 
'/www/wwwroot/qq/wwwroot/source/function/function_member.php'; 
C::app()->init(); header('Content-Type: application/json; charset=utf-8'); 
$username = $_POST['username'] ?? ''; $password = $_POST['password'] ?? ''; 
if(!$username || !$password){
    echo json_encode([ 'code'=>400, 'message'=>'用户名或密码不能为空' 
    ],JSON_UNESCAPED_UNICODE); exit;
}
$result = userlogin( $username, $password, 0, '', 'username', '' ); 
if($result['status'] != 1){
    echo json_encode([ 'code'=>401, 'message'=>'用户名或密码错误' 
    ],JSON_UNESCAPED_UNICODE); exit;
}
$member = $result['member']; /* 创建APP Token */ $token = 
bin2hex(random_bytes(32)); $expire = TIMESTAMP + 2592000; 
C::t('app_token')->insert_token(
    $member['uid'], $token, $expire ); unset($member['password']); echo 
json_encode([
    'code'=>0, 'message'=>'登录成功', 'token'=>$token, 'expire'=>$expire, 
    'user'=>$member
],JSON_UNESCAPED_UNICODE);
