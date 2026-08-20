package com.qjw.forum


object PermissionManager {



    suspend fun check(

        fid:String

    ):Boolean{


        return try{


            val uid =
                UserStore.getUid()



            val result =
                ApiClient.api.checkPermission(

                    fid,

                    uid

                )



            result.code==0
                    &&
                    result.data?.allow==true



        }catch(e:Exception){


            false


        }



    }



}