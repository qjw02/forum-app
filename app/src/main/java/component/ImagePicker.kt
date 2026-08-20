package com.qjw.forum.component


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember



@Composable
fun ImagePicker(

    images: MutableList<Uri>,

    onChange: () -> Unit

){



    val launcher =


        rememberLauncherForActivityResult(

            contract =

                ActivityResultContracts.GetMultipleContents()

        ){ result ->



            images.clear()



            images.addAll(result)



            onChange()



        }






    Button(


        onClick = {


            launcher.launch("image/*")


        }


    ){



        Text(

            if(images.isEmpty())

                "选择图片"

            else

                "已选择 ${images.size} 张"

        )



    }



}