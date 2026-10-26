package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.core.notification.clearChatNotifications
import com.exa.android.reflekt.loopit.core.utils.CurChatManager.activeChatId
import com.exa.android.reflekt.loopit.core.utils.generateChatId
import com.exa.android.reflekt.loopit.core.utils.showToast
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.ChatViewModel
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.UserViewModel
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MediaType
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Message
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User
import com.exa.android.reflekt.loopit.data.feature.main.media.viewmodel.MediaSharingViewModel
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.media.getMediaTypeFromUri
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.media.isFileTooLarge
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.media.mediaSelectionSheet.MediaPickerHandler
import com.exa.android.reflekt.loopit.presentation.navigation.component.PhotoRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Data-orchestration layer for the chat detail screen.
 *
 * Responsibilities:
 *  - Observe ViewModels and build [ChatDetailUiState]
 *  - Handle side effects (lifecycle, status bar, media picker, back press)
 *  - Translate user events from [ChatDetailCallbacks] into ViewModel calls
 *  - Delegate all rendering to [ChatDetailContent]
 *
 * Has no UI of its own.
 */
@Composable
fun DetailChat(
    navController: NavController,
    otherUserId: String,
    eventTitle: String? = null,
    onVideoCallClick: (List<String>) -> Unit
) {
    val chatViewModel: ChatViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val mediaSharingViewModel: MediaSharingViewModel = hiltViewModel()

    // ── Collect ViewModel state ─────────────────────────────────────────────
    val curUserId by chatViewModel.curUserId.collectAsState()
    val messagesResponse by chatViewModel.messages.collectAsState()
    val userDetailMap by userViewModel.userDetail.collectAsState()
    val userStatus by userViewModel.userStatus.observeAsState()
    val blockResponse by chatViewModel.blockDetails.collectAsState()
    val messageRequestResponse by chatViewModel.messageRequest.collectAsState()

    // ── Derive clean values ─────────────────────────────────────────────────
    val messages = remember(messagesResponse) {
        (messagesResponse as? Response.Success)?.data ?: emptyList()
    }
    val isLoadingMessages = messagesResponse is Response.Loading

    val otherUser: User? = remember(userDetailMap, otherUserId) {
        val resp = userDetailMap[otherUserId]
        // Firebase doesn't always embed userId in the document; set it explicitly.
        if (resp is Response.Success) resp.data?.also { it.userId = otherUserId } else null
    }

    var isCurUserBlocked by remember { mutableStateOf(false) }
    var isOtherUserBlocked by remember { mutableStateOf(false) }
    LaunchedEffect(blockResponse) {
        if (blockResponse is Response.Success) {
            val blocked = (blockResponse as Response.Success<Set<String>>).data
            isCurUserBlocked = blocked.contains(curUserId)
            isOtherUserBlocked = blocked.contains(otherUserId)
        }
    }

    val messageRequest = remember(messageRequestResponse) {
        (messageRequestResponse as? Response.Success)?.data ?: MessageRequest()
    }

    // ── Local interaction state ─────────────────────────────────────────────
    var replyMessage by remember { mutableStateOf<Message?>(null) }
    var editMessage by remember { mutableStateOf<Message?>(null) }
    var selectedMessages by remember { mutableStateOf<Set<Message>>(emptySet()) }
    var showEventBanner by remember { mutableStateOf(eventTitle != null) }

    val chatId = remember(curUserId, otherUserId) { generateChatId(curUserId, otherUserId) }

    // ── Build unified UI state ──────────────────────────────────────────────
    val uiState = ChatDetailUiState(
        messages = messages,
        isLoadingMessages = isLoadingMessages,
        otherUser = otherUser,
        curUserId = curUserId,
        isCurUserBlocked = isCurUserBlocked,
        isOtherUserBlocked = isOtherUserBlocked,
        messageRequest = messageRequest,
        userStatus = userStatus,
        replyMessage = replyMessage,
        editMessage = editMessage,
        selectedMessages = selectedMessages,
        eventTitle = eventTitle,
        showEventBanner = showEventBanner
    )

    // ── Plumbing ────────────────────────────────────────────────────────────
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val focusRequester = remember { FocusRequester() }

    // ── Back handler ────────────────────────────────────────────────────────
    BackHandler {
        if (selectedMessages.isEmpty()) navController.popBackStack()
        else selectedMessages = emptySet()
    }

    // ── Data loading ────────────────────────────────────────────────────────
    LaunchedEffect(otherUserId, curUserId) {
        if (curUserId.isBlank()) return@LaunchedEffect
        coroutineScope {
            launch { chatViewModel.getBlockDetails(chatId) }
            launch { userViewModel.getUserDetail(otherUserId) }
            launch { chatViewModel.getMessages(curUserId, otherUserId) }
            launch { userViewModel.observeUserStatus(otherUserId) }
            launch { userViewModel.updateUnreadMessages(curUserId, otherUserId) }
            launch { userViewModel.updateOnlineStatus(curUserId, true) }
            launch { chatViewModel.observeMessageRequest(chatId) }
        }
    }

    // ── Lifecycle: activeChatId + seen status ───────────────────────────────
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    activeChatId = chatId
                    clearChatNotifications(context, chatId)
                    chatViewModel.updateMessageStatusToSeen(chatId, messages)
                }
                Lifecycle.Event.ON_STOP -> activeChatId = null
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            userViewModel.setTypingStatus(curUserId, "")
            activeChatId = null
        }
    }

    // ── Status bar icons ────────────────────────────────────────────────────
    // The bar itself is transparent (edge-to-edge); only the icon contrast is set here.
    val view = LocalView.current
    val isDarkTheme = isSystemInDarkTheme()
    DisposableEffect(isDarkTheme) {
        val window = (view.context as? android.app.Activity)?.window
            ?: return@DisposableEffect onDispose {}
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        onDispose {}
    }

    // ── Media picker (sheet) ────────────────────────────────────────────────
    MediaPickerHandler(
        showAll = true,
        onLaunch = { uri ->
            launchMediaUpload(
                scope, context, uri, otherUserId, isCurUserBlocked,
                replyMessage, otherUser?.fcmToken, getMediaTypeFromUri(context, uri),
                null, chatViewModel, mediaSharingViewModel
            )
        }
    )

    // ── Media picker (system fallback) ─────────────────────────────────────
    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            launchMediaUpload(
                scope, context, uri, otherUserId, isCurUserBlocked,
                replyMessage, otherUser?.fcmToken, getMediaTypeFromUri(context, uri),
                null, chatViewModel, mediaSharingViewModel
            )
        }
    }

    // ── Render ──────────────────────────────────────────────────────────────
    ChatDetailContent(
        uiState = uiState,
        focusRequester = focusRequester,
        callbacks = ChatDetailCallbacks(
            onBackClick = { navController.popBackStack() },
            onProfileClick = {
                if (!isCurUserBlocked)
                    navController.navigate(ProfileRoute.UserProfile.createRoute(otherUserId))
            },
            onSendMessage = { text, replyTo ->
                if (editMessage != null) {
                    chatViewModel.updateMessage(editMessage!!, text)
                    editMessage = null
                } else {
                    chatViewModel.createChatAndSendMessage(
                        otherUserId, isCurUserBlocked, text, replyTo, otherUser?.fcmToken, null
                    )
                }
            },
            onReply = { msg ->
                replyMessage = msg
                focusRequester.requestFocus()
            },
            onCancelReply = { replyMessage = null },
            onEditClick = { msg ->
                editMessage = msg
                selectedMessages = emptySet()
            },
            onCancelEdit = { editMessage = null },
            onReplyFromHeader = { msg ->
                msg?.let { replyMessage = it; focusRequester.requestFocus() }
                selectedMessages = emptySet()
            },
            onTypingStatusChange = { message ->
                if (message.isEmpty()) userViewModel.setTypingStatus(curUserId, "")
                else userViewModel.setTypingStatus(curUserId, otherUserId)
            },
            onUpdateSelection = { updatedSet, _ -> selectedMessages = updatedSet },
            onDeselectAll = { selectedMessages = emptySet() },
            onCopyMessages = {
                val textToCopy = selectedMessages.joinToString("\n") { it.message }
                selectedMessages = emptySet()
                scope.launch {
                    clipboardManager.setText(AnnotatedString(textToCopy))
                }
            },
            onDeleteMessages = { deleteFor ->
                val messagesToDelete = selectedMessages.map { it.messageId }
                selectedMessages = emptySet()
                scope.launch {
                    chatViewModel.deleteMessages(
                        messagesToDelete, chatId, deleteFor
                    ) {}
                }
            },
            onAddMedia = { mediaSharingViewModel.showMediaPickerSheet = true },
            onRetryMedia = { message ->
                val uri = Uri.parse(message.media?.uri)
                launchMediaUpload(
                    scope, context, uri, otherUserId, isCurUserBlocked,
                    replyMessage, otherUser?.fcmToken, getMediaTypeFromUri(context, uri),
                    message.messageId, chatViewModel, mediaSharingViewModel
                )
            },
            onOpenImage = { url ->
                navController.navigate(PhotoRoute.ViewPhotoUsingUrl.createRoute(url))
            },
            onReact = { messageId, emoji ->
                chatViewModel.toggleReaction(chatId, messageId, emoji)
            },
            onBlockClick = {
                if (isOtherUserBlocked) chatViewModel.unblockUser(chatId, otherUserId)
                else chatViewModel.blockUser(chatId, otherUserId)
            },
            onUnblockClick = { chatViewModel.unblockUser(chatId, otherUserId) },
            onReportClick = { reason, proofText, proofImageUri ->
                scope.launch {
                    var imageUrl: String? = null
                    if (proofImageUri != null) {
                        val file = mediaSharingViewModel.createTempFileFromUri(context, proofImageUri)
                        imageUrl = mediaSharingViewModel.uploadFileToCloudinary(file)?.mediaUrl
                    }
                    chatViewModel.sendReportToAdmin(curUserId, otherUserId, reason, proofText, imageUrl)
                }
                showToast(context, "Report submitted successfully")
            },
            onClearChat = { chatViewModel.deleteAllMessages(chatId) },
            onApproveRequest = { chatViewModel.approveMessageRequest(chatId) },
            onDeclineRequest = { chatViewModel.rejectMessageRequest(chatId) },
            onAllowMessages = { chatViewModel.reEnableMessageRequest(chatId) },
            onDismissEventBanner = { showEventBanner = false },
            onSayHi = {
                chatViewModel.createChatAndSendMessage(
                    otherUserId, isCurUserBlocked, "Hi! 👋", null, otherUser?.fcmToken, null
                )
            }
        )
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun launchMediaUpload(
    scope: CoroutineScope,
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
    scope.launch {
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
                showToast(context, mediaSharingViewModel.getUploadErrorMessage(e))
            },
            onProgress = {}
        )
    }
}
