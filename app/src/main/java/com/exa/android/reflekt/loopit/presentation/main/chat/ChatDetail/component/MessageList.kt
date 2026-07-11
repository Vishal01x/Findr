package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component

import android.os.Build
import android.os.VibrationEffect
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.window.Popup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.letstalk.presentation.Main.Home.ChatDetail.components.media.docs.DocumentMessageItem
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.linkPreview.LinkPreview
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.media.image.downloadMedia
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.media.getFileNameFromUrl
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.media.image.ImageMessageContent
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.media.video.VideoMessageItem
import com.exa.android.reflekt.loopit.core.utils.LinkUtils
import com.exa.android.reflekt.loopit.core.utils.formatTimestamp
import com.exa.android.reflekt.loopit.core.utils.formatTimeOnly
import com.exa.android.reflekt.loopit.core.utils.getVibrator
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MediaType
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Message
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.ReplyType
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.UploadStatus
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User
import com.exa.android.reflekt.loopit.theme.Spacing
import com.exa.android.reflekt.loopit.theme.SuccessColor

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MessageList(
    messages: List<Message>,
    curUser: String,
    members: List<User?>,
    unreadMessages: Int,
    selectedMessages: Set<Message>,
    updateMessages: (Set<Message>, Boolean) -> Unit,
    onReply: (message: Message) -> Unit,
    onRetry: (Message) -> Unit,
    openImage: (String) -> Unit,
    isLoading: Boolean = false,
    onSayHi: () -> Unit = {},
    onReact: (messageId: String, emoji: String) -> Unit = { _, _ -> }
) {
    when {
        isLoading -> { MessageListShimmer(); return }
        messages.isEmpty() -> { ChatEmptyState(onSayHi = onSayHi); return }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var highlightedIndex by remember { mutableStateOf<Int?>(null) }
    val renderedIndex = remember { mutableStateMapOf<String, Int>() }

    val lastMessage = messages.lastOrNull()
    val isLastMessageSelected = lastMessage != null && selectedMessages.contains(lastMessage)
    var lastOtherUserMessageIdx = -1

    // Group messages by date, filtering out groups with no visible messages
    val groupedMessages = remember(messages) {
        messages
            .groupBy { message -> getDateLabel(message.timestamp.seconds * 1000L) }
            .filter { (_, msgs) -> msgs.any { it.members.contains(curUser) } }
    }

    LazyColumn(
        state = listState,
        reverseLayout = true
    ) {
        // Track global LazyColumn index across date groups so scrollToMessage works correctly.
        // Each group's itemsIndexed gives a LOCAL index starting at 0, so we accumulate offsets.
        var groupOffset = 0
        groupedMessages.entries.reversed().forEach { (dateLabel, messagesForDate) ->
            val reversedMsgs = messagesForDate.reversed()
            val offset = groupOffset  // capture current value before itemsIndexed lambda captures it lazily
            itemsIndexed(reversedMsgs) { localIdx, message ->
                val globalIdx = offset + localIdx
                renderedIndex[message.messageId] = globalIdx
                val isSentByCurrentUser = message.senderId == curUser
                if (!isSentByCurrentUser && lastOtherUserMessageIdx == -1) lastOtherUserMessageIdx = globalIdx
                val hasUnfinishedMedia =
                    (message.media != null && message.media.uploadStatus != UploadStatus.SUCCESS)

                if (message.members.contains(curUser)) {
                    if (!hasUnfinishedMedia || (hasUnfinishedMedia && isSentByCurrentUser)) {
                        MessageBubble(
                            message = message,
                            curUserId = curUser,
                            members = members,
                            isSelected = selectedMessages.contains(message),
                            selectedMessagesSize = selectedMessages.size,
                            isHighlighted = highlightedIndex == globalIdx,
                            isSeen = (isSentByCurrentUser && lastOtherUserMessageIdx != -1 && lastOtherUserMessageIdx < globalIdx),
                            showInlineReactionPicker = selectedMessages.size == 1 && selectedMessages.contains(message),
                            onTapOrLongPress = {
                                onMessageLongPress(
                                    message,
                                    selectedMessages,
                                    onSelect = { updatedSelection ->
                                        updateMessages(updatedSelection, isLastMessageSelected)
                                    })
                            },
                            onReply = { msg -> onReply(msg) },
                            onReplyClick = { id ->
                                coroutineScope.launch {
                                    scrollToMessage(id, renderedIndex, listState)
                                    renderedIndex[id]?.let { idx ->
                                        highlightedIndex = idx
                                        delay(1000)
                                        highlightedIndex = null
                                    }
                                }
                            },
                            onRetry = { onRetry(message) },
                            openImage = openImage,
                            onReact = { emoji -> onReact(message.messageId, emoji) }
                        )
                    }
                }
            }
            groupOffset += reversedMsgs.size

            item(key = "date_$dateLabel") {
                DateSeparator(dateLabel = dateLabel)
            }
            groupOffset += 1
        }

        item {
            Spacer(Modifier.height(4.dp))
        }
    }


}

/**
 * Date separator component for chat messages
 */
@Composable
fun DateSeparator(
    dateLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Helper function to get date label for messages
 */
fun getDateLabel(timestampMillis: Long): String {
    val messageDate = java.util.Calendar.getInstance().apply {
        timeInMillis = timestampMillis
    }
    val today = java.util.Calendar.getInstance()
    val yesterday = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        isSameDay(messageDate, today) -> "Today"
        isSameDay(messageDate, yesterday) -> "Yesterday"
        else -> {
            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            dateFormat.format(java.util.Date(timestampMillis))
        }
    }
}

fun isSameDay(cal1: java.util.Calendar, cal2: java.util.Calendar): Boolean {
    return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
}


@Composable
fun MessageBubble(
    message: Message,
    curUserId: String,
    members : List<User?>,
    isSelected: Boolean,
    selectedMessagesSize: Int,
    isHighlighted: Boolean,
    isSeen : Boolean,
    onTapOrLongPress: () -> Unit,
    onReply: (Message) -> Unit,
    onReplyClick: (String) -> Unit,
    onRetry: () -> Unit,
    openImage :(String) -> Unit,
    showInlineReactionPicker: Boolean = false,
    onReact: (emoji: String) -> Unit = {}
) {
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val vibrator = getVibrator(context)

    val highlightAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.3f else 0f,
        animationSpec = if (isHighlighted) tween(100) else tween(800),
        label = "highlight"
    )

    val isSent = curUserId == message.senderId
    val senderUser = if (!isSent) members.find { it?.userId == message.senderId } else null
    val senderName = senderUser?.name?.takeIf { it.isNotBlank() } ?: "Unknown"
    val senderInitial = senderName.firstOrNull()?.uppercase() ?: "?"
    val timestampInMillis = message.timestamp.seconds * 1000L
    val timeText = formatTimeOnly(timestampInMillis)
    val messageStatus = if (isSeen) "seen" else message.status

    val bubbleShape = if (isSent) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }
    val sentGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1EC8F8), Color(0xFF0D9ED4)),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    val bubbleBgMod = if (isSent)
        Modifier.background(sentGradient)
    else
        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = highlightAlpha)
                }
            )
    ) {
        if (offsetX.value > 60) {
            SwipeHint(icon = R.drawable.reply_icon, alignment = Alignment.CenterStart)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(selectedMessagesSize) {
                    detectTapGestures(
                        onTap = { if (selectedMessagesSize > 0) onTapOrLongPress() },
                        onLongPress = { onTapOrLongPress() }
                    )
                }
                .pointerInput(selectedMessagesSize <= 0) {
                    if (message.message != "deleted") {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        0f,
                                        animationSpec = tween(durationMillis = 600)
                                    )
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                if (kotlin.math.abs(dragAmount) > kotlin.math.abs(change.positionChange().y)) {
                                    if (dragAmount > 0) {
                                        coroutineScope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                                        if (offsetX.value > 180) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                vibrator?.vibrate(
                                                    VibrationEffect.createOneShot(
                                                        50,
                                                        VibrationEffect.DEFAULT_AMPLITUDE
                                                    )
                                                )
                                                onReply(message)
                                                coroutineScope.launch {
                                                    offsetX.animateTo(
                                                        0f,
                                                        animationSpec = tween(durationMillis = 600)
                                                    )
                                                }
                                            }
                                            change.consume()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
        ) {
            // Letter avatar for received messages (bottom-aligned by Row)
            if (!isSent) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = senderInitial,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(6.dp))
            }

            // Column: [header label] + [bubble]
            Column(
                modifier = Modifier.widthIn(max = (0.75 * LocalConfiguration.current.screenWidthDp).dp),
                horizontalAlignment = if (isSent) Alignment.End else Alignment.Start
            ) {
                // Sender name + time shown above the bubble
                Row(
                    modifier = Modifier.padding(
                        start = if (!isSent) 4.dp else 0.dp,
                        end = if (isSent) 4.dp else 0.dp,
                        bottom = 3.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!isSent) {
                        Text(
                            text = "$senderName • $timeText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (message.message != "deleted") {
                            when (messageStatus) {
                                "sent" -> Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sent",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(12.dp)
                                )

                                "delivered" -> Icon(
                                    painter = painterResource(id = R.drawable.ic_seen),
                                    contentDescription = "Delivered",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(12.dp)
                                )

                                else -> Icon(
                                    painter = painterResource(id = R.drawable.ic_seen),
                                    contentDescription = "Seen",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // Reaction box wrapping bubble
                val reactions =
                    if (message.message != "deleted") message.reactions.orEmpty() else emptyMap()
                Box {
                    // Bubble box
                    Box(
                        modifier = Modifier
                            .padding(bottom = if (reactions.isNotEmpty()) 12.dp else 0.dp)
                            .clip(bubbleShape)
                            .then(bubbleBgMod)
                    ) {
                        Column(
                            modifier = Modifier
                                .pointerInput(selectedMessagesSize <= 0) {
                                    if (message.message != "deleted") {
                                        detectHorizontalDragGestures(
                                            onDragEnd = {
                                                coroutineScope.launch {
                                                    offsetX.animateTo(
                                                        0f,
                                                        animationSpec = tween(durationMillis = 600)
                                                    )
                                                }
                                            },
                                            onHorizontalDrag = { change, dragAmount ->
                                                if (kotlin.math.abs(dragAmount) > kotlin.math.abs(
                                                        change.positionChange().y
                                                    )
                                                ) {
                                                    if (dragAmount > 0) {
                                                        coroutineScope.launch {
                                                            offsetX.snapTo(
                                                                offsetX.value + dragAmount
                                                            )
                                                        }
                                                        if (offsetX.value > 180) {
                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                vibrator?.vibrate(
                                                                    VibrationEffect.createOneShot(
                                                                        50,
                                                                        VibrationEffect.DEFAULT_AMPLITUDE
                                                                    )
                                                                )
                                                            }
                                                            onReply(message)
                                                            coroutineScope.launch {
                                                                offsetX.animateTo(
                                                                    0f,
                                                                    animationSpec = tween(
                                                                        durationMillis = 600
                                                                    )
                                                                )
                                                            }
                                                        }
                                                        change.consume()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                                .padding(Spacing.medium)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickableWithNoIndication(enabled = selectedMessagesSize <= 0 && message.message != "deleted") {
                                        message.replyTo?.let { onReplyClick(it.messageId) }
                                    }) {
                                message.replyTo?.let {
                                    if (message.message != "deleted")
                                        ReplyUi(
                                            curUser = curUserId,
                                            replyTo = it,
                                            members = members,
                                            replyType = if (isSent) ReplyType.YOU else ReplyType.OTHER
                                        )
                                }
                            }

                            if (message.message == "deleted") {
                                Text(
                                    text = if (isSent) "You deleted this message" else "This message was deleted",
                                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                                    color = if (isSent)
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Italic
                                )
                            } else {
                                if (LinkUtils.containsLink(text = message.message)) {
                                    LinkPreview(message.message, isSent, selectedMessagesSize)
                                } else {
                                    if (message.media == null) {
                                        Text(
                                            text = message.message,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                lineHeight = 24.sp
                                            ),
                                            color = if (isSent)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        val fileName = getFileNameFromUrl(message.media.mediaUrl)
                                        when (message.media.mediaType) {
                                            MediaType.IMAGE -> {
                                                ImageMessageContent(
                                                    imageUrl = message.media.mediaUrl,
                                                    fileName = fileName,
                                                    isSent,
                                                    message.media.uploadStatus,
                                                    onRetry = { onRetry() },
                                                    onDownloadClick = {
                                                        downloadMedia(
                                                            context,
                                                            message.media.mediaUrl,
                                                            fileName = fileName
                                                        )
                                                    },
                                                    onImageClick = { openImage(message.media.mediaUrl) }
                                                )
                                            }

                                            MediaType.VIDEO -> {
                                                VideoMessageItem(
                                                    message.media.mediaUrl,
                                                    fileName,
                                                    isSent,
                                                    message.media.uploadStatus,
                                                    onRetry = { onRetry() })
                                            }

                                            MediaType.AUDIO -> TODO()
                                            MediaType.DOCUMENT -> {
                                                DocumentMessageItem(
                                                    fileUrl = message.media.mediaUrl,
                                                    fileName = getFileNameFromUrl(message.media.mediaUrl),
                                                    isSent,
                                                    message.media.uploadStatus,
                                                    onRetry = { onRetry() }
                                                )
                                            }

                                            MediaType.LOCATION -> {}
                                            MediaType.CONTACT -> {}
                                            else -> Text("Nothing")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Reaction chips overlapping bubble
                    if (reactions.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(if (isSent) Alignment.BottomStart else Alignment.BottomEnd)
                                .offset(x = if (isSent) (-8).dp else 8.dp, y = 4.dp)
                        ) {
                            ReactionChips(
                                reactions = reactions,
                                curUserId = curUserId,
                                onReact = { emoji -> onReact(emoji) }
                            )
                        }
                    }
                } // Close the outer Box we added

                // Inline reaction picker — sits directly below the bubble in the Column
                if (showInlineReactionPicker && message.message != "deleted") {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 6.dp,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            REACTION_EMOJIS.forEach { emoji ->
                                Text(
                                    text = emoji,
                                    fontSize = 26.sp,
                                    modifier = Modifier
                                        .clickable {
                                            onReact(emoji)
                                            onTapOrLongPress() // deselect after reacting
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeHint(icon: Int, alignment: Alignment) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(2.dp),
        contentAlignment = alignment
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = "Reply Icon",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun onMessageLongPress(
    message: Message,
    selectedMessages: Set<Message>,
    onSelect: (Set<Message>) -> Unit
) {
    Log.d("checkingSelected", "${selectedMessages.toString()} -  $message")
    onSelect(if (selectedMessages.contains(message)) selectedMessages - message else selectedMessages + message)
}

suspend fun scrollToMessage(
    messageId: String,
    renderedIndex: MutableMap<String, Int>,
    listState: LazyListState
) {
    val index = renderedIndex[messageId] ?: return
    // reverseLayout=true: item appears at bottom by default.
    // Negative scrollOffset shifts item upward toward vertical center.
    val viewportHeight = listState.layoutInfo.viewportSize.height
    listState.animateScrollToItem(index, scrollOffset = -(viewportHeight / 2))
}

private val REACTION_EMOJIS = listOf("👍", "❤️", "😂", "😮", "😢", "😡")

@Composable
fun ReactionPicker(onEmojiSelected: (String) -> Unit, onDismiss: () -> Unit) {
    Popup(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                REACTION_EMOJIS.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 26.sp,
                        modifier = Modifier
                            .clickable { onEmojiSelected(emoji) }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReactionChips(
    reactions: Map<String, String>,
    curUserId: String,
    onReact: (String) -> Unit
) {
    val grouped = reactions.values.groupingBy { it }.eachCount()
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        grouped.forEach { (emoji, count) ->
            val isMe = reactions[curUserId] == emoji
            Surface(
                onClick = { onReact(emoji) },
                shape = RoundedCornerShape(50.dp),
                color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isMe) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = emoji, fontSize = 14.sp)
                }
            }
        }
    }
}

// ── Shimmer loader ────────────────────────────────────────────────────────────

private data class ShimmerSpec(val isSent: Boolean, val widthFraction: Float)

private val shimmerSpecs = listOf(
    ShimmerSpec(false, 0.55f),
    ShimmerSpec(true,  0.40f),
    ShimmerSpec(false, 0.70f),
    ShimmerSpec(true,  0.55f),
    ShimmerSpec(false, 0.45f),
    ShimmerSpec(true,  0.60f),
    ShimmerSpec(false, 0.65f),
    ShimmerSpec(true,  0.35f),
)

@Composable
fun MessageListShimmer() {
    val transition = rememberInfiniteTransition(label = "msg_shimmer")
    val translateX by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1.0f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1.0f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 600f, 300f)
    )

    LazyColumn(
        reverseLayout = true,
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 8.dp, vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(shimmerSpecs) { spec ->
            ShimmerMessageItem(spec.isSent, spec.widthFraction, shimmerBrush)
        }
    }
}

@Composable
private fun ShimmerMessageItem(isSent: Boolean, widthFraction: Float, brush: Brush) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isSent) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Spacer(Modifier.width(6.dp))
        }
        Column(
            horizontalAlignment = if (isSent) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tiny timestamp shimmer
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
            // Bubble shimmer
            Box(
                modifier = Modifier
                    .width(screenWidth * widthFraction)
                    .height((40 + (widthFraction * 20).toInt()).dp)
                    .clip(
                        if (isSent)
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
                        else
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
                    )
                    .background(brush)
            )
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
fun ChatEmptyState(onSayHi: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "No messages yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Be the first to start the conversation",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onSayHi,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 32.dp, vertical = 14.dp
                )
            ) {
                Text(
                    text = "Say Hi 👋",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Legacy MessageBubble overload (kept for binary compat) ────────────────────

@Composable
fun MessageBubble(message: Message, curUser: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (curUser == message.senderId) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = (0.7 * LocalConfiguration.current.screenWidthDp).dp)
                .background(
                    color = if (curUser == message.senderId) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Message Text
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyLarge,
                    color = if (curUser == message.senderId)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Timestamp and Status Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End)
            ) {
                val timestampInMillis = message.timestamp.seconds * 1000L
                Text(
                    text = formatTimestamp(timestampInMillis), // A helper function to format timestamp
                    style = MaterialTheme.typography.labelSmall,
                    color = if (curUser == message.senderId) 
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

            }
        }
    }
}
