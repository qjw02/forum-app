package com.qjw.forum.component


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter



@Composable
fun ImageViewer(

    url:String

){


    var show by remember {

        mutableStateOf(false)

    }



    Image(

        painter =
            rememberAsyncImagePainter(url),


        contentDescription=null,


        contentScale =
            ContentScale.Crop,


        modifier =

            Modifier

                .fillMaxWidth()

                .height(250.dp)

                .clickable {

                    show=true

                }

    )





    if(show){



        androidx.compose.foundation.layout.Box(

            modifier =

                Modifier

                    .fillMaxSize()

                    .background(Color.Black)

                    .clickable {

                        show=false

                    }

        ){



            Image(

                painter =
                    rememberAsyncImagePainter(url),


                contentDescription=null,


                contentScale =
                    ContentScale.Fit,


                modifier =
                    Modifier.fillMaxSize()


            )


        }


    }



}