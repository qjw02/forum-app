package com.qjw.forum


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit



object ServerChecker {


    private val client =

        OkHttpClient.Builder()

            .connectTimeout(
                5,
                TimeUnit.SECONDS
            )

            .readTimeout(
                5,
                TimeUnit.SECONDS
            )

            .build()





    suspend fun check(

        url:String

    ):Boolean{


        return withContext(

            Dispatchers.IO

        ){


            try{


                val request =

                    Request.Builder()

                        .url(url)

                        .get()

                        .build()



                val response =

                    client.newCall(request)

                        .execute()



                response.isSuccessful



            }catch(e:Exception){


                false


            }


        }


    }



}