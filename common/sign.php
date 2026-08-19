<?php


function check_api_sign(){


    $token=$_SERVER['HTTP_X_TOKEN'] ?? '';

    $time=$_SERVER['HTTP_X_TIME'] ?? '';

    $nonce=$_SERVER['HTTP_X_NONCE'] ?? '';

    $sign=$_SERVER['HTTP_X_SIGN'] ?? '';



    if(!$token || !$time || !$nonce || !$sign){

        return false;

    }



    // 时间限制 5分钟

    if(abs(TIMESTAMP-intval($time)) > 300){

        return false;

    }



    // APP密钥

    $secret='APP_SECRET_2026';



    $check=md5(
        $token.
        $time.
        $nonce.
        $secret
    );



    if($check!=$sign){

        return false;

    }



    return true;

}

