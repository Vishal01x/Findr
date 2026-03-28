package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.UserViewModel
import com.exa.android.reflekt.loopit.core.utils.AudioWaveForm
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MediaType
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Message
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.ReplyType
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User
import com.exa.android.reflekt.loopit.theme.chatInputBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun NewMessageSection(
    curUser: String,
    members: List<User?>,
    typingTo: String,
    replyTo: Message?,
    isOtherUserBlocked: Boolean,
    viewModel: UserViewModel,
    editMessage: Message?,
    focusRequester: FocusRequester,
    onTextMessageSend: (String, Message?) -> Unit,
    onRecordingSend: () -> Unit,
    onAddClick: () -> Unit,
    onUnblockClick: () -> Unit,
    onSendOrDiscard: () -> Unit,
    onDone: () -> Unit,
    onFocusChange: (FocusState) -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf("00:00") }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var timerJob by remember { mutableStateOf<Job?>(null) }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(1000L)
                elapsedSeconds++
                recordingTime = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)
            }
        }
    }

    fun pauseTimer() { timerJob?.cancel() }

    fun resetTimer() {
        isRecording = false
        isPaused = false
        recordingTime = "00:00"
        elapsedSeconds = 0
        timerJob?.cancel()
    }

    LaunchedEffect(isRecording, isPaused) {
        if (isRecording) {
            if (!isPaused) startTimer() else pauseTimer()
        } else {
            resetTimer()
        }
    }

    if (!isRecording) {
        // Text Input UI
        SendTFMessage(
            curUser,
            members,
            editMessage,
            replyTo,
            isOtherUserBlocked,
            focusRequester,
            onSendClick = { message, replyTo ->
                onSendOrDiscard()
                onTextMessageSend(message, replyTo)
            },
            onAddClick = onAddClick,
            onMicClick = { isRecording = true },
            onTyping = { message ->
                if (message.isEmpty())
                    viewModel.setTypingStatus(curUser, "")
                else
                    viewModel.setTypingStatus(curUser, typingTo)
            },
            onUnblockClick = onUnblockClick,
            onDiscardReply = onSendOrDiscard,
            onDone = onDone,
            onFocusChange = onFocusChange
        )
    } else {
        // Audio Recording UI
//        SendAudioMessage(
//            isPaused = isPaused,
//            recordingTime = recordingTime,
//            onDeleteRecording = {
//                resetTimer()
//            },
//            onPauseResumeRecording = {
//                isPaused = !isPaused
//            },
//            onSendRecording = {
//                onRecordingSend()
//                resetTimer()
//            }
//        )
    }
}


@Composable
fun SendTFMessage(
    curUser: String,
    members: List<User?>,
    editMessage: Message?,
    replyTo: Message?,
    isOtherUserBlocked: Boolean,
    focusRequester: FocusRequester,
    onSendClick: (String, Message?) -> Unit,
    onAddClick: () -> Unit,
    onMicClick: () -> Unit,
    onTyping: (msg: String) -> Unit,
    onUnblockClick: () -> Unit,
    onDiscardReply: () -> Unit,
    onDone: () -> Unit,
    onFocusChange: (FocusState) -> Unit
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(editMessage) {
        textFieldValue = TextFieldValue(
            text = editMessage?.message ?: "",
            selection = TextRange(editMessage?.message?.length ?: 0)
        )
        if (editMessage != null) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val hasText = textFieldValue.text.isNotEmpty()
    val inputBg = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val sendBtnBg =
        if (hasText) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
            alpha = 0.12f
        )
    val sendIconTint =
        if (hasText) MaterialTheme.colorScheme.surface else onSurfaceColor.copy(alpha = 0.35f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 5.dp,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // ── Blocked banner ──────────────────────────────────────────────
            if (isOtherUserBlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "You have blocked this user",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedButton(
                            onClick = onUnblockClick,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Unblock")
                        }
                    }
                }
            } else {
                // ── Reply preview ────────────────────────────────────────────
                if (replyTo != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        ReplyUi(curUser, replyTo, members, true, ReplyType.NEWMESSAGE) {
                            onDiscardReply()
                            onDone()
                        }
                    }
                }

                // ── Main bar ─────────────────────────────────────────────────
                // Matches reference: [◉+]  [────── pill input ──────]  [◉↑]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ── + circular button ──────────────────────────────────
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(inputBg)
                    ) {
                        IconButton(onClick = onAddClick) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add attachment",
                                tint = onSurfaceColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // ── Pill-shaped text input ────────────────────────────
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(30.dp))
                            .background(inputBg)
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "Message…",
                                color = onSurfaceColor.copy(alpha = 0.38f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = {
                                textFieldValue = it
                                onTyping(it.text)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 20.dp, max = 120.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged { onFocusChange(it) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = onSurfaceColor
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            maxLines = 5,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val text = textFieldValue.text.trim()
                                    if (text.isNotEmpty()) {
                                        onSendClick(text, replyTo)
                                        textFieldValue = TextFieldValue("")
                                        onTyping("")
                                    }
                                }
                            )
                        )
                    }

                    // ── Send circular button ──────────────────────────────
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(sendBtnBg)
                    ) {
                        IconButton(
                            onClick = {
                                val text = textFieldValue.text.trim()
                                if (text.isNotEmpty()) {
                                    onSendClick(text, replyTo)
                                    textFieldValue = TextFieldValue("")
                                    onTyping("")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send message",
                                tint = sendIconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SendAudioMessage(
    isPaused: Boolean,
    recordingTime: String,
    onDeleteRecording: () -> Unit,
    onPauseResumeRecording: () -> Unit,
    onSendRecording: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.chatInputBackground),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete Button
            IconButton(onClick = onDeleteRecording) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Recording",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Timer
            Text(
                text = recordingTime,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Sound Wave Animation
            if (!isPaused) {
                AudioWaveForm(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    isPaused = isPaused
                )
            } else {
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            // Pause/Resume Button
            IconButton(onClick = onPauseResumeRecording) {
                Icon(
                    painter = painterResource(if (isPaused) R.drawable.play else R.drawable.pause),
                    contentDescription = "Pause/Resume Recording",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Send Button
            IconButton(onClick = onSendRecording) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Recording",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


@Composable
fun ReplyUi(
    curUser: String,
    replyTo: Message,
    members: List<User?>,
    showCross: Boolean = false,
    replyType: ReplyType,
    onDiscard: (() -> Unit)? = null
) {
    val user = members.find { it?.userId == replyTo.senderId }
    val displayName = remember(user, curUser) {
        if (user?.userId == curUser) "You" else user?.name ?: "Unknown"
    }

    val replyColor = when (replyType) {
        ReplyType.YOU -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ReplyType.OTHER -> MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ReplyType.NEWMESSAGE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = replyColor,
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        replyTo.media?.let { media ->
                            Icon(
                                imageVector = when (media.mediaType) {
                                    MediaType.IMAGE -> Icons.Default.Image
                                    MediaType.VIDEO -> Icons.Default.Videocam
                                    else -> Icons.AutoMirrored.Filled.InsertDriveFile
                                },
                                contentDescription = "Media type",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    replyTo.message.ifEmpty { replyTo.media?.mediaType?.name }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (showCross) {
                    IconButton(
                        onClick = { onDiscard?.invoke() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Discard reply",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}