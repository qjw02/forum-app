package com.qjw.forum

import com.google.gson.annotations.SerializedName


// ===============================
// 通用返回
// ===============================

data class BaseResponse(

    val code:Int,

    val message:String?,

    val data:PostResult?

)


data class PostResult(

    val tid:Int? = null,

    val pid:Int? = null,

    val fid:Int? = null,

    val plid:String? = null

)

data class RegisterResponse(
    val code: Int,
    val message: String?
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

    val group_name:String? = null,

    @SerializedName("is_vip")
    val isVip:Boolean? = false,

    val credits:Int?,

    val money:Int?,

    val threads:Int?,

    val posts:Int?

)


data class PublicProfileResponse(
    val code: Int,
    val message: String?,
    val data: PublicProfileData?
)

data class PublicProfileData(
    val uid: Int,
    val username: String,
    val avatar: String?,
    val group_name: String? = null,
    val credits: Int? = 0,
    val money: Int? = 0,
    val threads: Int? = 0,
    val replies: Int? = 0,
    val favorites: Int? = 0,
    val regdate: Long? = null
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

    val new:List<Post>?,

    val ads:List<HomeAd>? = emptyList(),

    val announcement:Announcement? = null

)



data class Announcement(
    val subject:String?,
    val message:String?
)

data class HomeAd(
    val id:String? = null,
    val image:String?,
    val link:String? = null
)

data class Banner(

    val tid:String?,

    val subject:String?,

    val image:String? = null

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

    val replies:Int,

    val displayorder:Int? = 0,

    val image:String? = null

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

    val category_info:List<CategoryInfo> = emptyList(),

    // 新增：联系方式收费信息
    val contact:ContactInfo? = null

)



data class CategoryInfo(
    val optionid:Int?,
    val title:String?,
    val value:String?
)

data class ThreadInfo(

    val tid:String,

    val fid:String? = null,

    val forum_name:String? = null,

    val subject:String,

    val content:String,

    @SerializedName("raw_content")
    val rawContent:String? = null,

    val images:List<String>,

    val dateline:Long? = null,

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

    val list:List<Reply>,

    val page:Int? = 1,

    val page_size:Int? = 20

)



data class Reply(

    val pid:String,

    val message:String,

    val dateline:Long? = null,

    val author:Author

)



data class Author(

    val uid:String,

    val username:String,

    val avatar:String?,

    val group_name:String? = null

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
    val allow: Boolean,
    @SerializedName("allow_post")
    val allowPost: Boolean? = false,
    val groupid: Int,
    @SerializedName("group_name")
    val groupName: String? = null,
    val credits: Int
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

// ===============================
// 内容同步版本
// ===============================
data class ContentVersionResponse(
    val code: Int,
    val message: String?,
    val data: ContentVersionData?
)

data class ContentVersionData(
    val version: String
)


// ===============================
// 我的主题和回复
// ===============================
data class MyThreadsResponse(
    val code: Int,
    val message: String?,
    val data: MyThreadsData?
)

data class MyThreadsData(
    val total: Int?,
    val list: List<MyThreadItem>?
)

data class MyThreadItem(
    val tid: String,
    val fid: Int? = null,
    val subject: String,
    val views: Int?,
    val replies: Int?
)

data class MyRepliesResponse(
    val code: Int,
    val message: String?,
    val data: MyRepliesData?
)

data class MyRepliesData(
    val total: Int?,
    val list: List<MyReplyItem>?
)

data class MyReplyItem(
    val pid: String,
    val tid: String,
    val subject: String?,
    val message: String?
)


// ===============================
// 论坛通知
// ===============================
data class NotificationResponse(
    val code: Int,
    val message: String?,
    val data: NotificationData?
)

data class NotificationData(
    val count: Int?,
    val unread: Int?,
    val list: List<NotificationItem>?
)

data class NotificationItem(
    val id: String,
    @SerializedName("new")
    val isNew: Int?,
    val type: String?,
    val author: NotificationAuthor?,
    val note: String?,
    val from_id: String?,
    val from_idtype: String?,
    val tid: String?,
    val pid: String?
)

data class NotificationAuthor(
    val uid: String?,
    val username: String?
)


// ===============================
// 论坛私信
// ===============================
data class PrivateMessageResponse(
    val code: Int,
    val message: String?,
    val data: PrivateMessageData?
)

data class PrivateMessageData(
    val unread: Int?,
    val list: List<PrivateConversation>?
)

data class PrivateConversation(
    val plid: String,
    val subject: String?,
    val message: String?,
    val unread: Int?,
    @SerializedName("other_uid")
    val otherUid: String? = null,
    @SerializedName("other_name")
    val otherName: String? = null,
    val avatar: String? = null,
    val dateline: Long? = null
)

data class PrivateMessageDetailResponse(
    val code: Int,
    val message: String?,
    val data: PrivateMessageDetailData?
)

data class PrivateMessageDetailData(
    val plid: String,
    val list: List<PrivateChatItem>?
)

data class PrivateChatItem(
    val pmid: String,
    val uid: String,
    val username: String?,
    val message: String?,
    val dateline: Long? = null
)


// ===============================
// 好友管理
// ===============================
data class FriendsResponse(
    val code: Int,
    val message: String?,
    val data: FriendsData?
)

data class FriendsData(
    val friends: List<FriendItem>?,
    val requests: List<FriendItem>?
)

data class FriendItem(
    val fuid: String,
    val username: String?,
    val group_name: String?
)


// ===============================
// 推广统计
// ===============================
data class ReferralStatsResponse(
    val code: Int,
    val message: String?,
    val data: ReferralStatsData?
)

data class ReferralStatsData(
    val visit_count: Int?
)


// ===============================
// 主题搜索
// ===============================
data class ForumSearchResponse(
    val code: Int,
    val message: String?,
    val data: List<Post>?
)
