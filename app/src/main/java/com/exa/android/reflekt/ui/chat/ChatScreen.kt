package com.exa.android.reflekt.ui.chat

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.model.ChatUser
import com.exa.android.reflekt.model.Message
import com.exa.android.reflekt.ui.theme.AppColors
import com.exa.android.reflekt.ui.theme.ReflektTheme
import com.exa.android.reflekt.ui.theme.TextFieldShape
import com.exa.android.reflekt.ui.theme.textFieldColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.regex.Pattern


@Composable
fun ChatScreen(
    user: ChatUser,
    onBackPressed: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf(TextFieldValue()) }
    val messages = remember { mutableStateListOf(*sampleMessages) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ChatHeader(user, onBackPressed)

        MessagesList(messages = messages, modifier = Modifier.weight(1f))

        NewMessageSection(
            messageText = messageText,
            onMessageChange = { messageText = it },
            onSend = {
                if (messageText.text.isNotBlank()) {
                    messages.add(Message(
                        id = (messages.size + 1).toString(),
                        text = messageText.text,
                        senderId = "currentUser",
                        timestamp = "Now",
                        isSent = true
                    ))
                    onSendMessage(messageText.text)
                    messageText = TextFieldValue()
                }
            }
        )
    }
}

@Composable
private fun ChatHeader(user: ChatUser, onBackPressed: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            UserAvatar(user.imageUrl, user.isOnline)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (user.isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.CoralAccent
                )
            }

            IconButton(onClick = { /* Handle video call */ }) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Video Call",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MessagesList(messages: List<Message>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp),
        state = listState,
        reverseLayout = true
    ) {
        items(messages.reversed()) { message ->
            val isLink = LinkUtils.isValidUrl(message.text)
            MessageBubblee(message = message, isLink)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
//
//@Composable
//private fun MessageBubble(message: Message) {
//    val isSentByMe = message.isSent
//
//    val hasLink = containsLink(message.text)
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp),
//        contentAlignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
//    ) {
//        Surface(
//            shape = RoundedCornerShape(16.dp),
//            color = if (isSentByMe) AppColors.CoralAccent else MaterialTheme.colorScheme.surface,
//            shadowElevation = 1.dp
//        ) {
//            Column(
//                modifier = Modifier.padding(12.dp)
//            ) {
//                Text(
//                    text = message.text,
//                    style = MaterialTheme.typography.bodyLarge.copy(
//                        color = if (isSentByMe) Color.White else MaterialTheme.colorScheme.onSurface
//                    )
//                )
//                Text(
//                    text = message.timestamp,
//                    style = MaterialTheme.typography.labelSmall.copy(
//                        color = if (isSentByMe) Color.White.copy(alpha = 0.8f)
//                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                    ),
//                    modifier = Modifier.align(Alignment.End)
//                )
//            }
//        }
//    }
//}



// Add LinkMetadata class
data class LinkMetadata(
    val url: String,
    val title: String?,
    val description: String?,
    val imageUrl: String?
)

// Add these composables
@Composable
private fun MessageBubble(message: Message) {
    val isSentByMe = message.isSent
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    // State for storing link metadata
    var linkMetadata by remember { mutableStateOf<LinkMetadata?>(null) }

    // Detect URLs in message
    val annotatedString = buildAnnotatedString {
        append(message.text)

        LinkUtils.findUrls(message.text).forEach { urlSpan ->
            addStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                ),
                start = urlSpan.start,
                end = urlSpan.end
            )
            addStringAnnotation(
                tag = "URL",
                annotation = urlSpan.url,
                start = urlSpan.start,
                end = urlSpan.end
            )
        }
    }

    // Fetch metadata when message appears
    LaunchedEffect(message.id) {
        val urls = LinkUtils.findUrls(message.text)
        if (urls.isNotEmpty()) {
            coroutineScope.launch {
                linkMetadata = fetchLinkMetadata(urls.first().url)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSentByMe) AppColors.CoralAccent else MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    //Text(
//                    text = message.text,
//                    style = MaterialTheme.typography.bodyLarge.copy(
//                        color = if (isSentByMe) Color.White else MaterialTheme.colorScheme.onSurface
//                    )
//                )
                    ClickableText(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = if (isSentByMe) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations("URL", offset, offset)
                                .firstOrNull()
                                ?.let { annotation ->
                                    uriHandler.openUri(annotation.item)
                                }
                        }
                    )

                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSentByMe) Color.White.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Show link preview if metadata exists
//            linkMetadata?.let { metadata ->
//                LinkPreviewCard(
//                    metadata = metadata,
//                    modifier = Modifier.padding(top = 4.dp)
//                )
//            }
        }
    }
}

@Composable
private fun LinkPreviewCard(metadata: LinkMetadata, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = URL(metadata.url).host ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            metadata.title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
            metadata.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3
                )
            }
        }
    }
}

// Add to LinkUtils
object LinkUtils {
    // Existing containsLink function
    fun containsLink(text: String): Boolean {
        return findUrls(text).isNotEmpty()
    }

    // New URL detection function
    fun findUrls(text: String): List<LinkSpan> {
        val pattern = Pattern.compile("(https?://\\S+)")
        val matcher = pattern.matcher(text)
        val matches = mutableListOf<LinkSpan>()
        while (matcher.find()) {
            matches.add(LinkSpan(
                url = matcher.group(),
                start = matcher.start(),
                end = matcher.end()
            ))
        }
        return matches
    }

    fun isValidUrl(text: String): Boolean {
        return try {
            val url = java.net.URL(text)
            url.toURI() // Ensures it's a valid URI
            true
        } catch (e: Exception) {
            false
        }
    }


    data class LinkSpan(val url: String, val start: Int, val end: Int)
}

// Mock metadata fetch function
private suspend fun fetchLinkMetadata(url: String): LinkMetadata? {
    return try {
        val connection =
            withContext(Dispatchers.IO) {
                URL(url).openConnection()
            }

        withContext(Dispatchers.IO) {
            connection.connect()
        }

        // Parse HTML meta tags here
        LinkMetadata(
            url = url,
            title = connection.getHeaderField("og:title") ?: "Untitled",
            description = connection.getHeaderField("og:description"),
            imageUrl = connection.getHeaderField("og:image")
        )
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun NewMessageSection(
    messageText: TextFieldValue,
    onMessageChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Handle attachment */ }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Attach",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            TextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .clip(TextFieldShape),
                colors = textFieldColors(),
                placeholder = {
                    Text("Type a message...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                },
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColors.CoralAccent)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
 fun UserAvatar(imageUrl: String, isOnline: Boolean, size: Dp = 48.dp) {
    Box {
        // Replace with AsyncImage for real implementation
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(size)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User Avatar",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(AppColors.CoralAccent)
                    .align(Alignment.BottomEnd)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

// Preview
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChatScreenPreview() {
    ReflektTheme {
        ChatScreen(
            user = ChatUser(
                id = "1",
                name = "John Doe",
                imageUrl = "",
                lastMessage = "Hello!",
                unreadCount = 2,
                isOnline = true,
                lastSeen = ""
            ),
            onBackPressed = {},
            onSendMessage = {}
        )
    }
}

// Sample Data
private val sampleMessages = arrayOf(
    Message(
        id = "1",
        text = "Hey there! How are you?",
        senderId = "other",
        timestamp = "10:30 AM",
        isSent = false
    ),
    Message(
        id = "2",
        text = "I'm doing great, thanks for asking!",
        senderId = "currentUser",
        timestamp = "10:31 AM",
        isSent = true
    )
)

