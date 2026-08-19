package com.qjw.forum


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qjw.forum.component.BannerView
import com.qjw.forum.component.NoticeCard
import com.qjw.forum.component.PostCard
import kotlinx.coroutines.launch



@Composable
fun HomeScreen(

    onOpenThread:(String)->Unit,

    onOpenForum:(String)->Unit

){


    var homeData by remember {

        mutableStateOf<HomeData?>(null)

    }


    var loading by remember {

        mutableStateOf(true)

    }


    var errorText by remember {

        mutableStateOf("")

    }


    val scope =
        rememberCoroutineScope()



    fun loadHome(){

        scope.launch{


            loading=true


            try{


                val result =
                    ApiClient.api.getHomeIndex()



                if(result.code==0){


                    homeData =
                        result.data



                    result.data?.new?.let {


                        PostCache.save(it)


                    }



                }else{


                    errorText =
                        result.message ?: "加载失败"


                }



            }catch(e:Exception){


                errorText =
                    e.message ?: "网络错误"


            }



            loading=false



        }


    }





    LaunchedEffect(Unit){

        loadHome()

    }





    Box(

        modifier =
            Modifier.fillMaxSize()

    ){



        if(loading){



            Box(

                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center

            ){

                CircularProgressIndicator()

            }



        }else{



            LazyColumn(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp),

                contentPadding =
                    PaddingValues(
                        bottom = 20.dp
                    )

            ){



                item{


                    Text(

                        text="QJWForum",

                        style =
                            MaterialTheme.typography.titleLarge,

                        modifier =
                            Modifier.padding(
                                horizontal = 15.dp,
                                vertical = 10.dp
                            )

                    )


                }





                item{


                    NoticeCard()


                }





                item{


                    BannerView(


                        banners =
                            homeData?.banner
                                ?: emptyList(),


                        onClick = { tid ->


                            onOpenThread(tid)


                        }


                    )


                }





                item{


                    Text(

                        text="热门帖子",


                        style =
                            MaterialTheme.typography.titleMedium,


                        modifier =
                            Modifier.padding(
                                horizontal = 15.dp,
                                vertical = 10.dp
                            )


                    )


                }





                items(


                    homeData?.hot
                        ?: emptyList()


                ){ post ->



                    PostCard(

                        post = post,

                        onClick =
                            onOpenThread

                    )


                }





                item{


                    Text(

                        text="最新帖子",


                        style =
                            MaterialTheme.typography.titleMedium,


                        modifier =
                            Modifier.padding(
                                horizontal = 15.dp,
                                vertical = 10.dp
                            )


                    )


                }





                items(


                    homeData?.new
                        ?: emptyList()


                ){ post ->



                    PostCard(

                        post = post,

                        onClick =
                            onOpenThread

                    )


                }



            }



        }






        if(errorText.isNotEmpty()){


            Snackbar(

                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .padding(16.dp)

            ){

                Text(errorText)

            }


        }



    }



}