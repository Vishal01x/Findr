package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ChatViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.MediaSharingViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.ChatHeader
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.MessageList
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.NewMessageSection
import com.exa.android.reflekt.loopit.util.CurChatManager.activeChatId
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.generateChatId
import com.exa.android.reflekt.loopit.util.model.Message
import com.exa.android.reflekt.loopit.util.model.User
import com.exa.android.reflekt.loopit.data.remote.main.meeting.lobby.LobbyViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.getMediaTypeFromUri
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.isFileTooLarge
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.mediaSelectionSheet.MediaPickerHandler
import com.exa.android.reflekt.loopit.util.clearChatNotifications
import com.exa.android.reflekt.loopit.util.model.MediaType
import com.exa.android.reflekt.loopit.util.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun DetailChat(
    navController: NavController,
    otherUserId: String,
    onVideoCallClick: (List<String>) -> Unit
) {
    val chatViewModel: ChatViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val lobbyVM: LobbyViewModel = hiltViewModel()
    val mediaSharingViewModel: MediaSharingViewModel = hiltViewModel()

    val responseChatMessages by remember { chatViewModel.messages }.collectAsState() // all the chats of cur and other User
    val curUserId by chatViewModel.curUserId.collectAsState()  // cur User Id
    val chatMessages: MutableState<List<Message>> = remember { mutableStateOf(emptyList()) }
    val responseOtherUserDetail by remember { userViewModel.userDetail }.collectAsState()
    val otherUserDetail: MutableState<User?> = remember { mutableStateOf(User()) }
    val userStatus by userViewModel.userStatus.observeAsState()

    var replyMessage by remember { mutableStateOf<Message?>(null) } // to track is message replied
    var editMessage by remember { mutableStateOf<Message?>(null) } // to edit message
    var selectedMessages by remember { mutableStateOf<Set<Message>>(emptySet()) } // to track the Id's of messages selected to operate HeaderWithOptions
    val keyboardController = LocalSoftwareKeyboardController.current
    val imePadding = WindowInsets.ime.asPaddingValues() // Adjusts for the keyboard
    val focusRequester = remember { FocusRequester() } // to request focus of keyboard
    val focusManager = LocalFocusManager.current // handling focus like show or not show keyboard
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val chatIdState = rememberUpdatedState(generateChatId(curUserId, otherUserId))
    val coroutineScope =
        rememberCoroutineScope() // to handle asynchronous here for calling viewMode.delete

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            launchMediaUpload(
                coroutineScope, context, uri,
                otherUserId, otherUserDetail.value?.fcmToken, getMediaTypeFromUri(context, uri),
                null, chatViewModel, mediaSharingViewModel
            )
        }
    }

    BackHandler(true) {
        if (selectedMessages.isEmpty())
            navController.popBackStack()
        else selectedMessages = emptySet()
    }
//    val otherUserId = otherUser.userId

    LaunchedEffect(otherUserId) {
        userViewModel.observeUserStatus(otherUserId)
        chatViewModel.getMessages(curUserId, otherUserId)
        userViewModel.updateUnreadMessages(curUserId, otherUserId)
        userViewModel.getUserDetail(otherUserId)
        userViewModel.updateOnlineStatus(curUserId, true)
        // activeChatId = generateChatId(curUserId,otherUserId)
        clearChatNotifications(context, chatIdState.value)
    }

    when (val response = responseOtherUserDetail) {
        is Response.Loading -> {
            Text("---")
        }

        is Response.Success -> {
            Log.d("Detail Chat", "Success in userDetail")
            otherUserDetail.value = response.data
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
//
//    DisposableEffect(key1 = Unit) {// when the user while typing navigate to somewhere eelse then update its typingto null
//        onDispose {
//            userViewModel.setTypingStatus(curUserId, "")
//            activeChatId = null
//        }
//    }


// ✅ Lifecycle Observer: Ensures activeChatId updates when app resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    activeChatId = chatIdState.value // Update chat ID when app resumes
                    Log.d("ChatScreen", "App Resumed: ActiveChatId updated to $activeChatId")
                }

                Lifecycle.Event.ON_STOP -> {
                    activeChatId = null // Reset chat when app goes to background
                    Log.d("ChatScreen", "App in Background: ActiveChatId Cleared")
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            userViewModel.setTypingStatus(curUserId, "")
            activeChatId = null // Reset when leaving chat screen

            //keyboardController?.hide()
        }
    }

    //if (showMediaPickerSheet) {
        MediaPickerHandler(
            showAll = true,
            onLaunch = {uri->
                launchMediaUpload(
                    context = context,
                    uri = uri,
                    mediaType = MediaType.IMAGE,
                    otherUserId = otherUserId,
                    fcmToken = otherUserDetail.value?.fcmToken,
                    chatViewModel = chatViewModel,
                    coroutineScope = coroutineScope,
                    messageId = null,
                    mediaSharingViewModel = mediaSharingViewModel
                )
            }
        )


    Scaffold(
        topBar = {
            ChatHeader(
                otherUserDetail.value, userStatus, curUserId, selectedMessages,
                onProfileClick = { },
                onBackClick = { navController.popBackStack() },
                onVoiceCallClick = { },
                onVideoCallClick = { },
                onUnselectClick = { selectedMessages = emptySet() },
                onCopyClick = {
                    copyMessages(selectedMessages, clipboardManager, coroutineScope)
                    selectedMessages = emptySet()
                },
                onEditClick = { message ->
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
                    if (editMessage != null) {
                        chatViewModel.updateMessage(editMessage!!, text)
                        editMessage = null
                    } else {
                        chatViewModel.createChatAndSendMessage(
                            otherUserId,
                            text,
                            otherUserDetail.value?.fcmToken,
                            null
                        )
                    }
                },
                onRecordingSend = { /*TODO*/ },
                onAddClick = {
//                    filePickerLauncher.launch("*/*")
                    mediaSharingViewModel.showMediaPickerSheet = true
                    Log.d("Storage Cloudinary", "mediaPicker - $mediaSharingViewModel.showMediaPickerSheet")
                }

            )
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
                onReply = { replyMessage = it; focusRequester.requestFocus() },
                onRetry = { message ->
                    val uri = Uri.parse(message.media?.uri)
                    launchMediaUpload(
                        coroutineScope,
                        context,
                        uri,
                        otherUserId,
                        otherUserDetail?.value?.fcmToken,
                        getMediaTypeFromUri(context, uri),
                        message.messageId,
                        chatViewModel,
                        mediaSharingViewModel
                    )
                    //showToast(context, "Retry File Upload")
                }

            )
        }
    }
}

fun launchMediaUpload(
    coroutineScope: CoroutineScope,
    context: Context,
    uri: Uri?,
    otherUserId: String,
    fcmToken: String?,
    mediaType: MediaType,
    messageId: String? = null,
    chatViewModel: ChatViewModel,
    mediaSharingViewModel: MediaSharingViewModel
) {

    if (uri != null && isFileTooLarge(context, uri)) {
        showToast(context, "File too large. Maximum allowed size is 10MB.")
        return
    }

    coroutineScope.launch {
        mediaSharingViewModel.uploadAndSendMediaMessage(
            context = context,
            uri = uri,
            otherUserId = otherUserId,
            fcmToken = fcmToken,
            mediaType = mediaType,
            chatViewModel = chatViewModel,
            messageId = messageId,
            onError = { e ->
                val errorMessage = mediaSharingViewModel.getUploadErrorMessage(e)
                showToast(context, errorMessage)
            },
            onProgress = {
                // Optional: Handle UI progress updates
            }
        )
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
            it.message
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
