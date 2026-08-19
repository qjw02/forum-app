package com.qjw.forum.component


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qjw.forum.Banner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerView(

    banners:List<Banner>,

    onClick:(String)->Unit = {}

){


    if(banners.isEmpty()){

        return

    }



    val pagerState = rememberPagerState(

        initialPage = 0,

        pageCount = {

            banners.take(5).size

        }

    )



    val scope = rememberCoroutineScope()



    LaunchedEffect(Unit){

        while(true){

            delay(4000)


            val next =
                (pagerState.currentPage + 1) %
                        banners.take(5).size


            scope.launch{

                pagerState.animateScrollToPage(next)

            }


        }

    }




    Column(

        modifier =
            Modifier.fillMaxWidth()

    ){



        HorizontalPager(

            state = pagerState,


            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)

        ){ page ->



            val banner =
                banners.take(5)[page]



            Card(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                        .clickable{

                            banner.tid?.let {

                                onClick(it)

                            }

                        },


                shape =
                    RoundedCornerShape(14.dp)

            ){



                Column(

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(20.dp)

                ){



                    Text(

                        text =
                            banner.subject
                                ?: "QJWForum",


                        style =
                            MaterialTheme
                                .typography
                                .titleLarge


                    )



                    Spacer(

                        Modifier.height(10.dp)

                    )



                    Text(

                        text =
                            "推荐帖子",


                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium

                    )



                }



            }



        }





        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 4.dp
                    ),

            horizontalArrangement =
                Arrangement.Center

        ){



            repeat(
                banners.take(5).size
            ){ index ->



                Text(

                    text =
                        if(index == pagerState.currentPage)
                            "●"
                        else
                            "○"

                )



            }



        }




    }



}