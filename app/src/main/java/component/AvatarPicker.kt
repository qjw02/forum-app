package com.qjw.forum.component


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage



@Composable
fun AvatarPicker(

    avatar:String?,

    onSelect:(Uri)->Unit

){



    val launcher =

        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts.GetContent()

        ){ uri ->


            if(uri!=null){

                onSelect(uri)

            }


        }






    AsyncImage(

        model =

            avatar
                ?: "https://a3x9r3.cdlf3.com/default_avatar.png",


        contentDescription=null,


        modifier =

            Modifier

                .size(100.dp)

                .clickable {


                    launcher.launch("image/*")


                }


    )



}