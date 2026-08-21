package com.qjw.forum.navigation


import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.qjw.forum.*
import com.qjw.forum.component.BottomBar
import kotlinx.coroutines.delay



@Composable
fun AppNav(){


    var page by remember {

        mutableStateOf("home")

    }

    val unreadCount = UnreadStore.totalCount

    LaunchedEffect(UserStore.getToken()) {
        while (true) {
            UnreadStore.refresh()
            delay(30_000)
        }
    }



    Scaffold(


        bottomBar = {


            BottomBar(


                current =
                    page.substringBefore("/"),

                unreadCount = unreadCount,



                onChange = { target ->



                    when(target){



                        "profile" -> {



                            if(UserStore.isLogin()){


                                page="profile"


                            }else{


                                page="login/profile"


                            }



                        }




                        else -> {


                            page = target


                        }



                    }



                }



            )


        }



    ){ padding ->




        Box(

            modifier = Modifier

                .padding(padding)

        ){



            when{





                // 首页

                page=="home" -> {



                    HomeScreen(



                        onOpenThread = { tid ->



                            Log.e(

                                "THREAD_DEBUG",

                                "首页点击帖子 tid=$tid"

                            )


                            page="thread/$tid"



                        },



                        onOpenForum = { fid ->


                            page="forumThread/$fid"



                        }



                    )



                }








                // 板块

                page=="forum" -> {



                    ForumListScreen(



                        onOpenForum = { fid ->



                            page="forumThread/$fid"



                        }



                    )



                }








                // 消息

                page=="message" -> {



                    MessageScreen(
                        onOpenThread = { tid, pid ->
                            page = if (pid.isNullOrBlank()) {
                                "thread/$tid?back=message"
                            } else {
                                "thread/$tid?back=message&pid=$pid"
                            }
                        },
                        onOpenChat = { plid -> page="chat/$plid" }
                    )



                }









                // 私信聊天
                page.startsWith("chat/") -> {

                    val plid = page.removePrefix("chat/")

                    PrivateChatScreen(
                        plid = plid,
                        onBack = { page="message" }
                    )

                }

                // 推广中心

                page=="referral" -> {

                    ReferralScreen()

                }



                // 我的

                page=="profile" -> {



                    ProfileScreen(



                        onLogout = {



                            UserStore.clear()


                            page="home"



                        },


                        onReferral = {

                            page="referral"

                        },

                        onMyThreads = {

                            page="myThreads"

                        },

                        onMyReplies = {

                            page="myReplies"

                        },

                        onFriends = {

                            page="friends"

                        }



                    )



                }










                // 好友管理
                page=="friends" -> {

                    FriendsScreen(
                        onBack = { page="profile" }
                    )

                }

                // 我的主题
                page=="myThreads" -> {

                    MyContentScreen(
                        showReplies = false,
                        onBack = { page="profile" },
                        onOpenThread = { tid, pid ->
                            page = if (pid.isNullOrBlank()) {
                                "thread/$tid?back=myThreads"
                            } else {
                                "thread/$tid?back=myThreads&pid=$pid"
                            }
                        }
                    )

                }

                // 我的回复
                page=="myReplies" -> {

                    MyContentScreen(
                        showReplies = true,
                        onBack = { page="profile" },
                        onOpenThread = { tid, pid ->
                            page = if (pid.isNullOrBlank()) {
                                "thread/$tid?back=myReplies"
                            } else {
                                "thread/$tid?back=myReplies&pid=$pid"
                            }
                        }
                    )

                }

                // 登录我的

                page=="login/profile" -> {

                    LoginScreen(

                        onLoginSuccess = {

                            page="profile"

                        },

                        onRegister = {

                            page="register"

                        })

                }











                // 注册

                page=="register" -> {


                    RegisterScreen(

                        onSuccess = {

                            page="login/profile"

                        },

                        onBack = {

                            page="login/profile"

                        }

                    )


                }






                // 忘记密码

                page=="forgot" -> {


                    ForgotPasswordScreen(

                        onSuccess = {

                            page="login/profile"

                        },

                        onBack = {

                            page="login/profile"

                        }

                    )


                }






                // 我的进入发布

                page=="create" -> {



                    CreatePost(



                        fid = null,



                        onOpenThread = { tid ->



                            page="thread/$tid"



                        }



                    )



                }










                // 板块进入发布 create/fid

                page.startsWith("create/") -> {



                    val fid =

                        page.removePrefix(
                            "create/"
                        )




                    CreatePost(



                        fid=fid,



                        onOpenThread = { tid ->



                            page="thread/$tid"



                        }



                    )



                }










                // 板块帖子列表

                page.startsWith("forumThread/") -> {



                    val fid =

                        page.removePrefix(
                            "forumThread/"
                        )




                    ForumThreadListScreen(



                        fid=fid,



                        onCreatePost = { postFid ->



                            page="create/$postFid"



                        },



                        onOpenThread = { tid ->



                            Log.e(

                                "THREAD_DEBUG",

                                "板块点击帖子 tid=$tid"

                            )



                            page="thread/$tid"



                        },



                        onBack = {



                            page="forum"



                        }



                    )



                }









                // 帖子详情

                page.startsWith("thread/") -> {



                    val threadRoute =

                        page.removePrefix(
                            "thread/"
                        )

                    val tid =

                        threadRoute.substringBefore("?")

                    val focusPid =

                        threadRoute
                            .substringAfter("pid=", "")
                            .substringBefore("&")
                            .takeIf { it.isNotBlank() }



                    Log.e(

                        "THREAD_DEBUG",

                        "进入帖子详情 tid=$tid"

                    )





                    ThreadDetail(



                        tid=tid,

                        focusPid=focusPid,



                        onBack = { forumFid ->

                            page = if (forumFid.isNotBlank()) {
                                "forumThread/$forumFid"
                            } else {
                                "forum"
                            }

                        },



                        onLogin = {



                            page="login/thread"



                        }



                    )



                }










                // 回复登录

                page=="login/thread" -> {

                    LoginScreen(

                        onLoginSuccess = {

                            page="home"

                        },

                        onRegister = {

                            page="register"

                        })

                }



            }



        }



    }



}