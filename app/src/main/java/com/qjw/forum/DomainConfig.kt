package com.qjw.forum


import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject



object DomainManager {


    private const val CONFIG_URL =
        "https://raw.githubusercontent.com/qjw02/qjwconfig/main/domain.json"



    private const val PREF =
        "domain_cache"


    private const val KEY_DOMAIN =
        "current_domain"



    private var context:Context? = null



    fun init(ctx:Context){

        context =
            ctx.applicationContext

    }





    fun getDomain():String{

        return context
            ?.getSharedPreferences(
                PREF,
                Context.MODE_PRIVATE
            )
            ?.getString(
                KEY_DOMAIN,
                ""
            )
            ?: ""

    }






    private fun saveDomain(url:String){

        context
            ?.getSharedPreferences(
                PREF,
                Context.MODE_PRIVATE
            )
            ?.edit()
            ?.putString(
                KEY_DOMAIN,
                url
            )
            ?.apply()

    }







    suspend fun updateDomain(){


        withContext(
            Dispatchers.IO
        ){


            try{


                val client =
                    OkHttpClient()



                val request =
                    Request.Builder()
                        .url(CONFIG_URL)
                        .build()



                val response =
                    client
                        .newCall(request)
                        .execute()



                val body =
                    response.body?.string()
                        ?: return@withContext



                val json =
                    JSONObject(body)



                val list =
                    json
                        .getJSONArray(
                            "api_list"
                        )



                for(i in 0 until list.length()){


                    val domain =
                        list.getString(i)



                    if(check(domain)){


                        saveDomain(domain)

                        break

                    }


                }



            }catch(e:Exception){


            }


        }


    }







    private fun check(
        domain:String
    ):Boolean{


        return try{


            val client =
                OkHttpClient()



            val request =
                Request.Builder()
                    .url(
                        domain +
                        "/api/home/index.php"
                    )
                    .build()



            val response =
                client
                    .newCall(request)
                    .execute()



            response.isSuccessful



        }catch(e:Exception){


            false

        }

    }

}