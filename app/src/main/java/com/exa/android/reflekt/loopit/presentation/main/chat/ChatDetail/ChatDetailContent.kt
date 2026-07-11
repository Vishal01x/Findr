package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.ChatHeader
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.MessageList
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.NewMessageSection
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.resolveRequestInputState
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

/**
 * Pure stateless UI for the chat detail screen.
 *
 * Has **no ViewModel dependency** — all state comes from [uiState] and all
 * events go out through [callbacks]. This makes it fully previewable.
 */
@Composable
fun ChatDetailContent(
    uiState: ChatDetailUiState,
    callbacks: ChatDetailCallbacks,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = Color.Transparent,
        topBar = {
            ChatHeader(
                user = uiState.otherUser,
                status = uiState.userStatus,
                curUser = uiState.curUserId,
                selectedMessages = uiState.selectedMessages,
                isCurUserBlock = uiState.isCurUserBlocked,
                isOtherUserBlock = uiState.isOtherUserBlocked,
                onProfileClick = callbacks.onProfileClick,
                onBackClick = callbacks.onBackClick,
                onVoiceCallClick = {},
                onVideoCallClick = {},
                onUnselectClick = callbacks.onDeselectAll,
                onCopyClick = callbacks.onCopyMessages,
                onEditClick = callbacks.onEditClick,
                onReplyClick = callbacks.onReplyFromHeader,
                onForwardClick = {},
                onDeleteClick = callbacks.onDeleteMessages,
                onClearChatClick = callbacks.onClearChat,
                onBlockClick = callbacks.onBlockClick,
                onReportClick = callbacks.onReportClick
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LiquidColors.GlassSurface)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Column {
                    if (uiState.showEventBanner && uiState.eventTitle != null) {
                        EventContextBanner(
                            eventTitle = uiState.eventTitle,
                            onDismiss = callbacks.onDismissEventBanner
                        )
                    }
                    NewMessageSection(
                        curUser = uiState.curUserId,
                        members = uiState.members,
                        typingTo = uiState.otherUser?.userId ?: "",
                        replyTo = uiState.replyMessage,
                        isOtherUserBlocked = uiState.isOtherUserBlocked,
                        onTypingStatusChange = callbacks.onTypingStatusChange,
                        editMessage = uiState.editMessage,
                        focusRequester = focusRequester,
                        onTextMessageSend = callbacks.onSendMessage,
                        onRecordingSend = {},
                        onAddClick = callbacks.onAddMedia,
                        onUnblockClick = callbacks.onUnblockClick,
                        onSendOrDiscard = callbacks.onCancelReply,
                        onDone = {},
                        onFocusChange = {},
                        requestInputState = resolveRequestInputState(
                            uiState.messageRequest,
                            uiState.curUserId
                        ),
                        otherUserName = uiState.otherUser?.name ?: "",
                        onApproveRequest = callbacks.onApproveRequest,
                        onDeclineRequest = callbacks.onDeclineRequest,
                        onAllowMessages = callbacks.onAllowMessages
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(paddingValues)
        ) {
            val isReceiverPending =
                uiState.messageRequest.status == com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.STATUS_PENDING &&
                uiState.messageRequest.receiverId == uiState.curUserId

            val visibleMessages = if (isReceiverPending)
                uiState.messages.takeLast(
                    com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.MAX_REQUEST_MESSAGES
                )
            else
                uiState.messages

            MessageList(
                messages = visibleMessages,
                curUser = uiState.curUserId,
                members = uiState.members,
                unreadMessages = 0,
                selectedMessages = uiState.selectedMessages,
                isLoading = uiState.isLoading,
                onSayHi = callbacks.onSayHi,
                updateMessages = { updatedSet, isLastSelected ->
                    callbacks.onUpdateSelection(updatedSet, isLastSelected)
                },
                onReply = { msg ->
                    callbacks.onReply(msg)
                },
                onRetry = callbacks.onRetryMedia,
                openImage = callbacks.onOpenImage,
                onReact = callbacks.onReact
            )
        }
    }
}

/**
 * Dismissable banner shown above the message input when the user
 * navigated here via the "Chat" button on an event detail screen.
 */
@Composable
internal fun EventContextBanner(eventTitle: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LiquidColors.Primary.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Event,
            contentDescription = null,
            tint = LiquidColors.Primary,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = "About: $eventTitle",
            style = MaterialTheme.typography.labelMedium,
            color = LiquidColors.Primary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = LiquidColors.Primary.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
    HorizontalDivider(color = LiquidColors.Primary.copy(alpha = 0.15f), thickness = 0.5.dp)
}
