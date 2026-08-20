package com.qjw.forum.component


import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter



@Composable
fun ImagePreview(


    images: MutableList<Uri>,


    onRemove:(Int)->Unit


){



    LazyRow{


        itemsIndexed(images){ index, uri ->




            Card(

                modifier =
                    Modifier
                        .padding(5.dp)
                        .size(100.dp)

            ){



                Box(


                    modifier =
                        Modifier.fillMaxSize()


                ){





                    Image(

                        painter =
                            rememberAsyncImagePainter(uri),


                        contentDescription = null,


                        modifier =
                            Modifier.fillMaxSize()


                    )






                    IconButton(

                        onClick = {


                            onRemove(index)


                        },


                        modifier =
                            Modifier
                                .size(30.dp)

                    ){



                        Text(

                            text = "×",

                            style =
                                MaterialTheme.typography.titleLarge

                        )



                    }





                }



            }



        }



    }



}