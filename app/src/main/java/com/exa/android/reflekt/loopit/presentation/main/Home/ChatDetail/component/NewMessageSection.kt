package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.util.AudioWaveForm
import com.exa.android.reflekt.loopit.util.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@Composable
fun NewMessageSection(
    curUser: String,
    typingTo: String,
    viewModel: UserViewModel,
    editMessage: Message?,
    focusRequester: FocusRequester,
    onTextMessageSend: (String) -> Unit,
    onRecordingSend: () -> Unit,
    onAddClick: () -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf("00:00") }
    var elapsedSeconds by remember { mutableStateOf(0) } // Tracks total elapsed seconds
    var timerJob by remember { mutableStateOf<Job?>(null) }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(1000L)
                elapsedSeconds++
                recordingTime =
                    String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
    }

    fun resetTimer() {
        isRecording = false
        isPaused = false
        recordingTime = "00:00"
        elapsedSeconds = 0
        timerJob?.cancel()
    }

    // Timer logic
    LaunchedEffect(isRecording, isPaused) {
        if (isRecording) {
            if (!isPaused) {
                startTimer()
            } else {
                pauseTimer()
            }
        } else {
            resetTimer()
        }
    }

    if (!isRecording) {
        // Text Input UI
        SendTFMessage(
            editMessage,
            focusRequester,
            onSendClick = { message -> onTextMessageSend(message) },
            onAddClick = onAddClick,
            onMicClick = { isRecording = true },
            onTyping = { message ->
                if (message.isEmpty())
                    viewModel.setTypingStatus(curUser, "")
                else if (message.isNotEmpty())
                    viewModel.setTypingStatus(curUser, typingTo)
            }
        )
    } else {
        // Audio Recording UI
        SendAudioMessage(
            isPaused = isPaused,
            recordingTime = recordingTime,
            onDeleteRecording = {
                resetTimer()
            },
            onPauseResumeRecording = {
                isPaused = !isPaused
            },
            onSendRecording = {
                onRecordingSend()
                resetTimer()
            }
        )
    }
}


@Composable
fun SendTFMessage(
    editMessage: Message?,
    focusRequester: FocusRequester,
    onSendClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onMicClick: () -> Unit,
    onTyping: (msg: String) -> Unit
) {
//    var message by remember { mutableStateOf("") }
//
//    LaunchedEffect(editMessage) {  // Update text field when editMessage changes
//        message = editMessage?.message ?: ""
//        if(editMessage != null)focusRequester.requestFocus()
//    }

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

    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(Color.White),
        shape = RectangleShape,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add Button
            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEFEFEF)) // Light grey background
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (textFieldValue.text.isEmpty()) {
                    Text(
                        text = "Type a Message",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue =
                            it.copy(selection = TextRange(it.text.length)) // Keep cursor at end
                        onTyping(it.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val text = textFieldValue.text.trim()
                            if (text.isNotEmpty())
                                onSendClick(text)
                            textFieldValue = TextFieldValue("") // Clear text after sending
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Microphone or Send Button
            IconButton(
                onClick = {
                    if (textFieldValue.text.isNotEmpty()) {
                        onSendClick(textFieldValue.text)
                        textFieldValue = TextFieldValue("")
                        onTyping("")
                    } else {
                        onMicClick()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(if (textFieldValue.text.isEmpty()) R.drawable.microphone else R.drawable.send),
                    contentDescription = "Send or Mic",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SendAudioMessage(
//    isRecording: Boolean = true,
    isPaused: Boolean,
    recordingTime: String,
    onDeleteRecording: () -> Unit,
    onPauseResumeRecording: () -> Unit,
    onSendRecording: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(Color.White),
        shape = RectangleShape
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
                    tint = Color.Black
                )
            }

            // Timer
            Text(
                text = recordingTime,
                color = Color.Black,
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
                        .background(Color.Gray)
                )
            }
            // Pause/Resume Button
            IconButton(onClick = onPauseResumeRecording) {
                Icon(
                    painter = painterResource(if (isPaused) R.drawable.play else R.drawable.pause),
                    contentDescription = "Pause/Resume Recording",
                    tint = Color.Black
                )
            }

            // Send Button
            IconButton(onClick = onSendRecording) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Recording",
                    tint = Color.Black
                )
            }
        }
    }
}