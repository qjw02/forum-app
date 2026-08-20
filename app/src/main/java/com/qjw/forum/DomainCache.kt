package com.qjw.forum


import android.content.Context



object DomainCache {



    private const val PREF_NAME =

        "qjw_domain_cache"



    private const val KEY_API =

        "api_url"



    private const val KEY_IMAGE =

        "image_url"





    fun save(

        context: Context,

        api:String,

        image:String

    ){


        val sp =

            context.getSharedPreferences(

                PREF_NAME,

                Context.MODE_PRIVATE

            )



        sp.edit()

            .putString(

                KEY_API,

                api

            )

            .putString(

                KEY_IMAGE,

                image

            )

            .apply()



    }







    fun load(

        context: Context

    ){



        val sp =

            context.getSharedPreferences(

                PREF_NAME,

                Context.MODE_PRIVATE

            )




        val api =

            sp.getString(

                KEY_API,

                ""

            )





        val image =

            sp.getString(

                KEY_IMAGE,

                ""

            )





        if(!api.isNullOrEmpty()){


            ApiConfig.baseUrl = api


        }



        if(!image.isNullOrEmpty()){


            ApiConfig.imageUrl=image


        }



    }





}