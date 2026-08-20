package com.qjw.forum.component


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qjw.forum.Post



@Composable
fun PostCard(

    post:Post,

    onClick:(String)->Unit

){


    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 5.dp
                )
                .clickable{

                    onClick(
                        post.tid
                    )

                },

        shape =
            RoundedCornerShape(12.dp)

    ){


        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp)

        ){


            Text(

                text =
                    if((post.displayorder ?: 0) > 0){
                        "📌 置顶  " + post.subject
                    }else{
                        post.subject
                    },


                style =
                    MaterialTheme
                        .typography
                        .titleMedium,


                maxLines = 2

            )



            Spacer(

                Modifier.height(8.dp)

            )



            Text(

                text =
                    "作者: ${post.author}",


                style =
                    MaterialTheme
                        .typography
                        .bodyMedium

            )



            Spacer(

                Modifier.height(6.dp)

            )



            Text(

                text =
                    "浏览 ${post.views}   回复 ${post.replies}",


                style =
                    MaterialTheme
                        .typography
                        .bodySmall

            )


        }


    }


}