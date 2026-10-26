package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Preview
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Message
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Status
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User
import com.google.firebase.Timestamp
import io.getstream.meeting.room.compose.ui.AppTheme

// ── Dummy data ──────────────────────────────────────────────────────────────

private const val CUR_USER = "cur_user_1"
private const val OTHER_USER = "other_user_1"

private val dummyOtherUser = User(
    userId = OTHER_USER,
    name = "Alex Chen",
    bio = "CS Major • Coffee addict",
    profilePicture = null
)

private val dummyStatus = Status(isOnline = true, typingTo = "")

private fun ts(secondsAgo: Long): Timestamp {
    val nowSec = System.currentTimeMillis() / 1000
    return Timestamp(nowSec - secondsAgo, 0)
}

private val dummyMessages = listOf(
    Message(
        messageId = "m1",
        senderId = OTHER_USER,
        receiverId = CUR_USER,
        message = "Hey! Are you coming to the Hackathon next week?",
        timestamp = ts(3600),
        status = "delivered"
    ),
    Message(
        messageId = "m2",
        senderId = CUR_USER,
        receiverId = OTHER_USER,
        message = "Yeah for sure! Saw your post about it 👀",
        timestamp = ts(3500),
        status = "read"
    ),
    Message(
        messageId = "m3",
        senderId = OTHER_USER,
        receiverId = CUR_USER,
        message = "Nice! We should team up. I'm strong on backend, need someone for UI",
        timestamp = ts(3400),
        status = "delivered"
    ),
    Message(
        messageId = "m4",
        senderId = CUR_USER,
        receiverId = OTHER_USER,
        message = "That's perfect 😄 I've been writing Compose for a while",
        timestamp = ts(3300),
        status = "read"
    ),
    Message(
        messageId = "m5",
        senderId = OTHER_USER,
        receiverId = CUR_USER,
        message = "Let's plan. Want to jump on a call this weekend?",
        timestamp = ts(60),
        status = "delivered"
    )
)

// ── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun PreviewShell(
    darkTheme: Boolean = false,
    uiState: ChatDetailUiState
) {
    AppTheme(darkTheme = darkTheme) {
        ChatDetailContent(
            uiState = uiState,
            callbacks = ChatDetailCallbacks(),
            focusRequester = remember { FocusRequester() }
        )
    }
}

// ── Loading / Shimmer ────────────────────────────────────────────────────────

@Preview(
    name = "Loading — Light",
    showBackground = true,
    widthDp = 390, heightDp = 844
)
@Composable
private fun PreviewLoadingLight() {
    PreviewShell(
        darkTheme = false,
        uiState = ChatDetailUiState(
            isLoadingMessages = true,
            messages = emptyList(),
            otherUser = dummyOtherUser,
            curUserId = CUR_USER,
            userStatus = dummyStatus
        )
    )
}

@Preview(
    name = "Loading — Dark",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    widthDp = 390, heightDp = 844
)
@Composable
private fun PreviewLoadingDark() {
    PreviewShell(
        darkTheme = true,
        uiState = ChatDetailUiState(
            isLoadingMessages = true,
            messages = emptyList(),
            otherUser = dummyOtherUser,
            curUserId = CUR_USER,
            userStatus = dummyStatus
        )
    )
}

// ── Empty state ──────────────────────────────────────────────────────────────

@Preview(
    name = "Empty — Light",
    showBackground = true,
    widthDp = 390, heightDp = 844
)
@Composable
private fun PreviewEmptyLight() {
    PreviewShell(
        darkTheme = false,
        uiState = ChatDetailUiState(
            isLoadingMessages = false,
            messages = emptyList(),
            otherUser = dummyOtherUser,
            curUserId = CUR_USER,
            userStatus = dummyStatus
        )
    )
}

@Preview(
    name = "Empty — Dark",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    widthDp = 390, heightDp = 844
)
@Composable
private fun PreviewEmptyDark() {
    PreviewShell(
        darkTheme = true,
        uiState = ChatDetailUiState(
            isLoadingMessages = false,
            messages = emptyList(),
            otherUser = dummyOtherUser,
            curUserId = CUR_USER,
            userStatus = dummyStatus
        )
    )
}

// ── With messages ────────────────────────────────────────────────────────────

@Preview(
    name = "With Messages — Light",
    showBackground = true,
    widthDp = 390, heightDp = 844
)
@Composable
private fun PreviewMessagesLight() {
    PreviewShell(
        darkTheme = false,
        uiState = ChatDetailUiState(
            isLoadingMessages = false,
            messages = dummyMessages,
            otherUser = dummyOtherUser,
            curUserId = CUR_USER,
            userStatus = dummyStatus,
            messageRequest = MessageRequest(status = MessageRequest.STATUS_NONE)
        )
    )
}

@Preview(
    name = "With Messages — Dark",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    widthDp = 390, heightDp = 844
)
@Composable
private fun PreviewMessagesDark() {
    PreviewShell(
        darkTheme = true,
        uiState = ChatDetailUiState(
            isLoadingMessages = false,
            messages = dummyMessages,
            otherUser = dummyOtherUser,
            curUserId = CUR_USER,
            userStatus = dummyStatus,
            messageRequest = MessageRequest(status = MessageRequest.STATUS_NONE)
        )
    )
}

// ── Event context banner ─────────────────────────────────────────────────────

@Preview(
    name = "Event Banner — Light",
    showBackground = true,
    widthDp = 390, heightDp = 844
)
@Composable
private fun PreviewEventBannerLight() {
    PreviewShell(
        darkTheme = false,
        uiState = ChatDetailUiState(
            isLoadingMessages = false,
            messages = dummyMessages,
            otherUser = dummyOtherUser,
            curUserId = CUR_USER,
            userStatus = dummyStatus,
            eventTitle = "NUS Hackathon 2024 — Build Something Amazing",
            showEventBanner = true
        )
    )
}

@Preview(
    name = "Event Banner — Dark",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    widthDp = 390, heightDp = 844
)
@Composable
private fun PreviewEventBannerDark() {
    PreviewShell(
        darkTheme = true,
        uiState = ChatDetailUiState(
            isLoadingMessages = false,
            messages = dummyMessages,
            otherUser = dummyOtherUser,
            curUserId = CUR_USER,
            userStatus = dummyStatus,
            eventTitle = "NUS Hackathon 2024 — Build Something Amazing",
            showEventBanner = true
        )
    )
}

// ── Typing indicator ─────────────────────────────────────────────────────────

@Preview(
    name = "Other User Typing — Dark",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    widthDp = 390, heightDp = 844
)
@Composable
private fun PreviewTypingDark() {
    PreviewShell(
        darkTheme = true,
        uiState = ChatDetailUiState(
            isLoadingMessages = false,
            messages = dummyMessages,
            otherUser = dummyOtherUser,
            curUserId = CUR_USER,
            userStatus = Status(isOnline = true, typingTo = CUR_USER)
        )
    )
}
