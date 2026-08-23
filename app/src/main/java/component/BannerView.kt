package com.qjw.forum.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qjw.forum.Banner
import com.qjw.forum.appThumbnailUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerView(
    banners: List<Banner>,
    onClick: (String) -> Unit = {}
) {
    val items = banners.take(5)
    if (items.isEmpty()) return

    // 列表内容更新时，使用安全页数，避免旧页面下标在手势滑动期间越界。
    val bannerKey = items.joinToString("|") { it.tid.orEmpty() + ":" + it.image.orEmpty() }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { items.size.coerceAtLeast(1) })

    LaunchedEffect(bannerKey) {
        if (pagerState.currentPage >= items.size) {
            pagerState.scrollToPage(0)
        }
    }

    LaunchedEffect(bannerKey, items.size) {
        if (items.size <= 1) return@LaunchedEffect
        while (isActive) {
            delay(4000)
            if (!isActive || items.size <= 1) break
            val nextPage = (pagerState.currentPage + 1).mod(items.size)
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) { page ->
            // 数据刷新与用户滑动同时发生时，旧页可能短暂存在；此时跳过即可。\n            val banner = items.getOrNull(page) ?: return@HorizontalPager\n            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { banner.tid?.let(onClick) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = banner.subject ?: "QJWForum",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 3
                        )
                    }

                    if (!banner.image.isNullOrBlank()) {
                        val thumbnail = appThumbnailUrl(banner.image, 540)
                        var loadOriginal by remember(banner.image) { mutableStateOf(false) }
                        Spacer(Modifier.width(12.dp))
                        AsyncImage(
                            model = if (loadOriginal || thumbnail == banner.image) banner.image else thumbnail,
                            contentDescription = banner.subject,
                            contentScale = ContentScale.Crop,
                            onError = {
                                if (!loadOriginal && thumbnail != banner.image) loadOriginal = true
                            },
                            modifier = Modifier
                                .width(125.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(items.size) { index ->
                Text(if (index == pagerState.currentPage) "●" else "○")
            }
        }
    }
}
