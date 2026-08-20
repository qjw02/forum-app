package com.qjw.forum.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qjw.forum.Post

@Composable
fun PostCard(
    post: Post,
    onClick: (String) -> Unit,
    showPinnedLabel: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clickable { onClick(post.tid) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (showPinnedLabel && (post.displayorder ?: 0) > 0) {
                        "📌 置顶  " + post.subject
                    } else {
                        post.subject
                    },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "作者: " + post.author,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "浏览 " + post.views + "   回复 " + post.replies,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!post.image.isNullOrBlank()) {
                Spacer(Modifier.width(12.dp))
                AsyncImage(
                    model = post.image,
                    contentDescription = post.subject,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(width = 82.dp, height = 62.dp)
                )
            }
        }
    }
}
