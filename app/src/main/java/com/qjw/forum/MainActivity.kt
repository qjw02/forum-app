package com.qjw.forum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import com.qjw.forum.navigation.AppNav
import kotlinx.coroutines.launch
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.decode.GifDecoder


class MainActivity : ComponentActivity(){


    override fun onCreate(

        savedInstanceState:Bundle?

    ){

        super.onCreate(savedInstanceState)



        // 初始化用户存储

        UserStore.init(this)

        ContentCache.init(this)

        ProfileCache.init(this)



        // 初始化动态域名管理

        DomainManager.init(this)



        // 启动时更新GitHub域名配置

        lifecycleScope.launch {

            DomainManager.updateDomain()

        }





        // 恢复登录token

        val token =

            UserStore.getToken()



        if(!token.isNullOrEmpty()){


        }





        // 初始化网络

        ApiClient.init()





        val appImageLoader = ImageLoader.Builder(applicationContext)
            .components {
                add(GifDecoder.Factory())
            }
            .build()

        setContent {

            CompositionLocalProvider(LocalImageLoader provides appImageLoader) {
                AppNav()
            }

        }


    }


}