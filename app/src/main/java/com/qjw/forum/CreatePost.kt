package com.qjw.forum


import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.qjw.forum.component.ImagePicker
import com.qjw.forum.component.ImagePreview
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


@Composable
fun CreatePost(

    fid:String? = null,

    onOpenThread:(String)->Unit

){

    var forums by remember {
        mutableStateOf<List<ForumItem>>(emptyList())
    }

    var selectedForum by remember {
        mutableStateOf<ForumItem?>(null)
    }

    var subject by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    var resultText by remember {
        mutableStateOf("")
    }

    var showForumMenu by remember {
        mutableStateOf(false)
    }

    val images = remember {
        mutableStateListOf<Uri>()
    }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current


    LaunchedEffect(Unit){

        scope.launch{

            try{

                val result = ApiClient.api.getForums()

                if(result.code==0){

                    forums = result.data ?: emptyList()

                    if(fid != null){

                        selectedForum =
                            forums.find {
                                it.fid.toString()==fid
                            }

                    }

                }

            }catch(e:Exception){

                resultText =
                    e.message ?: ""

            }

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

            "发布帖子",

            style =
                MaterialTheme.typography.titleLarge

        )


        Spacer(
            Modifier.height(12.dp)
        )


        if(fid == null){


            OutlinedButton(

                modifier =
                    Modifier.fillMaxWidth(),

                onClick = {

                    showForumMenu = !showForumMenu

                }

            ){

                Text(
                    selectedForum?.name
                        ?: "选择发布板块"
                )

            }



            if(showForumMenu){

                Card(

                    modifier =
                        Modifier.fillMaxWidth()

                ){

                    Column{

                        forums.forEach { forum ->

                            Text(

                                text = forum.name,

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable{

                                            selectedForum = forum

                                            showForumMenu=false

                                        }
                                        .padding(14.dp)

                            )

                        }

                    }

                }

            }


        }else{


            Text(
                "发布到：${selectedForum?.name ?: "加载中..."}"
            )

        }



        Spacer(
            Modifier.height(12.dp)
        )



        OutlinedTextField(

            value = subject,

            onValueChange = {
                subject = it
            },

            label = {
                Text("标题")
            },

            modifier =
                Modifier.fillMaxWidth()

        )



        Spacer(
            Modifier.height(10.dp)
        )



        OutlinedTextField(

            value = message,

            onValueChange = {
                message = it
            },

            label = {
                Text("内容")
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)

        )



        Spacer(
            Modifier.height(12.dp)
        )


        ImagePicker(

            images = images,

            onChange = {}

        )


        if(images.isNotEmpty()){

            ImagePreview(

                images = images,

                onRemove = { index ->

                    images.removeAt(index)

                }

            )

        }



        Spacer(
            Modifier.height(15.dp)
        )


        Button(

            modifier =
                Modifier.fillMaxWidth(),

            onClick = {

                if(selectedForum == null){

                    resultText="请选择板块"

                    return@Button

                }


                scope.launch{


                    try{


                        var finalMessage = message



                        images.forEach { uri ->


                            val file = File(

                                context.cacheDir,

                                "upload_${System.currentTimeMillis()}.jpg"

                            )


                            context.contentResolver
                                .openInputStream(uri)
                                ?.use { input ->

                                    file.outputStream()
                                        .use { output ->

                                            input.copyTo(output)

                                        }

                                }



                            val body =
                                file.asRequestBody(
                                    "image/*".toMediaTypeOrNull()
                                )


                            val part =
                                MultipartBody.Part
                                    .createFormData(
                                        "file",
                                        file.name,
                                        body
                                    )


                            val upload =
                                ApiClient.api.uploadImage(part)


                            if(upload.code==0){

                                finalMessage +=

                                    "\n\n[img]" +
                                            ApiConfig.baseUrl +
                                            "data/attachment/forum/" +
                                            upload.data?.attachment +
                                            "[/img]"

                            }

                        }



                        val result =
                            ApiClient.api.createPost(

                                fid =
                                    selectedForum!!
                                        .fid
                                        .toString(),

                                subject = subject,

                                message = finalMessage

                            )



                        if(result.code==0){

                            PostCache.clear()

                            result.data?.tid?.let {

                                onOpenThread(
                                    it.toString()
                                )

                            }

                        }else{

                            resultText =
                                result.message ?: "发布失败"

                        }


                    }catch(e:Exception){

                        resultText =
                            e.message ?: "错误"

                    }


                }

            }

        ){

            Text("发布")

        }



        Spacer(
            Modifier.height(10.dp)
        )


        Text(resultText)


    }


}