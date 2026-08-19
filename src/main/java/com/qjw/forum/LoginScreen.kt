package com.qjw.forum

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess:()->Unit,
    onRegister:()->Unit,
    onForgotPassword:()->Unit
){

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(30.dp)
    ){

        Text(
            text="QJWForum 登录",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value=username,
            onValueChange={ username=it },
            label={ Text("用户名") },
            modifier=Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value=password,
            onValueChange={ password=it },
            label={ Text("密码") },
            modifier=Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            modifier=Modifier.fillMaxWidth(),
            onClick={

                scope.launch{

                    try{

                        val result =
                            ApiClient.api.login(
                                username,
                                password
                            )

                        if(result.code==0){

                            result.data?.let { data ->

                                UserStore.saveUser(
                                    uid=data.uid,
                                    username=data.username,
                                    token=data.token
                                )

                            }

                            message="登录成功"
                            onLoginSuccess()

                        }else{

                            message=result.message ?: "登录失败"

                        }

                    }catch(e:Exception){

                        message=e.message ?: "网络错误"

                    }

                }

            }

        ){

            Text("登录")

        }


        Spacer(Modifier.height(15.dp))


        Row(
            modifier=Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.SpaceAround
        ){

            TextButton(
                onClick={
                    onRegister()
                }
            ){

                Text("注册账号")

            }


            TextButton(
                onClick={
                    onForgotPassword()
                }
            ){

                Text("忘记密码")

            }

        }


        Spacer(Modifier.height(10.dp))

        Text(message)

    }

}