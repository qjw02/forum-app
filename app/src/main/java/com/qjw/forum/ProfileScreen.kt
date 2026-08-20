package com.qjw.forum


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch


@Composable
fun ProfileScreen(

    onLogout:()->Unit,

    onCreatePost:()->Unit,

    onReferral:()->Unit

){


    var profile by remember {
        mutableStateOf<ProfileData?>(null)
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
                    ApiClient.api.profile()


                if(result.code==0){

                    profile=result.data

                }else{

                    message=result.message ?: ""

                }


            }catch(e:Exception){

                message=e.message ?: "加载失败"

            }


            loading=false

        }

    }




    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 16.dp)

    ){


        Text(

            text="我的",

            style =
                MaterialTheme.typography.titleLarge

        )



        Spacer(
            Modifier.height(16.dp)
        )



        if(loading){


            Box(

                modifier =
                    Modifier.fillMaxWidth(),

                contentAlignment =
                    Alignment.Center

            ){

                CircularProgressIndicator()

            }


        }else{


            profile?.let { user ->



                Card(

                    modifier =
                        Modifier.fillMaxWidth()

                ){


                    Column(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(20.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally

                    ){



                        AsyncImage(

                            model=user.avatar,

                            contentDescription=null,

                            modifier =
                                Modifier.size(90.dp)

                        )



                        Spacer(
                            Modifier.height(10.dp)
                        )



                        Text(

                            user.username,

                            style =
                                MaterialTheme.typography.titleLarge

                        )



                        Spacer(
                            Modifier.height(15.dp)
                        )



                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceAround

                        ){


                            Text("UID\n${user.uid}")


                            Text("积分\n${user.credits ?:0}")


                            Text("C币\n${user.money ?:0}")


                            Text("主题\n${user.threads ?:0}")


                            Text("帖子\n${user.posts ?:0}")


                        }


                    }


                }





                Spacer(
                    Modifier.height(20.dp)
                )



                Text(

                    "我的功能",

                    style =
                        MaterialTheme.typography.titleMedium

                )



                Spacer(
                    Modifier.height(10.dp)
                )



                Card(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable{

                                onCreatePost()

                            }

                ){


                    Text(

                        "📝 发布帖子",

                        modifier =
                            Modifier.padding(18.dp)

                    )

                }



                Spacer(
                    Modifier.height(10.dp)
                )



                Card(

                    modifier =
                        Modifier.fillMaxWidth()

                ){


                    Text(

                        "📄 我的主题",

                        modifier =
                            Modifier.padding(18.dp)

                    )


                }



                Spacer(
                    Modifier.height(10.dp)
                )



                Card(

                    modifier =
                        Modifier.fillMaxWidth()

                ){


                    Text(

                        "💬 我的回复",

                        modifier =
                            Modifier.padding(18.dp)

                    )


                }




                Spacer(
                    Modifier.height(30.dp)
                )



                OutlinedButton(

                    modifier =
                        Modifier.fillMaxWidth(),

                    onClick={

                        UserStore.clear()

                        onLogout()

                    }

                ){

                    Text("退出登录")

                }



            }



        }



        Text(message)



    }


}