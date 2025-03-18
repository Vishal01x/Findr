package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalConfiguration
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.mvvm.ViewModel.ChatViewModel
import com.exa.android.reflekt.loopit.mvvm.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.navigation.component.ChatInfo
import com.exa.android.reflekt.loopit.util.AudioWaveForm
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.formatTimestamp
import com.exa.android.reflekt.loopit.util.model.Message
import com.exa.android.reflekt.loopit.util.model.Status
import com.exa.android.reflekt.loopit.util.model.User


@Composable
fun DetailChat(navController: NavController, otherUserId: String, onVideoCallClick: () -> Unit) {
    val viewModel: ChatViewModel = hiltViewModel()
    val userVM : UserViewModel = hiltViewModel()
    val chatMessages by viewModel.messages.collectAsState()
    val curUserId by viewModel.curUser.collectAsState()
    val responseUserDetail by userVM.userDetail.collectAsState()
    val userDetail : MutableState<User?> =  remember{ mutableStateOf(User()) }
    val userStatus by userVM.userStatus.observeAsState()

    LaunchedEffect(otherUserId) {
        userVM.observeUserStatus(otherUserId)
        viewModel.getMessages(curUserId, otherUserId)
        userVM.updateUnreadMessages(curUserId, otherUserId)
        userVM.updateOnlineStatus(curUserId,true)
    }


    when(val response = responseUserDetail){
        is Response.Loading -> {}
        is Response.Success -> {
            Log.d("Detail Chat", "Success in userDetail")
            userDetail.value = response.data
        }
        else -> {
            Log.d("Detail Chat", "Error in userDetail")
        }
    }
    DisposableEffect(key1 = Unit) {// when the user while typing navigate to somewhere eelse then update its typingto null
        onDispose {
            userVM.setTypingStatus(curUserId,"")
        }
    }
    // Handle keyboard visibility
//    val insets = LocalWindowInfo.current
//    val isKeyboardVisible = insets.isWindowFocused

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
        // .windowInsetsPadding(WindowInsets(bottom = if (isKeyboardVisible) 0.dp else 80.dp))
    ) {
        // Chat Header (remains fixed)
        userDetail.value?.let {
            ChatHeader(
                it,
                userStatus!!,
                curUserId,
                onProfileClick = { navController.navigate(ChatInfo.ProfileScreen.route) },
                onBackClick = { navController.popBackStack() },
                onCallClick = { /*TODO*/ },
                onVideoCallClick = onVideoCallClick
            )
        }

        // Chat Messages (LazyColumn) in the center
        Box(modifier = Modifier.weight(1f)) {
            when (val response = chatMessages) {
                is Response.Loading -> {
                    Text(text = "Messages Rendering")
                }

                is Response.Success -> {
                    MessageList(response.data, curUserId)
                }

                is Response.Error -> {
                    Text(text = response.message)
                }
            }
        }

        // New message section at the bottom (adjusts when the keyboard is visible)
        userDetail.value?.userId?.let {
            NewMessageSection(
                curUserId, it,
                userVM,
                onTextMessageSend = { text -> viewModel.createChatAndSendMessage(otherUserId, text) },
                onRecordingSend = { /*TODO*/ }
            ) {

            }
        }
    }
}

@Composable
fun ChatHeader(
//    profilePictureUrl: String,
//    userName: String,
//    userStatus: String,
    user : User,
    status : Status,
    curUser: String,
    onProfileClick: () -> Unit,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(Color.White),
        shape = RectangleShape,
        modifier = Modifier.clickable { onProfileClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.chat_img3),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Black, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(8.dp))

            // User Name and Status
            Column {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = when {
                        status.typingTo == curUser -> "typing..."
                        status.isOnline -> "Online"
                        status.lastSeen != null -> {
                            val timestamp = status.lastSeen.seconds*1000L
                            val time = formatTimestamp(timestamp)
                            "last seen at ${time}"
                        }
                        else -> {"Online"}
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Video Call Icon
            IconButton(onClick = onVideoCallClick) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Video Call",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Call Icon
            IconButton(onClick = onCallClick) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun MessageList(messages: List<Message>, curUser: String) {
    LazyColumn {
        items(messages) { message ->
            MessageBubble(message, curUser)
        }
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


@Composable
fun NewMessageSection(
    curUser: String,
    typingTo : String,
    viewModel: UserViewModel,
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
            onSendClick = { message -> onTextMessageSend(message) },
            onAddClick = onAddClick,
            onMicClick = { isRecording = true },
            onTyping = {message->
                if(message.isEmpty())
                    viewModel.setTypingStatus(curUser, "")
                else if(message.isNotEmpty())
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
    onSendClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onMicClick: () -> Unit,
    onTyping : (msg : String) -> Unit
) {
    var message by remember { mutableStateOf("") }
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
                if (message.isEmpty()) {
                    Text(
                        text = "Type a Message",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                BasicTextField(
                    value = message,
                    onValueChange = { message = it; onTyping(message) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    maxLines = 4
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Microphone or Send Button
            IconButton(
                onClick = {
                    if (message.isNotEmpty()) {
                        onSendClick(message)
                        message = ""
                        onTyping(message)
                    } else {
                        onMicClick()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(if (message.isEmpty()) R.drawable.microphone else R.drawable.send),
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

//data class Message(val isSentByCurrentUser : Boolean, val message : String)
//@Preview(showBackground = true)
//@Composable
//fun PreviewDashedCircle() {
//    DetailChat()
//}