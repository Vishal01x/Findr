package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.linkPreview.LinkPreview
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.linkPreview.LinkPreviewCard
import com.exa.android.reflekt.loopit.util.LinkUtils
import com.exa.android.reflekt.loopit.util.formatTimestamp
import com.exa.android.reflekt.loopit.util.getVibrator
import com.exa.android.reflekt.loopit.util.model.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MessageList(
    messages: List<Message>,
    curUser: String,
    unreadMessages: Int,
    selectedMessages: Set<Message>,

    updateMessages: (Set<Message>) -> Unit,
    onReply: (message: Message) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var highlightedIndex by remember { mutableStateOf<Int?>(null) }
    val renderedIndex = remember { mutableStateMapOf<String, Int>() }

//    LaunchedEffect(messages.size) {
//          listState.animateScrollToItem(messages.lastIndex)
//    }

    Log.d("Chat Messages1234", "Messages - ${messages.toString()}")

    LazyColumn(
        state = listState,
        reverseLayout = true
    ) {
        itemsIndexed(messages.reversed()) { index, message ->
            renderedIndex[message.messageId] = index
            if (message.members.contains(curUser)) {
            MessageBubble(
                message = message,
                curUserId = curUser,
                isSelected = selectedMessages.contains(message),
                selectedMessagesSize = selectedMessages.size,
                isHighlighted = highlightedIndex == index,
                onTapOrLongPress = {
                    onMessageLongPress(
                        message,
                        selectedMessages,
                        onSelect = { updatedSelection ->
                            updateMessages(updatedSelection)
                        })
                },
                onReply = { message ->
                    onReply(message)
                },
                onReplyClick = { id ->
                    coroutineScope.launch {// since using launched effect i was not able to re-scroll to the same message again and again
                        // and if we try to maintain any variable so we need to update it that causes re-composition that leads it to scroll till mid and before reaching
                        // re-scroll to end
                        scrollToMessage(id, renderedIndex, listState)
                        renderedIndex[id]?.let { index ->
                            highlightedIndex = index
                            delay(500)
                            highlightedIndex = null
                        }
                    }
                }
            )
            }
        }
        item{
            Spacer(Modifier.height(4.dp))
        }
    }


}

@Composable
fun MessageBubble(
    message: Message,
    curUserId: String,
    isSelected: Boolean, // it is used to extract that particular messages is selected or not
    selectedMessagesSize: Int, // it is passed to use a key for pointerInput so it changes whenever
    // selection changes and causes the detectGesture to call
    isHighlighted: Boolean, // it used to change color of messages for 500ms to reply that reply message is rendered
    onTapOrLongPress: () -> Unit, //select and unselect messages
    onReply: (Message) -> Unit, // pass the message which is to be reply
    onReplyClick: (String) -> Unit // pass the click reply index to messageList to scroll and update the ui of replied message
) {
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // used in vibration
    val vibrator = getVibrator(context) // to vibrate on right Swipe

    Log.d("Chat Messages123", message.toString())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFF007AFF
            ).copy(alpha = .2f) else Color.Transparent)
            .pointerInput(selectedMessagesSize) { // selectedMessagesSize is used for Key as it change it enables to call
                detectTapGestures(
                    onTap = { if (selectedMessagesSize > 0) onTapOrLongPress() },
                    onLongPress = { onTapOrLongPress() }
                )
            }
    ) {
        if (offsetX.value > 50) { // show reply icon on right swipe
            SwipeHint(icon = R.drawable.ic_reply, alignment = Alignment.CenterStart)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                //.background(if (isSelected) Color.Yellow else Color.Transparent)
                .pointerInput(selectedMessagesSize <= 0) { // active whenever message is unselected
                    /*if (message.message != "deleted") {
                        detectHorizontalDragGestures(// used for applying rightSwipe gesture for replyMessage functionality
                            onDragEnd = {
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        0f,
                                        animationSpec = tween(durationMillis = 600)
                                    )
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                coroutineScope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount)
                                }
                                if (offsetX.value > 100) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator?.vibrate(
                                            VibrationEffect.createOneShot(
                                                50,
                                                VibrationEffect.DEFAULT_AMPLITUDE
                                            )
                                        )
                                        onReply(message) // send reply message to messageList using Lambda
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
                        )
                    }*/
                }
                .offset { IntOffset(offsetX.value.toInt(), 0) } // applying animation
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = if (curUserId == message.senderId) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
        ) {
            val bubbleColor = if (curUserId == message.senderId) Color(
                0xFF007AFF
            ).copy(alpha = .6f) else Color(0xFFf6f6f6)
            Column(
                modifier = Modifier
                    .widthIn(max = (0.7 * LocalConfiguration.current.screenWidthDp).dp) // occupy 70% of screen only
                    .background(
                        color = if (isHighlighted) bubbleColor.copy(alpha = 0.6f) else bubbleColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(top = 8.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
            ) {
                Box(modifier = Modifier
                    //.fillMaxWidth()
                    .clickable(enabled = selectedMessagesSize <= 0 && message.message != "deleted") {
                        message.replyTo?.let {
                            onReplyClick(
                                it.messageId
                            )
                        }
                    }) {
                    message.replyTo?.let {
//                        if (message.message != "deleted")
//                            ReplyUi(
//                                curUser = curUserId,
//                                replyTo = it,
//                                members = members
//                            ) // show replied message inside box
                    }
                }

                if (message.message == "deleted") {
                    Text(
                        text = if (message.senderId == curUserId) "You deleted this message" else "This message was deleted",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = if (message.senderId == curUserId) Color.White.copy(alpha = 0.8F) else Color.Black.copy(
                            alpha = 0.8F
                        ),
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    if(LinkUtils.containsLink(text = message.message)){
                        LinkPreview(message.message, message.senderId == curUserId, selectedMessagesSize)
                    }else {
                        Text(
                            text = message.message,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                            color = if (curUserId == message.senderId) Color.White else Color.Black,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    val timestampInMillis = message.timestamp.seconds * 1000L
                    Text(
                        text = formatTimestamp(timestampInMillis), // generate timeStamp like hrs, yesterday
                        style = MaterialTheme.typography.labelSmall,
                        fontStyle = FontStyle.Italic,
                        color = if (curUserId == message.senderId) Color.White else Color.Gray
                    )
                    if (curUserId == message.senderId && message.message != "deleted") {
                        Spacer(modifier = Modifier.width(2.dp))
                        Log.d("Detail Chat", message.status)

                        if (message.status == "delivered") {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = Color.White,  // Gray color for sent
                                modifier = Modifier.size(14.dp)
                            )
                        } else if (message.status == "sent") {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_seen),
                                contentDescription = "Delivered",
                                tint = Color.White,  // Gray color for delivered
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_seen),
                                contentDescription = "Seen",
                                tint = Color.Yellow,  // Blue color for seen
                                modifier = Modifier.size(14.dp)
                            )
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
            .border(1.dp, Color.White, CircleShape)
            .background(Color.Black)
            .padding(2.dp),
        contentAlignment = alignment
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "Reply Icon",
            tint = Color.White,
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
    renderedIndex[messageId]?.let { index ->
        listState.animateScrollToItem(index)
    }
}

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
                    color = if (curUser == message.senderId) Color(0xFF007AFF) else Color(0xFFEAEAEA),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Message Text
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyLarge,
                color = if (curUser == message.senderId) Color.White else Color.Black,
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
                    color = if (curUser == message.senderId) Color.White else Color.Gray
                )

            }
        }
    }
}