package com.qjw.forum

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@Composable
fun ForgotPasswordScreen(

    onSuccess:()->Unit,

    onBack:()->Unit

){


    var username by remember {

        mutableStateOf("")

    }


    var password by remember {

        mutableStateOf("")

    }


    var message by remember {

        mutableStateOf("")

    }



    val scope =
        rememberCoroutineScope()



    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(30.dp)

    ){



        Text(

            "忘记密码",

            style =
                MaterialTheme.typography.headlineSmall

        )



        Spacer(
            Modifier.height(20.dp)
        )



        OutlinedTextField(

            value=username,

            onValueChange={
                username=it
            },

            label={
                Text("用户名")
            },

            modifier =
                Modifier.fillMaxWidth()

        )



        Spacer(
            Modifier.height(10.dp)
        )



        OutlinedTextField(

            value=password,

            onValueChange={
                password=it
            },

            label={
                Text("新密码")
            },

            modifier =
                Modifier.fillMaxWidth()

        )



        Spacer(
            Modifier.height(20.dp)
        )



        Button(

            modifier =
                Modifier.fillMaxWidth(),

            onClick={


                scope.launch{


                    try{


                        val result =

                            ApiClient.api.resetPassword(

                                username,

                                password

                            )



                        if(result.code==0){


                            message="密码修改成功"


                            onSuccess()


                        }else{


                            message =
                                result.message
                                    ?: "修改失败"


                        }



                    }catch(e:Exception){


                        message =
                            e.message
                                ?: "网络错误"


                    }


                }



            }

        ){

            Text("修改密码")

        }



        Spacer(
            Modifier.height(10.dp)
        )



        TextButton(

            onClick = {

                onBack()

            }

        ){

            Text("返回登录")

        }



        Text(message)



    }



}