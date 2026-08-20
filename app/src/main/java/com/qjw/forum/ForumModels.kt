package com.qjw.forum


// ===============================
// 通用返回
// ===============================

data class BaseResponse(

    val code:Int,

    val message:String?,

    val data:PostResult?

)


data class PostResult(

    val tid:Int?,

    val pid:Int?,

    val fid:Int?

)


// ===============================
// 登录
// ===============================

data class LoginResponse(

    val code:Int,

    val message:String?,

    val data:LoginData?

)


data class LoginData(

    val uid:Int,

    val username:String,

    val token:String

)



// ===============================
// 用户资料
// ===============================

data class ProfileResponse(

    val code:Int,

    val message:String?,

    val data:ProfileData?

)



data class ProfileData(

    val uid:Int,

    val username:String,

    val avatar:String?,

    val groupid:Int?,

    val credits:Int?,

    val money:Int?,

    val threads:Int?,

    val posts:Int?

)



// ===============================
// 首页
// ===============================

data class HomeResponse(

    val code:Int,

    val message:String?,

    val data:HomeData?

)


data class HomeData(

    val banner:List<Banner>?,

    val forums:List<HomeForum>?,

    val hot:List<Post>?,

    val new:List<Post>?

)



data class Banner(

    val tid:String?,

    val subject:String?

)



data class HomeForum(

    val fid:String,

    val name:String

)



// ===============================
// 帖子列表
// ===============================

data class Post(

    val tid:String,

    val subject:String,

    val author:String,

    val views:Int,

    val replies:Int

)



// ===============================
// 帖子详情
// ===============================

data class ThreadResponse(

    val code:Int,

    val message:String?,

    val data:ThreadData?

)



data class ThreadData(

    val thread:ThreadInfo,

    val replies:ReplyData,

    // 新增：联系方式收费信息
    val contact:ContactInfo? = null

)



data class ThreadInfo(

    val tid:String,

    val subject:String,

    val content:String,

    val images:List<String>,

    val author:Author

)



// ===============================
// 联系方式购买
// ===============================

data class ContactInfo(

    // true=隐藏
    val locked:Boolean,

    // 分类信息ID
    val optionid:Int?,

    // C币价格
    val price:Int?,

    // 已购买后的联系方式
    val value:String?

)



data class ReplyData(

    val total:Int,

    val list:List<Reply>

)



data class Reply(

    val pid:String,

    val message:String,

    val author:Author

)



data class Author(

    val uid:String,

    val username:String,

    val avatar:String?

)





// ===============================
// 上传图片
// ===============================

data class UploadResponse(

    val code:Int,

    val message:String?,

    val data:UploadData?

)



data class UploadData(

    val attachment:String?

)





// ===============================
// 板块帖子
// ===============================

data class ForumThreadResponse(

    val code:Int,

    val message:String?,

    val data:ForumThreadData?

)



data class ForumThreadData(

    val fid:String,

    val name:String,

    val list:List<Post>

)





// ===============================
// 权限
// ===============================

data class PermissionResponse(

    val code:Int,

    val message:String?,

    val data:PermissionData?

)



data class PermissionData(

    val allow:Boolean,

    val groupid:Int,

    val credits:Int

)





// ===============================
// 板块列表
// ===============================

data class ForumItem(

    val fid:Int,

    val name:String

)



data class ForumListResponse(

    val code:Int,

    val message:String?,

    val data:List<ForumItem>?

)