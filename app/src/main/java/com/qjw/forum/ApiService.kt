package com.qjw.forum

import okhttp3.MultipartBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


interface ApiService {


    @FormUrlEncoded
    @POST("api/user/login.php")
    suspend fun login(

        @Field("username")
        username:String,

        @Field("password")
        password:String

    ):LoginResponse




    @GET("api/user/profile.php")
    suspend fun profile():

            ProfileResponse




    @GET("api/forum/user_profile.php")
    suspend fun getUserProfile(
        @Query("uid") uid: String
    ): PublicProfileResponse


    @GET("api/home/index.php")
    suspend fun getHomeIndex():

            HomeResponse




    @GET("api/forum/search.php")
    suspend fun searchThreads(
        @Query("keyword") keyword: String
    ): ForumSearchResponse

    @GET("api/forum/thread.php")
    suspend fun getThread(
        @Query("tid")
        tid: String,

        @Query("token")
        token: String = UserStore.getToken(),

        @Query("pid")
        pid: String? = null,

        @Query("page")
        page: Int = 1
    ): ThreadResponse





    @FormUrlEncoded
    @POST("api/forum/post.php")
    suspend fun createPost(
        @Field("fid")
        fid:String,

        @Field("subject")
        subject:String,

        @Field("message")
        message:String,

        @Field("contact")
        contact:String = "",

        @Field("price")
        price:Int = 0
    ):BaseResponse





    @FormUrlEncoded
    @POST("api/forum/reply.php")
    suspend fun reply(

        @Field("tid")
        tid:String,

        @Field("message")
        message:String

    ):BaseResponse





    @Multipart
    @POST("api/forum/upload.php")
    suspend fun uploadImage(

        @Part file:MultipartBody.Part

    ):UploadResponse





    @GET("api/forum/forum.php")
    suspend fun getForumThreads(

        @Query("fid")
        fid:String

    ):ForumThreadResponse





    @Multipart
    @POST("api/user/avatar.php")
    suspend fun uploadAvatar(

        @Part file:MultipartBody.Part

    ):UploadResponse





    @GET("api/forum/check_permission.php")
    suspend fun checkPermission(

        @Query("fid")
        fid:String,

        @Query("uid")
        uid:Int

    ):okhttp3.ResponseBody





    // 注册账号

    @FormUrlEncoded
    @POST("api/user/register.php")
    suspend fun register(

        @Field("username")
        username:String,

        @Field("password")
        password:String,

        @Field("email")
        email:String,

        @Field("referid")
        referid:String = ""

    ):okhttp3.ResponseBody





    // 忘记密码

    @FormUrlEncoded
    @POST("api/user/reset_password.php")
    suspend fun resetPassword(

        @Field("username")
        username:String,

        @Field("password")
        password:String

    ):BaseResponse





    // 获取所有板块

    @GET("api/forum/forums.php")
    suspend fun getForums():

            ForumListResponse





    // =================================
    // 购买联系方式
    // api/forum/buy_field.php
    // =================================

    @FormUrlEncoded
    @POST("api/forum/buy_field.php")
    suspend fun buyField(

        @Field("token")
        token:String,

        @Field("tid")
        tid:String,

        @Field("optionid")
        optionid:Int

    ):BuyFieldResponse





    @GET("api/app/sync.php")
    suspend fun getContentVersion(
        @Query("scope")
        scope: String,

        @Query("fid")
        fid: String? = null
    ): ContentVersionResponse


    @GET("api/user/threads.php")
    suspend fun getMyThreads(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): MyThreadsResponse

    @GET("api/user/replies.php")
    suspend fun getMyReplies(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): MyRepliesResponse

    @FormUrlEncoded
    @POST("api/forum/edit_post.php")
    suspend fun editPost(
        @Field("tid") tid: String,
        @Field("subject") subject: String,
        @Field("message") message: String
    ): BaseResponse

    @FormUrlEncoded
    @POST("api/forum/delete_post.php")
    suspend fun deletePost(
        @Field("tid") tid: String
    ): BaseResponse

    @FormUrlEncoded
    @POST("api/forum/delete_image.php")
    suspend fun deleteThreadImage(
        @Field("tid") tid: String,
        @Field("image") image: String
    ): BaseResponse


    @GET("api/user/notice.php")
    suspend fun getNotifications(
        @Query("read_id") readId: String? = null
    ): NotificationResponse


    @GET("api/user/message.php")
    suspend fun getPrivateMessages(): PrivateMessageResponse

    @GET("api/user/message_detail.php")
    suspend fun getPrivateMessageDetail(
        @Query("plid") plid: String
    ): PrivateMessageDetailResponse

    @FormUrlEncoded
    @POST("api/user/message_send_v2.php")
    suspend fun sendPrivateMessage(
        @Field("uid") uid: String,
        @Field("message") message: String
    ): okhttp3.ResponseBody


    @FormUrlEncoded
    @POST("api/user/push_token.php")
    suspend fun registerPushToken(
        @Field("token") token: String
    ): BaseResponse

    @GET("api/user/referral_stats.php")
    suspend fun getReferralStats(): ReferralStatsResponse

    @GET("api/user/friends.php")
    suspend fun getFriends(
        @Query("action") action: String = "list"
    ): FriendsResponse

    @FormUrlEncoded
    @POST("api/user/friends.php")
    suspend fun friendAction(
        @Field("action") action: String,
        @Field("uid") uid: String
    ): BaseResponse

}