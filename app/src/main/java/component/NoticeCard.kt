package com.qjw.forum.component


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun NoticeCard(

    text:String = "欢迎来到 QJWForum，请遵守社区规则，文明交流"

){

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 6.dp
                ),

        shape =
            RoundedCornerShape(12.dp)

    ){

        Column(

            modifier =
                Modifier.padding(15.dp)

        ){

            Text(

                text = "📢 公告",

                style =
                    MaterialTheme.typography.titleMedium

            )


            Spacer(
                Modifier.height(6.dp)
            )


            Text(

                text = text,

                style =
                    MaterialTheme.typography.bodyMedium

            )


        }

    }

}