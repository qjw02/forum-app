package com.qjw.forum


import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.qjw.forum.component.ImageViewer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun formatForumTime(timestamp: Long?): String {
    if (timestamp == null || timestamp <= 0) return ""
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .format(Date(timestamp * 1000))
}

fun cleanDiscuzText(text:String):String{

    return text

        .replace(
            Regex("<style.*?</style>",
                RegexOption.DOT_MATCHES_ALL),
            ""
        )

        .replace(
            Regex("<script.*?</script>",
                RegexOption.DOT_MATCHES_ALL),
            ""
        )

        .replace(
            Regex("<img.*?>",
                RegexOption.DOT_MATCHES_ALL),
            ""
        )

        .replace(
            Regex("<a.*?</a>",
                RegexOption.DOT_MATCHES_ALL),
            ""
        )

        .replace(
            Regex("<div.*?>",
                RegexOption.DOT_MATCHES_ALL),
            ""
        )

        .replace("</div>","")

        // 清理 Discuz 编辑器遗留的 font、span 等 HTML 标签
        .replace(
            Regex("<[^>]+>"),
            ""
        )

        .replace("<br />","\n")
        .replace("<br/>","\n")
        .replace("<br>","\n")

        .replace("&nbsp;"," ")

        .replace(
            Regex("\\[attach\\].*?\\[/attach\\]"),
            ""
        )

        .replace(
            Regex("\\[img\\].*?\\[/img\\]"),
            ""
        )

        .trim()

}




@Composable
fun ThreadDetail(

    tid:String,

    focusPid:String? = null,

    onBack:(String)->Unit,

    onLogin:()->Unit,

    onOpenUser: (String) -> Unit

){


    android.util.Log.e(
        "THREAD_DEBUG",
        "ThreadDetail进入 tid=$tid"
    )



    val cachedThread = remember(tid) { ContentCache.getThread(tid) }

    var data by remember(tid) {

        mutableStateOf(cachedThread)

    }



    var loading by remember(tid) {

        mutableStateOf(cachedThread == null)

    }

    var refreshing by remember(tid) {
        mutableStateOf(false)
    }



    var replyPage by remember { mutableStateOf(1) }

    var loadingMore by remember { mutableStateOf(false) }

    var replyText by remember {

        mutableStateOf("")

    }



    var replyMsg by remember {

        mutableStateOf("")

    }

    var deleteReplyPid by remember { mutableStateOf<String?>(null) }
    var deletingReply by remember { mutableStateOf(false) }



    val scope =
        rememberCoroutineScope()

    val replyListState =
        rememberLazyListState()





    fun loadThread(manual: Boolean = false){


        scope.launch{

            if (manual) {
                refreshing = true
                replyMsg = ""
            }

            try{


                android.util.Log.e(
                    "THREAD_DEBUG",
                    "开始请求帖子 tid=$tid"
                )



                val result =

                    ApiClient.api.getThread(
                        tid = tid,
                        pid = focusPid
                    )




                android.util.Log.e(
                    "THREAD_DEBUG",
                    result.toString()
                )




                if(result.code==0){

                    data =
                        result.data
                    result.data?.let { ContentCache.saveThread(tid, it) }
                    replyPage = result.data?.replies?.page ?: 1

                }else{

                    replyMsg =
                        result.message ?: "加载失败"

                }



            }catch(e:Exception){


                android.util.Log.e(
                    "THREAD_ERROR",
                    e.stackTraceToString()
                )


                replyMsg =
                    e.message ?: "加载失败"


            }



            loading=false
            refreshing=false



        }


    }





    fun loadMoreReplies(){
        val current = data ?: return
        if(loadingMore || current.replies.list.size >= current.replies.total) return

        scope.launch{
            loadingMore = true
            try{
                val nextPage = (current.replies.page ?: replyPage) + 1
                val result = ApiClient.api.getThread(tid = tid, page = nextPage)
                if(result.code == 0 && result.data != null){
                    val next = result.data
                    data = next.copy(
                        replies = next.replies.copy(
                            list = current.replies.list + next.replies.list
                        )
                    )
                    replyPage = next.replies.page ?: nextPage
                }else{
                    replyMsg = result.message ?: "加载更多回复失败"
                }
            }catch(e:Exception){
                replyMsg = e.message ?: "加载更多回复失败"
            }
            loadingMore = false
        }
    }

    LaunchedEffect(tid){

        loadThread()

    }

    LaunchedEffect(data, focusPid){

        val replyIndex =
            data?.replies?.list
                ?.indexOfFirst { it.pid == focusPid }
                ?: -1

        if(replyIndex >= 0){

            replyListState.scrollToItem(replyIndex + 1)

        }

    }




    Box(

        modifier =
            Modifier.fillMaxSize()

    ){



        Column(

            modifier =
                Modifier.fillMaxSize()

        ){



            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        onBack(data?.thread?.fid ?: "")
                    }
                ) {
                    Text("返回")
                }

                Button(
                    enabled = !refreshing,
                    onClick = { loadThread(manual = true) }
                ) {
                    Text(if (refreshing) "刷新中…" else "刷新")
                }
            }







            if(loading){


                Box(

                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center

                ){

                    Text("加载中...")

                }


            }else{





                data?.let { threadData ->




                    LazyColumn(

                        state = replyListState,

                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp)
                                .widthIn(max = 600.dp)

                    ){



                        item{



                            Text(

                                text =
                                    threadData.thread.subject,

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleLarge

                            )



                            Spacer(
                                Modifier.height(10.dp)
                            )



                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Row(
                                    modifier = Modifier.clickable {
                                        onOpenUser(threadData.thread.author.uid)
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = threadData.thread.author.avatar,
                                        contentDescription = threadData.thread.author.username + "的头像",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("作者：" + threadData.thread.author.username + " · ")
                                    Text(
                                        text = threadData.thread.author.group_name ?: "普通会员",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Text(
                                    text = formatForumTime(threadData.thread.dateline),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(
                                Modifier.height(10.dp)
                            )

                            if (threadData.category_info.isNotEmpty()) {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "分类信息",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        threadData.category_info.forEach { field ->
                                            val title = field.title?.takeIf { it.isNotBlank() } ?: "信息"
                                            val value = cleanDiscuzText(field.value ?: "")
                                            if (value.isNotBlank()) {
                                                Text("$title：$value")
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(14.dp))
                            }

                            Text(
                                text = cleanDiscuzText(threadData.thread.content),
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 19.sp,
                                lineHeight = 30.sp
                            )



                            Spacer(
                                Modifier.height(15.dp)
                            )




                            threadData.thread.images.forEach { img ->


                                Box(

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)

                                ){

                                    ImageViewer(

                                        url = img

                                    )

                                }


                                Spacer(
                                    Modifier.height(10.dp)
                                )


                            }





                            Spacer(
                                Modifier.height(20.dp)
                            )





    
                        // ===============================
                        // 联系方式购买
                        // ===============================

                        if (threadData.thread.fid.toString() == "2") {
                        threadData.contact?.let { contact ->

                            val highlightContact = threadData.thread.forum_name == "高级报告"

                            Spacer(
                                Modifier.height(15.dp)
                            )

                            Text(
                                text = "联系方式",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (highlightContact) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (highlightContact) FontWeight.Bold else FontWeight.Normal
                            )

                            Spacer(
                                Modifier.height(8.dp)
                            )

                            if(contact.locked){

                                Text(
                                    text = "🔒 联系方式已隐藏",
                                    color = if (highlightContact) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (highlightContact) FontWeight.Bold else FontWeight.Normal
                                )

                                Text(
                                    text = "需要 ${contact.price ?: 0} C币查看",
                                    color = if (highlightContact) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (highlightContact) FontWeight.Bold else FontWeight.Normal
                                )

                                Spacer(
                                    Modifier.height(8.dp)
                                )

                                Button(
                                    onClick = {

                                        if(!UserStore.isLogin()){

                                            onLogin()

                                            return@Button

                                        }

                                        scope.launch {

                                            try {

                                                val result =
                                                    ApiClient.api.buyField(
                                                        UserStore.getToken(),
                                                        tid,
                                                        contact.optionid ?: 7
                                                    )

                                                replyMsg =
                                                    result.message ?: ""

                                                if(result.code == 0){

                                                    ProfileCache.clear()
                                                    loadThread()

                                                }

                                            }catch(e:Exception){

                                                replyMsg =
                                                    e.message ?: "购买失败"

                                            }

                                        }

                                    }

                                ){

                                    Text("购买查看")

                                }


                            }else{

                                Text(
                                    text = cleanDiscuzText(contact.value ?: ""),
                                    color = if (highlightContact) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (highlightContact) FontWeight.Bold else FontWeight.Normal
                                )

                            }

                            Spacer(
                                Modifier.height(15.dp)
                            )

                        }
                        }

                        Text(

                                text =
                                    "回复(${threadData.replies.total})",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium

                            )





                            Spacer(
                                Modifier.height(10.dp)
                            )





                            OutlinedTextField(

                                value =
                                    replyText,

                                onValueChange = {

                                    replyText = it

                                },

                                label = {

                                    Text("输入回复")

                                },

                                modifier =
                                    Modifier.fillMaxWidth()

                            )






                            Spacer(
                                Modifier.height(10.dp)
                            )





                            Button(

                                onClick = {


                                    if(!UserStore.isLogin()){

                                        onLogin()

                                        return@Button

                                    }



                                    scope.launch{


                                        try{


                                            val result =

                                                ApiClient.api.reply(

                                                    tid,

                                                    replyText

                                                )



                                            replyMsg =

                                                result.message
                                                    ?: ""



                                            if(result.code==0){


                                                ProfileCache.clear()
                                                replyText=""


                                                PostCache.clear()


                                                val refresh =

                                                    ApiClient.api.getThread(
                                                        tid
                                                    )



                                                if(refresh.code==0){

                                                    data =
                                                        refresh.data
                                                    refresh.data?.let { ContentCache.saveThread(tid, it) }

                                                }


                                            }



                                        }catch(e:Exception){


                                            replyMsg =
                                                e.message
                                                    ?: "回复失败"

                                        }


                                    }



                                }

                            ){

                                Text("发送回复")

                            }





                            Spacer(
                                Modifier.height(10.dp)
                            )



                            Text(replyMsg)



                        }







                        items(
                            items = threadData.replies.list,
                            key = { it.pid }
                        ){ reply ->




                            Card(

                                colors =
                                    if(reply.pid == focusPid){
                                        CardDefaults.cardColors(
                                            containerColor =
                                                MaterialTheme.colorScheme.primaryContainer
                                        )
                                    }else{
                                        CardDefaults.cardColors()
                                    },

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)

                            ){


                                Column(

                                    modifier =
                                        Modifier.padding(10.dp)

                                ){


                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Row(
                                            modifier = Modifier.clickable {
                                                onOpenUser(reply.author.uid)
                                            },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = reply.author.avatar,
                                                contentDescription = reply.author.username + "的头像",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(reply.author.username + " · ")
                                            Text(
                                                text = reply.author.group_name ?: "普通会员",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        Text(
                                            text = formatForumTime(reply.dateline),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(

                                        cleanDiscuzText(
                                            reply.message
                                        )

                                    )

                                    if (reply.author.uid.toString() == UserStore.getUid().toString()) {
                                        TextButton(
                                            onClick = { deleteReplyPid = reply.pid.toString() }
                                        ) {
                                            Text(
                                                text = "删除回复",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }


                                }


                            }



                        }

                        item{
                            if(threadData.replies.list.size < threadData.replies.total){
                                Button(
                                    onClick = { loadMoreReplies() },
                                    enabled = !loadingMore,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ){
                                    Text(
                                        if(loadingMore) "加载中..."
                                        else "加载更多回复（已显示 " +
                                            threadData.replies.list.size + "/" +
                                            threadData.replies.total + "）"
                                    )
                                }
                            }
                        }



                    }



                }



            }



        }

        deleteReplyPid?.let { pid ->
            AlertDialog(
                onDismissRequest = { if (!deletingReply) deleteReplyPid = null },
                title = { Text("删除回复？") },
                text = { Text("删除后不能恢复。") },
                confirmButton = {
                    Button(
                        enabled = !deletingReply,
                        onClick = {
                            scope.launch {
                                deletingReply = true
                                try {
                                    val result = ApiClient.api.deleteReply(tid, pid)
                                    replyMsg = result.message ?: if (result.code == 0) "回复已删除" else "删除失败"
                                    if (result.code == 0) {
                                        loadThread(manual = true)
                                        deleteReplyPid = null
                                    }
                                } catch (e: Exception) {
                                    replyMsg = e.message ?: "删除回复失败"
                                } finally {
                                    deletingReply = false
                                }
                            }
                        }
                    ) {
                        Text(if (deletingReply) "删除中..." else "确认删除")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        enabled = !deletingReply,
                        onClick = { deleteReplyPid = null }
                    ) {
                        Text("取消")
                    }
                }
            )
        }

    }



}