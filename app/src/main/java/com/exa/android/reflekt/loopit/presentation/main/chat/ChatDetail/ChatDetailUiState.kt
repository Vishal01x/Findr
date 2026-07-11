package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail

import android.net.Uri
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Message
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Status
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User

/**
 * Unified UI state for the chat detail screen.
 *
 * Splits cleanly into two buckets:
 *  - **ViewModel-driven** fields (messages, user info, block status, message request)
 *  - **Local interaction** fields (selection, reply/edit, event banner)
 *
 * [ChatDetailContent] is the single consumer; [DetailChat] is the single producer.
 */
data class ChatDetailUiState(
    // ── ViewModel-driven ────────────────────────────────────────────────────
    val messages: List<Message> = emptyList(),
    val isLoadingMessages: Boolean = true,
    val otherUser: User? = null,
    val curUserId: String = "",
    val isCurUserBlocked: Boolean = false,
    val isOtherUserBlocked: Boolean = false,
    val messageRequest: MessageRequest = MessageRequest(),
    val userStatus: Status? = null,
    // ── Local interaction ───────────────────────────────────────────────────
    val replyMessage: Message? = null,
    val editMessage: Message? = null,
    val selectedMessages: Set<Message> = emptySet(),
    val eventTitle: String? = null,
    val showEventBanner: Boolean = false
) {
    /** True only during initial load before any messages arrive. */
    val isLoading: Boolean get() = isLoadingMessages && messages.isEmpty()

    /** True when load finished but the conversation has no messages yet. */
    val isEmpty: Boolean get() = !isLoadingMessages && messages.isEmpty()

    /** Convenience list consumed by MessageList and NewMessageSection. */
    val members: List<User?> get() = listOf(otherUser, User(userId = curUserId))

    val isLastMessageSelected: Boolean
        get() = messages.isNotEmpty() && selectedMessages.contains(messages.last())
}

/**
 * All event callbacks [ChatDetailContent] can fire upward.
 *
 * All fields default to no-ops so previews can simply use
 * `ChatDetailCallbacks()` without wiring anything.
 */
data class ChatDetailCallbacks(
    // ── Navigation ──────────────────────────────────────────────────────────
    val onBackClick: () -> Unit = {},
    val onProfileClick: () -> Unit = {},
    // ── Message send / edit ─────────────────────────────────────────────────
    val onSendMessage: (text: String, replyTo: Message?) -> Unit = { _, _ -> },
    val onReply: (Message) -> Unit = {},
    val onCancelReply: () -> Unit = {},
    val onEditClick: (Message?) -> Unit = {},
    val onCancelEdit: () -> Unit = {},
    val onReplyFromHeader: (Message?) -> Unit = {},
    // ── Typing indicator ────────────────────────────────────────────────────
    val onTypingStatusChange: (message: String) -> Unit = {},
    // ── Selection ───────────────────────────────────────────────────────────
    val onUpdateSelection: (messages: Set<Message>, isLastSelected: Boolean) -> Unit = { _, _ -> },
    val onDeselectAll: () -> Unit = {},
    val onCopyMessages: () -> Unit = {},
    val onDeleteMessages: (deleteFor: Int) -> Unit = {},
    // ── Media ───────────────────────────────────────────────────────────────
    val onAddMedia: () -> Unit = {},
    val onRetryMedia: (Message) -> Unit = {},
    val onOpenImage: (url: String) -> Unit = {},
    // ── Reactions ───────────────────────────────────────────────────────────
    val onReact: (messageId: String, emoji: String) -> Unit = { _, _ -> },
    // ── Block / report ──────────────────────────────────────────────────────
    val onBlockClick: () -> Unit = {},
    val onUnblockClick: () -> Unit = {},
    val onReportClick: (reason: String, proofText: String?, proofImageUri: Uri?) -> Unit = { _, _, _ -> },
    val onClearChat: () -> Unit = {},
    // ── Message request ─────────────────────────────────────────────────────
    val onApproveRequest: () -> Unit = {},
    val onDeclineRequest: () -> Unit = {},
    val onAllowMessages: () -> Unit = {},
    // ── Event context ───────────────────────────────────────────────────────
    val onDismissEventBanner: () -> Unit = {},
    // ── Empty-state shortcut ────────────────────────────────────────────────
    val onSayHi: () -> Unit = {}
)
