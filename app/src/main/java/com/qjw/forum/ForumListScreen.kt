package com.qjw.forum


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch



@Composable
fun ForumListScreen(

    onOpenForum:(String)->Unit

){


    var forums by remember {

        mutableStateOf<List<ForumItem>>(emptyList())

    }



    var loading by remember {

        mutableStateOf(true)

    }



    var message by remember {

        mutableStateOf("")

    }




    val scope =

        rememberCoroutineScope()





    LaunchedEffect(Unit){


        scope.launch{


            try{


                val result =

                    ApiClient.api.getForums()



                if(result.code==0){


                    forums =

                        result.data ?: emptyList()


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







    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(15.dp)

    ){



        Text(

            text="板块",

            style =
                MaterialTheme.typography
                    .headlineSmall

        )





        Spacer(

            Modifier.height(15.dp)

        )






        if(loading){



            Text("加载中...")


        }else{



            LazyColumn{



                items(forums){ forum ->




                    Card(

                        modifier =
                            Modifier

                                .fillMaxWidth()

                                .padding(5.dp)

                                .clickable{



                                    onOpenForum(

                                        forum.fid.toString()

                                    )


                                }


                    ){



                        Text(

                            text =
                                forum.name,


                            modifier =
                                Modifier.padding(20.dp)



                        )



                    }



                }




            }



        }







        Text(message)



    }



}