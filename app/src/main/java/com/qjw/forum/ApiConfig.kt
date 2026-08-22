package com.qjw.forum


object ApiConfig {


    // 首次启动兜底地址。动态域名配置成功后会自动覆盖，正常使用不依赖此值。
    var baseUrl =
        "https://a3x9r3.cdlf3.com/"



    // 首次启动图片兜底地址；动态配置成功后自动覆盖。
    var imageUrl =
        "https://a3x9r3.cdlf3.com/"




    // GitHub配置文件

    const val CONFIG_URL =

        "https://raw.githubusercontent.com/qjw02/qjwforum-config/main/config.json"



    //备用域名

    var backupUrls =

        mutableListOf<String>()



}