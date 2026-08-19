package com.qjw.forum

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReferralScreen(){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){

        Text(
            text = "推广中心",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(20.dp))

        Text("我的推广链接")

        Spacer(Modifier.height(10.dp))

        Text(
            "https://你的域名/member.php?mod=register&referid=${UserStore.getUid()}"
        )

        Spacer(Modifier.height(20.dp))

        Text("邀请好友注册可获得论坛推广奖励")

    }

}
