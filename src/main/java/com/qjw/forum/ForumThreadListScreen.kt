package com.qjw.forum


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qjw.forum.component.PostCard
import kotlinx.coroutines.launch



@Composable
fun ForumThreadListScreen(

    fid:String,

    onOpenThread:(String)->Unit,

    onCreatePost:(String)->Unit,

    onBack:()->Unit

){


    var forumData by remember {

        mutableStateOf<ForumThreadData?>(null)

    }


    var loading by remember {

        mutableStateOf(true)

    }


    var message by remember {

        mutableStateOf("")

    }


    val scope =
        rememberCoroutineScope()



    fun loadThreads(){


        scope.launch{


            try{


                val result =

                    ApiClient.api.getForumThreads(fid)



                if(result.code==0){


                    forumData =
                        result.data


                }else{


                    message =
                        result.message ?: "加载失败"


                }



            }catch(e:Exception){


                message =
                    e.message ?: "网络错误"


            }



            loading=false


        }


    }




    LaunchedEffect(fid){

        loadThreads()

    }





    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)

    ){



        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween

        ){



            Button(

                onClick = {

                    onBack()

                }

            ){

                Text("返回")

            }





            Button(

                onClick = {

                    onCreatePost(fid)

                }

            ){

                Text("＋发布主题")

            }



        }





        Spacer(

            Modifier.height(12.dp)

        )





        if(loading){


            Box(

                modifier =
                    Modifier.fillMaxWidth()

            ){

                Text("加载中...")

            }



        }else{



            Text(

                text =
                    forumData?.name ?: "板块",


                style =
                    MaterialTheme
                        .typography
                        .titleLarge,


                modifier =
                    Modifier.padding(
                        horizontal = 4.dp
                    )

            )





            Spacer(

                Modifier.height(10.dp)

            )





            if(forumData?.list.isNullOrEmpty()){


                Text("暂无帖子")



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



                    items(

                        forumData!!.list

                    ){ post ->



                        PostCard(

                            post = post,

                            onClick =
                                onOpenThread

                        )



                    }



                }





            }



        }





        if(message.isNotEmpty()){


            Text(

                text = message,

                modifier =
                    Modifier.padding(10.dp)

            )


        }





    }



}