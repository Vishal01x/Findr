package com.exa.android.reflekt.loopit.presentation.main.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.core.utils.formatTimestamp
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.ChatList
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User
import com.exa.android.reflekt.loopit.presentation.main.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.presentation.main.placement.components.*
import com.exa.android.reflekt.loopit.presentation.main.post.components.UserAvatar
import com.exa.android.reflekt.loopit.presentation.main.post.components.AvatarSize

import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.Timestamp

@Composable
fun ChatListItem(chat: ChatList, currentUserId: String = "", openImage: (String?) -> Unit, openChat: (user: User) -> Unit) {
    val isIncomingRequest = chat.requestStatus == "pending" && chat.requestSenderId != currentUserId && chat.requestSenderId.isNotEmpty()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(16.dp))
            .clickableWithNoIndication {
                openChat(User(userId = chat.userId, name = chat.name, fcmToken = chat.fcmToken))
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            val profilePic = if (chat.isCurUserBlock) "" else chat.profilePicture

            UserAvatar(
                imageUrl = profilePic?.takeIf { it.isNotBlank() },
                userName = chat.name,
                size = AvatarSize.Large,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.clickableWithNoIndication(enabled = !profilePic.isNullOrEmpty()) { openImage(chat.profilePicture) }
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            chat.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = LiquidColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (chat.isOtherBlock) {
                            Icon(
                                Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                tint = LiquidColors.TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    val ts = chat.lastMessageTimestamp.seconds * 1000L
                    if (ts > 0) {
                        Text(
                            formatTimestamp(ts),
                            style = MaterialTheme.typography.labelSmall,
                            color = LiquidColors.TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    if (isIncomingRequest) "Sent you a message request" else chat.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isIncomingRequest) LiquidColors.Primary else LiquidColors.TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (isIncomingRequest) FontWeight.Medium else FontWeight.Normal
                )
            }

            if (isIncomingRequest) {
                // Amber "Request" badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF59E0B))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "Request",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (chat.unreadMessages > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(LiquidColors.Primary)
                ) {
                    Text(
                        if (chat.unreadMessages > 99) "99+" else "${chat.unreadMessages}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun ChatListItemPreview() {
    MaterialTheme   {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Normal chat
            ChatListItem(
                chat = ChatList(
                    userId = "1",
                    name = "Alice",
                    lastMessage = "Hey, how are you?",
                    lastMessageTimestamp = Timestamp.now(),
                    profilePicture = "",
                    unreadMessages = 2,
                    requestStatus = "accepted"
                ),
                currentUserId = "0",
                openImage = {},
                openChat = {}
            )
            
            // Incoming request
            ChatListItem(
                chat = ChatList(
                    userId = "2",
                    name = "Bob",
                    lastMessage = "Let's connect!",
                    lastMessageTimestamp = Timestamp.now(),
                    profilePicture = "",
                    unreadMessages = 0,
                    requestStatus = "pending",
                    requestSenderId = "2"
                ),
                currentUserId = "0",
                openImage = {},
                openChat = {}
            )
        }
    }
}