package com.exa.android.reflekt.loopit.presentation.test

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ThumbUpAlt
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.exa.android.reflekt.R

@Composable
fun HomeScreen(posts: List<Post>, onPostClick: (Post) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar()
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(posts) { post ->
                PostCard(post = post, onPostClick = onPostClick)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Campus Connect",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.background(
                brush = Brush.horizontalGradient(listOf(Color(0xFF6A11CB), Color(0xFF2575FC)))
            ),
            color = Color.Transparent
        )
        IconButton(onClick = { /* Notification */ }) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notification"
            )
        }
    }
}

@Composable
fun PostCard(post: Post, onPostClick: (Post) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick(post) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = post.avatarRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(post.author, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${post.time}h • ", fontSize = 12.sp, color = Color.Gray)
                        Text("${post.views} views", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                PostTypeTag(post.type)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.description,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (post.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    post.images.take(2).forEach { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .height(100.dp)
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            post.link?.let {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { /* Open Link */ }) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = Color.Blue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = it, color = Color.Blue, fontSize = 13.sp)
                }
            }

            post.pollOptions?.let { options ->
                Spacer(modifier = Modifier.height(8.dp))
                Text("Quick Poll", fontWeight = FontWeight.SemiBold)
                options.forEach { (label, percent) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(label, modifier = Modifier.weight(1f))
                        LinearProgressIndicator(progress = percent / 100f, modifier = Modifier.weight(2f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${percent}%")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                post.tags.forEach { tag ->
                    AssistChip(
                        onClick = {},
                        label = { Text(tag) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconWithText(icon = Icons.Default.ThumbUpAlt, text = post.upvotes.toString())
                IconWithText(icon = Icons.Default.HeartBroken, text = post.likes.toString())
                IconWithText(icon = Icons.Outlined.ModeComment, text = post.comments.toString())
            }
        }
    }
}

@Composable
fun PostTypeTag(type: PostType) {
    val (text, color) = when (type) {
        PostType.Debate -> "Debate" to Color.Red
        PostType.Poll -> "Poll" to Color(0xFF4CAF50)
        PostType.Event -> "Event" to Color.Blue
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun IconWithText(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp)
    }
}

@Preview
@Composable
private fun pre() {


    val samplePosts = listOf(
        Post(
            avatarRes = R.drawable.placeholder,
            author = "John Doe",
            time = 2,
            views = 120,
            type = PostType.Debate,
            title = "Should AI be regulated in universities?",
            description = "A heated discussion on the limits of artificial intelligence in academic research...",
            images = listOf("https://picsum.photos/200/300", "https://picsum.photos/201/300"),
            link = null,
            pollOptions = null,
            tags = listOf("AI", "Education"),
            upvotes = 32,
            likes = 12,
            comments = 8
        ),
        Post(
            avatarRes = R.drawable.placeholder,
            author = "Alice Smith",
            time = 1,
            views = 90,
            type = PostType.Poll,
            title = "Best Programming Language in 2025?",
            description = "Vote for your favorite language dominating the industry in 2025!",
            images = emptyList(),
            link = null,
            pollOptions = listOf(
                "Kotlin" to 45,
                "Python" to 30,
                "Rust" to 25
            ),
            tags = listOf("Poll", "Programming"),
            upvotes = 50,
            likes = 20,
            comments = 15
        ),
        Post(
            avatarRes = R.drawable.placeholder,
            author = "Event Team",
            time = 5,
            views = 300,
            type = PostType.Event,
            title = "Campus Tech Fest 2025",
            description = "Join us for workshops, coding battles, and speaker sessions.",
            images = listOf("https://picsum.photos/202/300"),
            link = "www.techfest2025.com",
            pollOptions = null,
            tags = listOf("Event", "TechFest"),
            upvotes = 70,
            likes = 40,
            comments = 18
        )
    )
    HomeScreen(posts = samplePosts) {

    }
}
data class Post(
    @DrawableRes val avatarRes: Int,
    val author: String,
    val time: Int, // in hours
    val views: Int,
    val type: PostType,
    val title: String,
    val description: String,
    val images: List<String> = emptyList(),
    val link: String? = null,
    val pollOptions: List<Pair<String, Int>>? = null,
    val tags: List<String> = emptyList(),
    val upvotes: Int = 0,
    val likes: Int = 0,
    val comments: Int = 0
)

enum class PostType {
    Debate,
    Poll,
    Event
}

