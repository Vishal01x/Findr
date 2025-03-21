package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.mvvm.ViewModel.ChatViewModel
import com.exa.android.reflekt.loopit.mvvm.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.ChatHeader
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.MessageList
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.NewMessageSection
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.generateChatId
import com.exa.android.reflekt.loopit.util.model.Message
import com.exa.android.reflekt.loopit.util.model.User
import io.getstream.meeting.room.compose.ui.lobby.LobbyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun DetailChat(
    navController: NavController,
    otherUserId: String,
    onVideoCallClick: (List<String>) -> Unit
) {
    val chatViewModel: ChatViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val lobbyVM: LobbyViewModel = hiltViewModel()

    val responseChatMessages by remember { chatViewModel.messages }.collectAsState() // all the chats of cur and other User
    val curUserId by chatViewModel.curUser.collectAsState()  // cur User Id
    val chatMessages: MutableState<List<Message>> = remember { mutableStateOf(emptyList()) }
    val responseUserDetail by userViewModel.userDetail.collectAsState()
    val userDetail: MutableState<User?> = remember { mutableStateOf(User()) }
    val userStatus by userViewModel.userStatus.observeAsState()

    var replyMessage by remember { mutableStateOf<Message?>(null) } // to track is message replied
    var editMessage by remember { mutableStateOf<Message?>(null) } // to edit message
    var selectedMessages by remember { mutableStateOf<Set<Message>>(emptySet()) } // to track the Id's of messages selected to operate HeaderWithOptions
    val focusRequester = remember { FocusRequester() } // to request focus of keyboard
    val focusManager = LocalFocusManager.current // handling focus like show or not show keyboard
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope =
        rememberCoroutineScope() // to handle asynchronous here for calling viewMode.delete

    LaunchedEffect(otherUserId) {
        userViewModel.observeUserStatus(otherUserId)
        chatViewModel.getMessages(curUserId, otherUserId)
        userViewModel.updateUnreadMessages(curUserId, otherUserId)
        userViewModel.getUserDetail(otherUserId)
        userViewModel.updateOnlineStatus(curUserId, true)
    }

    when (val response = responseUserDetail) {
        is Response.Loading -> {
            Text("---")
        }

        is Response.Success -> {
            Log.d("Detail Chat", "Success in userDetail")
            userDetail.value = response.data
        }

        else -> {
            Log.d("Detail Chat", "Error in userDetail")
        }
    }

    when (val response = responseChatMessages) {
        is Response.Success -> chatMessages.value = response.data
        is Response.Error -> Text(text = response.message)
        else -> {}
    }

    DisposableEffect(key1 = Unit) {// when the user while typing navigate to somewhere eelse then update its typingto null
        onDispose {
            userViewModel.setTypingStatus(curUserId, "")
        }
    }

    Scaffold(
        topBar = {
            ChatHeader(
                userDetail.value, userStatus, curUserId, selectedMessages,
                onProfileClick = { },
                onBackClick = { navController.popBackStack() },
                onVoiceCallClick = { },
                onVideoCallClick = { },
                onUnselectClick = { selectedMessages = emptySet() },
                onCopyClick = {
                    copyMessages(selectedMessages, clipboardManager, coroutineScope)
                    selectedMessages = emptySet()
                },
                onEditClick = {
                    message->
                    editMessage = message
                    selectedMessages = emptySet()
                },
                onForwardClick = {},
                onDeleteClick = { deleteFor ->
                    deleteMessages(
                        chatViewModel,
                        selectedMessages,
                        generateChatId(curUserId, otherUserId),
                        deleteFor,
                        coroutineScope
                    )
                    selectedMessages = emptySet()
                }
            )
        },
        bottomBar = {
            NewMessageSection(
                curUserId, otherUserId,
                userViewModel, editMessage,
                focusRequester,
                onTextMessageSend = { text ->
                    if(editMessage != null){
                        chatViewModel.updateMessage(editMessage!!, text)
                        editMessage = null
                    }else {
                        chatViewModel.createChatAndSendMessage(
                            otherUserId,
                            text
                        )
                    }
                },
                onRecordingSend = { /*TODO*/ }
            ) {

            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            MessageList(
                chatMessages.value,
                curUserId,
                0,
                selectedMessages,
                updateMessages = { selectedMessages = it },
                onReply = { replyMessage = it; focusRequester.requestFocus() }
            )
        }
    }
}

//data class Message(val isSentByCurrentUser : Boolean, val message : String)
//@Preview(showBackground = true)
//@Composable
//fun PreviewDashedCircle() {
//    DetailChat()
//}

private fun copyMessages(
    selectedMessages: Set<Message>,
    clipboardManager: ClipboardManager,
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch {
        val formattedMessages = selectedMessages.joinToString("\n") {
            "[${
                SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(
                    Date(it.timestamp.seconds * 1000L)
                )
            }] ${it.senderId}: ${it.message}"
        }
        clipboardManager.setText(AnnotatedString(formattedMessages))
    }
}
//
//private fun forwardMessages(selectedMessages: Set<Message>, navController: NavController) {
//    val messagesText = selectedMessages.map { it.message }
//    val jsonString = Gson().toJson(messagesText)
//    navController.navigate(HomeRoute.AllUserScreen.createRoute(ScreenPurpose.FORWARD_MESSAGES, jsonString))
//    // navigate to forward screen where we have all users
//}

private fun deleteMessages(
    viewModel: ChatViewModel,
    selectedMessages: Set<Message>,
    chatId: String,
    deleteFor: Int,
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch {
        viewModel.deleteMessages(
            selectedMessages.map { it.messageId }, chatId,
            deleteFor
        ) {
//            emptySelectedMessages()
        }
    }
}
