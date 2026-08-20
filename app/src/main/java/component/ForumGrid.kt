package com.qjw.forum.component


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qjw.forum.HomeForum



@Composable
fun ForumGrid(

    forums:List<HomeForum>,

    onClick:(String)->Unit

){



    LazyVerticalGrid(

        columns =
            GridCells.Fixed(3),

        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    max = 180.dp
                ),

        contentPadding =
            PaddingValues(
                horizontal = 10.dp
            )

    ){



        items(forums){ forum ->




            Card(

                modifier =
                    Modifier

                        .padding(5.dp)

                        .fillMaxWidth()

                        .clickable{


                            onClick(
                                forum.fid
                            )


                        },


                shape =
                    RoundedCornerShape(12.dp)


            ){



                Box(

                    modifier =
                        Modifier
                            .height(60.dp)
                            .fillMaxWidth(),

                    ){



                    Text(

                        text =
                            forum.name,


                        modifier =
                            Modifier
                                .padding(12.dp)

                    )



                }



            }





        }




    }



}