package com.qjw.forum

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiClient {


    private var retrofit:Retrofit? = null


    private var currentUrl:String = ""



    fun init(){

        retrofit = null

        currentUrl = ""

    }





    val api:ApiService

        get(){


            val domain =
                DomainManager.getDomain()



            if(domain.isEmpty()){


                throw IllegalStateException(
                    "服务器地址未初始化"
                )


            }





            if(
                retrofit == null ||
                currentUrl != domain
            ){



                currentUrl =
                    domain





                retrofit =
                    Retrofit.Builder()


                        .baseUrl(

                            domain + "/"

                        )


                        .addConverterFactory(

                            GsonConverterFactory
                                .create()

                        )


                        .build()



            }





            return retrofit!!

                .create(

                    ApiService::class.java

                )


        }





    fun reset(){


        retrofit = null

        currentUrl = ""


    }


}