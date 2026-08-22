package com.qjw.forum

import android.os.Bundle
import android.graphics.Color
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import com.qjw.forum.navigation.AppNav
import kotlinx.coroutines.launch
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.decode.GifDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache


class MainActivity : ComponentActivity(){


    override fun onCreate(

        savedInstanceState:Bundle?

    ){

        super.onCreate(savedInstanceState)

        // 保留系统状态栏，显示时间、信号、Wi-Fi 和电量图标。
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(255, 247, 255)
        window.navigationBarColor = Color.rgb(255, 247, 255)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }



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
            .memoryCache {
                MemoryCache.Builder(applicationContext)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("qjw_image_cache"))
                    .maxSizeBytes(150L * 1024L * 1024L)
                    .build()
            }
            .build()

        setContent {

            CompositionLocalProvider(LocalImageLoader provides appImageLoader) {
                AppNav()
            }

        }


    }


}