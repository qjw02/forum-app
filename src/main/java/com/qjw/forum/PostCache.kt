package com.qjw.forum


object PostCache {


    var posts: List<Post> = emptyList()


    private var updateTime: Long = 0L



    fun isValid(): Boolean {


        val now = System.currentTimeMillis()


        return posts.isNotEmpty()
                &&
                (now - updateTime) < 10 * 60 * 1000


    }




    fun save(
        data: List<Post>
    ){

        posts = data


        updateTime =
            System.currentTimeMillis()

    }




    fun clear(){

        posts =
            emptyList()


        updateTime =
            0L

    }


}