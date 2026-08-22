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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qjw.forum.Banner
import com.qjw.forum.appThumbnailUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerView(
    banners: List<Banner>,
    onClick: (String) -> Unit = {}
) {
    val items = banners.take(5)
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { items.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(items.size) {
        if (items.size <= 1) return@LaunchedEffect
        while (true) {
            delay(4000)
            scope.launch {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % items.size)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) { page ->
            val banner = items[page]
            Card(
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
                        Spacer(Modifier.width(12.dp))
                        AsyncImage(
                            model = appThumbnailUrl(banner.image, 540) ?: banner.image,
                            contentDescription = banner.subject,
                            contentScale = ContentScale.Crop,
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
