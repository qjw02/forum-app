package com.qjw.forum


import android.content.Context



object UserStore {



    private const val PREF_NAME =
        "qjw_user"



    private const val KEY_UID =
        "uid"



    private const val KEY_USERNAME =
        "username"



    private const val KEY_TOKEN =
        "token"





    private var context: Context? = null





    fun init(ctx: Context){


        context =
            ctx.applicationContext


    }






    private fun prefs() =

        context?.getSharedPreferences(

            PREF_NAME,

            Context.MODE_PRIVATE

        )








    fun saveUser(

        uid:Int,

        username:String,

        token:String

    ){



        prefs()

            ?.edit()

            ?.putInt(

                KEY_UID,

                uid

            )

            ?.putString(

                KEY_USERNAME,

                username

            )

            ?.putString(

                KEY_TOKEN,

                token

            )

            ?.apply()



    }








    fun getToken():String{


        return prefs()

            ?.getString(

                KEY_TOKEN,

                ""

            )

            ?: ""



    }







    fun getUid():Int{


        return prefs()

            ?.getInt(

                KEY_UID,

                0

            )

            ?:0



    }







    fun getUsername():String{


        return prefs()

            ?.getString(

                KEY_USERNAME,

                ""

            )

            ?: ""



    }







    fun isLogin():Boolean{


        return getToken()

            .isNotEmpty()



    }







    fun clear(){



        prefs()

            ?.edit()

            ?.clear()

            ?.apply()



    }



}