package com.qjw.forum


import android.content.Context



object DomainCache {



    private const val PREF_NAME =
        "domain_cache"



    private const val KEY_DOMAIN =
        "current_domain"



    private const val KEY_TIME =
        "update_time"





    private var context:Context? = null





    fun init(ctx:Context){


        context =
            ctx.applicationContext


    }





    fun save(

        domain:String

    ){


        context

            ?.getSharedPreferences(

                PREF_NAME,

                Context.MODE_PRIVATE

            )

            ?.edit()

            ?.putString(

                KEY_DOMAIN,

                domain

            )

            ?.putLong(

                KEY_TIME,

                System.currentTimeMillis()

            )

            ?.apply()


    }







    fun get():String{


        return context

            ?.getSharedPreferences(

                PREF_NAME,

                Context.MODE_PRIVATE

            )

            ?.getString(

                KEY_DOMAIN,

                ""

            )

            ?: ""


    }







    fun getUpdateTime():Long{


        return context

            ?.getSharedPreferences(

                PREF_NAME,

                Context.MODE_PRIVATE

            )

            ?.getLong(

                KEY_TIME,

                0

            )

            ?:0


    }






    fun clear(){


        context

            ?.getSharedPreferences(

                PREF_NAME,

                Context.MODE_PRIVATE

            )

            ?.edit()

            ?.clear()

            ?.apply()


    }


}