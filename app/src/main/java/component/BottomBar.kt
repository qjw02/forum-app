package com.qjw.forum.component


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector



data class BottomItem(

    val key:String,

    val title:String,

    val icon:ImageVector

)



@Composable
fun BottomBar(

    current:String,

    unreadCount:Int,

    onChange:(String)->Unit

){


    val items = listOf(


        BottomItem(

            "home",

            "首页",

            Icons.Default.Home

        ),


        BottomItem(

            "forum",

            "板块",

            Icons.Default.Forum

        ),


        BottomItem(

            "message",

            "消息",

            Icons.Default.Message

        ),


        BottomItem(

            "profile",

            "我的",

            Icons.Default.Person

        )


    )





    NavigationBar{


        items.forEach { item ->



            NavigationBarItem(


                selected =
                    current == item.key,


                onClick = {

                    onChange(item.key)

                },


                icon = {


                    if(item.key=="message" && unreadCount>0){

                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(if(unreadCount>99) "99+" else unreadCount.toString())
                                }
                            }
                        ){
                            Icon(
                                item.icon,
                                contentDescription = item.title
                            )
                        }

                    }else{

                        Icon(
                            item.icon,
                            contentDescription = item.title
                        )

                    }


                },


                label = {


                    Text(

                        item.title

                    )


                }


            )


        }



    }



}