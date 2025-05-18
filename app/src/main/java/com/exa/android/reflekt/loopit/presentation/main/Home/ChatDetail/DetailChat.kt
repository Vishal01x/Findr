package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
//import android.util.Log
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
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
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.getMediaTypeFromUri
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.isFileTooLarge
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.media.mediaSelectionSheet.MediaPickerHandler
import com.exa.android.reflekt.loopit.presentation.main.Home.component.RequestNotificationPermissionIfNeeded
import com.exa.android.reflekt.loopit.presentation.main.Home.component.showLoader
import com.exa.android.reflekt.loopit.presentation.navigation.component.PhotoRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.util.clearChatNotifications
import com.exa.android.reflekt.loopit.util.model.MediaType
import com.exa.android.reflekt.loopit.util.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalFocusManager


@Composable
fun DetailChat(
    navController: NavController,
    otherUserId: String,
    onVideoCallClick: (List<String>) -> Unit
) {
    val chatViewModel: ChatViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
//    val lobbyVM: LobbyViewModel = hiltViewModel()
    val mediaSharingViewModel: MediaSharingViewModel = hiltViewModel()

    val responseChatMessages by remember { chatViewModel.messages }.collectAsState() // all the chats of cur and other User
    val curUserId by chatViewModel.curUserId.collectAsState()  // cur User Id
    val chatMessages: MutableState<List<Message>> = remember { mutableStateOf(emptyList()) }
    val responseOtherUserDetail by remember { userViewModel.userDetail }.collectAsState()
    val otherUserDetail: MutableState<User?> = remember { mutableStateOf(User()) }
    val userStatus by userViewModel.userStatus.observeAsState()
    val blockUsers by chatViewModel.blockDetails.collectAsState()
    val responseState = remember { chatViewModel.responseState.value }

    var replyMessage by remember { mutableStateOf<Message?>(null) } // to track is message replied
    var editMessage by remember { mutableStateOf<Message?>(null) } // to edit message
    var selectedMessages by remember { mutableStateOf<Set<Message>>(emptySet()) } // to track the Id's of messages selected to operate HeaderWithOptions
    var isCurUserBlocked by remember { mutableStateOf(false) }
    var isOtherUserBlocked by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val imePadding = WindowInsets.ime.asPaddingValues() // Adjusts for the keyboard
    val focusRequester = remember { FocusRequester() } // to request focus of keyboard
    val focusManager = LocalFocusManager.current // handling focus like show or not show keyboard
    val isFocused = remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isLastMessageSelected by remember { mutableStateOf(false) }
    val chatIdState = rememberUpdatedState(generateChatId(curUserId, otherUserId))
    val coroutineScope =
        rememberCoroutineScope() // to handle asynchronous here for calling viewMode.delete

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            launchMediaUpload(
                coroutineScope,
                context,
                uri,
                otherUserId,
                isCurUserBlocked,
                replyMessage,
                otherUserDetail.value?.fcmToken,
                getMediaTypeFromUri(context, uri),
                null,
                chatViewModel,
                mediaSharingViewModel
            )
        }
    }

    BackHandler(true) {
//       if (isFocused.value) {
//           focusManager.clearFocus()
//           replyMessage = null
//       } else {
        if (selectedMessages.isEmpty()) {
            navController.popBackStack()
        } else {
            selectedMessages = emptySet()
        }
    }
    //}
//    val otherUserId = otherUser.userId

    LaunchedEffect(otherUserId) {
        coroutineScope {
            launch { chatViewModel.getBlockDetails(chatIdState.value) }
            launch { userViewModel.getUserDetail(otherUserId) }
            launch { chatViewModel.getMessages(curUserId, otherUserId) }
            launch { userViewModel.observeUserStatus(otherUserId) }
            launch { userViewModel.updateUnreadMessages(curUserId, otherUserId) }
            launch { userViewModel.updateOnlineStatus(curUserId, true) }
            // launch { clearChatNotifications(context, chatIdState.value) } // if needed later
        }
        // activeChatId = generateChatId(curUserId,otherUserId)
//        clearChatNotifications(context, chatIdState.value)
    }

    when (val response = responseOtherUserDetail) {
        is Response.Loading -> {
            Text("---")
        }

        is Response.Success -> {
            // Log.d("Detail Chat", "Success in userDetail")
            otherUserDetail.value = response.data
        }

        else -> {
            // Log.d("Detail Chat", "Error in userDetail")
        }
    }

    when (val response = responseChatMessages) {
        is Response.Success -> chatMessages.value = response.data
        is Response.Error -> Text(text = response.message)
        else -> {}
    }

    when (val response = blockUsers) {
        is Response.Error -> {}
        Response.Loading -> {}
        is Response.Success -> {
            isCurUserBlocked = if (response.data.contains(curUserId)) true else false
            isOtherUserBlocked = if (response.data.contains(otherUserId)) true else false
        }
    }

    when (responseState) {
        is Response.Loading -> {
            showLoader("Please wait a moment")
        }

        is Response.Success -> {
            //showToast(context,"Successfully Done")
        }

        is Response.Error -> {
            val errorMsg = (responseState).message
            showToast(context, "Successfully Done")
        }
    }


// ✅ Lifecycle Observer: Ensures activeChatId updates when app resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    activeChatId = chatIdState.value // Update chat ID when app resumes
                    clearChatNotifications(context, chatIdState.value)
                    chatViewModel.updateMessageStatusToSeen(
                        generateChatId(curUserId, otherUserId),
                        chatMessages.value
                    )
                    //Log.d("ChatScreen", "App Resumed: ActiveChatId updated to $activeChatId")
                }

                Lifecycle.Event.ON_STOP -> {
                    activeChatId = null // Reset chat when app goes to background
                    //Log.d("ChatScreen", "App in Background: ActiveChatId Cleared")
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


    val view = LocalView.current
    val window = (view.context as Activity).window
    val insetsController = WindowCompat.getInsetsController(window, view)

    // Remember original colors to restore later
    val originalStatusBarColor = remember { window.statusBarColor }
    val originalNavigationBarColor = remember { window.navigationBarColor }

    val statusBarColor = MaterialTheme.colorScheme.tertiary.toArgb()
    val navigationBarColor = MaterialTheme.colorScheme.onTertiary.toArgb()
    val contentColor = MaterialTheme.colorScheme.onTertiary.toArgb()

    DisposableEffect(Unit) {
        // Set custom colors when entering
        window.statusBarColor = statusBarColor
        window.navigationBarColor = navigationBarColor
        insetsController.isAppearanceLightStatusBars = true // Dark icons for light color
        insetsController.isAppearanceLightNavigationBars = true // Light icons for dark background

        onDispose {
            // Restore original colors when leaving
            window.statusBarColor = originalStatusBarColor
            window.navigationBarColor = originalNavigationBarColor
            insetsController.isAppearanceLightStatusBars =
                ColorUtils.calculateLuminance(originalStatusBarColor) > 0.5

            insetsController.isAppearanceLightNavigationBars =
                ColorUtils.calculateLuminance(originalNavigationBarColor) > 0.5
        }
    }

    //if (showMediaPickerSheet) {
    MediaPickerHandler(
        showAll = true,
        onLaunch = { uri ->
            launchMediaUpload(
                context = context,
                uri = uri,
                mediaType = getMediaTypeFromUri(context, uri),
                otherUserId = otherUserId,
                isCurUserBlocked = isCurUserBlocked,
                replyTo = replyMessage,
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
                isCurUserBlocked,
                isOtherUserBlocked,
                onProfileClick = {
                    if (!isCurUserBlocked) {
                        navController.navigate(ProfileRoute.UserProfile.createRoute(otherUserId))
                    }
                },
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
                        coroutineScope,
                        isLastMessageSelected
                    )
                    isLastMessageSelected = false
                    selectedMessages = emptySet()
                },
                onClearChatClick = {
                    val chatId = generateChatId(curUserId, otherUserId)
                    chatViewModel.deleteAllMessages(chatId)

                },
                onBlockClick = {
                    val chatId = chatIdState.value
                    if (isOtherUserBlocked) {
                        chatViewModel.unblockUser(chatId, otherUserId)
                    } else {
                        chatViewModel.blockUser(chatId, otherUserId)
                        showToast(context, "User is unblocked")
                    }
                },
                onReportClick = { reason, proofText, proofImageUri ->
                    coroutineScope.launch {
                        var imageUrl: String? = null

                        if (proofImageUri != null) {
                            val file =
                                mediaSharingViewModel.createTempFileFromUri(context, proofImageUri)
                            val uploadResult = mediaSharingViewModel.uploadFileToCloudinary(file)
                            imageUrl = uploadResult?.mediaUrl
                        }

                        //  Log.d("FireStore Service", imageUrl.toString())

                        chatViewModel.sendReportToAdmin(
                            curUserId = curUserId,
                            reportedUserId = otherUserId,
                            reason = reason,
                            proofText = proofText,
                            proofImageUrl = imageUrl
                        )
                    }
                    showToast(context, "Report is successfully submitted")
                }
            )
        },
        bottomBar = {
            val otherUser = otherUserDetail.value
            if (otherUser != null) {
                otherUser.userId = otherUserId
            }
            val members = listOf(
                otherUser,
                User(userId = curUserId)
            )
            NewMessageSection(
                curUserId, members, otherUserId,
                replyMessage,
                isOtherUserBlocked,
                userViewModel, editMessage,
                focusRequester,
                onTextMessageSend = { text, replyTo ->
                    if (editMessage != null) {
                        chatViewModel.updateMessage(editMessage!!, text)
                        editMessage = null
                    } else {
                        chatViewModel.createChatAndSendMessage(
                            otherUserId,
                            isCurUserBlocked,
                            text,
                            replyTo,
                            otherUserDetail.value?.fcmToken,
                            null
                        )
                    }
                },
                onRecordingSend = { /*TODO*/ },
                onAddClick = {
//                    filePickerLauncher.launch("*/*")
                    mediaSharingViewModel.showMediaPickerSheet = true
                    //Log.d("Storage Cloudinary", "mediaPicker - $mediaSharingViewModel.showMediaPickerSheet")
                },
                onUnblockClick = {
                    chatViewModel.unblockUser(chatIdState.value, otherUserId)
                },
                onSendOrDiscard = { replyMessage = null },
                onDone = { focusManager.clearFocus() },
                onFocusChange = { isFocused.value = it.isFocused }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            val otherUser = otherUserDetail.value
            if (otherUser != null) {
                otherUser.userId = otherUserId
            }
            val members = listOf(
                otherUser,
                User(userId = curUserId)
            )
            MessageList(
                chatMessages.value,
                curUserId,
                members,
                0,
                selectedMessages,
                updateMessages = { updatedMessages, isLastMessage ->
                    selectedMessages = updatedMessages
                    isLastMessageSelected = isLastMessage
                },
                onReply = { replyMessage = it; focusRequester.requestFocus() },
                onRetry = { message ->
                    val uri = Uri.parse(message.media?.uri)
                    launchMediaUpload(
                        coroutineScope,
                        context,
                        uri,
                        otherUserId,
                        isCurUserBlocked,
                        replyMessage,
                        otherUserDetail?.value?.fcmToken,
                        getMediaTypeFromUri(context, uri),
                        message.messageId,
                        chatViewModel,
                        mediaSharingViewModel
                    )
                    //showToast(context, "Retry File Upload")
                },
                openImage = {
                    navController.navigate(PhotoRoute.ViewPhotoUsingUrl.createRoute(it))
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
    isCurUserBlocked: Boolean,
    replyTo: Message?,
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
            isCurUserBlocked = isCurUserBlocked,
            replyTo = replyTo,
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
    coroutineScope: CoroutineScope,
    isLastMessageSelected : Boolean
) {
    coroutineScope.launch {
        viewModel.deleteMessages(
            selectedMessages.map { it.messageId }, chatId,
            deleteFor
        ) {
//            emptySelectedMessages()
        }
        //viewModel.clearLastMessage(chatId)
    }
}
