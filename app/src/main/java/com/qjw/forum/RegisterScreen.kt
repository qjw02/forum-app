package com.qjw.forum

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@Composable
fun RegisterScreen(

    onSuccess:()->Unit,

    onBack:()->Unit

){

    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var email by remember {
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

            "注册账号",

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
                Text("密码")
            },

            modifier =
                Modifier.fillMaxWidth()

        )


        Spacer(
            Modifier.height(10.dp)
        )


        OutlinedTextField(

            value=email,

            onValueChange={
                email=it
            },

            label={
                Text("邮箱（可选）")
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
                            ApiClient.api.register(
                                username,
                                password,
                                email
                            )


                        if(result.code==0){

                            message="注册成功"

                            onSuccess()

                        }else{

                            message =
                                result.message ?: "注册失败"

                        }


                    }catch(e:Exception){

                        message =
                            e.message ?: "网络错误"

                    }


                }


            }

        ){

            Text("注册")

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