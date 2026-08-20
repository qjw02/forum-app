package com.qjw.forum

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qjw.forum.component.BannerView
import com.qjw.forum.component.NoticeCard
import com.qjw.forum.component.PostCard
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenThread: (String) -> Unit,
    onOpenForum: (String) -> Unit
) {
    val cachedHome = remember { ContentCache.getHome() }
    var homeData by remember { mutableStateOf(cachedHome?.data) }
    var loading by remember { mutableStateOf(cachedHome == null) }
    var errorText by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var keyword by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Post>>(emptyList()) }
    val scope = rememberCoroutineScope()

    fun fetchHome(version: String?) {
        scope.launch {
            try {
                val result = ApiClient.api.getHomeIndex()
                if (result.code == 0 && result.data != null) {
                    homeData = result.data
                    PostCache.save(result.data.new.orEmpty())
                    version?.let { ContentCache.saveHome(it, result.data) }
                } else {
                    errorText = result.message ?: "加载失败"
                }
            } catch (e: Exception) {
                errorText = e.message ?: "网络错误"
            } finally {
                loading = false
            }
        }
    }

    fun search() {
        if (keyword.trim().isBlank()) {
            searchMessage = "请输入主题标题关键词"
            return
        }
        scope.launch {
            searching = true
            searchMessage = ""
            try {
                val result = ApiClient.api.searchThreads(keyword.trim())
                if (result.code == 0) {
                    searchResults = result.data.orEmpty()
                    if (searchResults.isEmpty()) searchMessage = "没有找到相关主题"
                } else {
                    searchMessage = result.message ?: "搜索失败"
                }
            } catch (e: Exception) {
                searchMessage = e.message ?: "搜索失败"
            } finally {
                searching = false
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val sync = ApiClient.api.getContentVersion("home")
                val version = sync.data?.version
                when {
                    sync.code != 0 || version.isNullOrBlank() -> {
                        if (cachedHome == null) fetchHome(null) else loading = false
                    }
                    cachedHome?.version == version -> loading = false
                    else -> fetchHome(version)
                }
            } catch (_: Exception) {
                if (cachedHome == null) fetchHome(null) else loading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            homeData != null -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("QJWForum", style = MaterialTheme.typography.titleLarge)
                        Button(onClick = {
                            showSearch = true
                            searchMessage = ""
                        }) { Text("🔍 搜索") }
                    }
                }

                item {
                    NoticeCard(
                        title = homeData?.announcement?.subject ?: "公告",
                        text = homeData?.announcement?.message
                            ?: "欢迎来到 QJWForum，请遵守社区规则，文明交流"
                    )
                }

                item {
                    BannerView(
                        banners = homeData?.banner.orEmpty(),
                        onClick = onOpenThread
                    )
                }

                item {
                    Text(
                        text = "热门帖子",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                    )
                }

                items(homeData?.hot.orEmpty(), key = { it.tid }) { post ->
                    PostCard(post = post, onClick = onOpenThread)
                }

                item {
                    Text(
                        text = "最新帖子",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
                    )
                }

                items(homeData?.new.orEmpty(), key = { it.tid }) { post ->
                    PostCard(post = post, onClick = onOpenThread)
                }
            }

            else -> Text(
                text = "暂无内容",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (errorText.isNotEmpty()) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) { Text(errorText) }
        }
    }

    if (showSearch) {
        AlertDialog(
            onDismissRequest = { showSearch = false },
            title = { Text("搜索主题") },
            text = {
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = keyword,
                            onValueChange = { keyword = it },
                            label = { Text("输入主题标题关键词") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (searching) {
                            Spacer(Modifier.padding(top = 12.dp))
                            CircularProgressIndicator()
                        }
                        if (searchMessage.isNotBlank()) {
                            Text(
                                searchMessage,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }
                    items(searchResults, key = { it.tid }) { post ->
                        Text(
                            text = post.subject,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showSearch = false
                                    onOpenThread(post.tid)
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(enabled = !searching, onClick = { search() }) {
                    Text("搜索")
                }
            },
            dismissButton = {
                Button(onClick = { showSearch = false }) { Text("关闭") }
            }
        )
    }
}
