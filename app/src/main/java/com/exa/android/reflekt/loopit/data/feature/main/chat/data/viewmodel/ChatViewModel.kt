package com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.ChatRepository
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.ChatList
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Media
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Message
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository
) : ViewModel() {

    private val _searchResult = MutableStateFlow<Response<User?>>(Response.Loading)
    val searchResult: StateFlow<Response<User?>> = _searchResult

    private val _messages = MutableStateFlow<Response<List<Message>>>(Response.Loading)
    val messages: StateFlow<Response<List<Message>>> = _messages

    private val _chatList = MutableStateFlow<Response<List<ChatList>>>(Response.Loading)
    val chatList: StateFlow<Response<List<ChatList>>> = _chatList

    private val _blockDetails = MutableStateFlow<Response<Set<String>>>(Response.Loading)
    val blockDetails: StateFlow<Response<Set<String>>> = _blockDetails.asStateFlow()

    private val _responseState = mutableStateOf<Response<Boolean>>(Response.Success(false))
    val responseState: State<Response<Boolean>> = _responseState

    private val _unreadCount = mutableStateOf(0)
    val unreadCount: State<Int> = _unreadCount

    private val _pendingRequestCount = mutableStateOf(0)
    val pendingRequestCount: State<Int> = _pendingRequestCount

    val curUserId = MutableStateFlow("")
    val curUser = MutableStateFlow<User?>(null)


    init {
        viewModelScope.launch {
            // Set current user and fetch chat list
            repo.currentUser?.let { user ->
                curUserId.value = user
                getChatList()
                getCurUser()
                repo.registerFCMToken()
            } ?: run {
                _chatList.value = Response.Error("Current user is null")
            }
        }
    }

    private fun getCurUser() {
        viewModelScope.launch {
            curUser.value = repo.getCurUser()
        }
    }

    val messageIdFlow = MutableSharedFlow<String?>()

    fun createChatAndSendMessage(
        userId: String,
        isCurUserBlocked: Boolean,
        message: String,
        replyTo : Message?,
        receiverToken: String?,
        media: Media? = null,
        messageId: String? = null
    ) {
        viewModelScope.launch {
            val id = repo.createChatAndSendMessage(
                userId,
                isCurUserBlocked,
                message,
                replyTo,
                media,
                if (isCurUserBlocked) null else receiverToken,
                curUser.value,
                messageId
            )
            messageIdFlow.emit(id)
        }
    }

    fun updateMessageStatusToSeen(chatId: String, message: List<Message>) {
        viewModelScope.launch {
            repo.updateMessageStatusToSeen(chatId, message)
        }
    }


    fun deleteMessages(
        messages: List<String>,
        chatId: String,
        deleteFor: Int,
        onCleared: () -> Unit
    ) {
        viewModelScope.launch {
            repo.deleteMessages(messages, chatId, deleteFor) {
                onCleared()
            }

        }
    }

    fun clearLastMessage(chatId: String){
        viewModelScope.launch {
            repo.clearLastMessage(chatId)
        }
    }


    fun deleteAllMessages(
        chatId: String
    ) {
        viewModelScope.launch {
            repo.deleteAllMessages(
                chatId
            )
        }
    }


    fun getChatList(forceRefresh: Boolean = false) {
        if (!forceRefresh && _chatList.value is Response.Success) {
            return
        }
        viewModelScope.launch {
            repo.getChatList(curUserId.value)
                .flowOn(Dispatchers.IO)
                .distinctUntilChanged()
                .collect { response ->
                    _chatList.value = response

                    // Extract and update unread count from chat list
                    if (response is Response.Success) {
                        val totalUnreadChats = response.data.count { it.unreadMessages > 0 }
                        _unreadCount.value = totalUnreadChats

                        val totalPendingRequests = response.data.count { chat ->
                            chat.requestStatus == "pending" &&
                            chat.requestSenderId != curUserId.value &&
                            chat.requestSenderId.isNotEmpty()
                        }
                        _pendingRequestCount.value = totalPendingRequests
                    }

                }


        }
    }

    fun getMessages(userId1: String, userId2: String) {
        viewModelScope.launch {
            repo.getMessages(userId1, userId2).collect { response ->
                _messages.value = response
            }
        }
    }

    fun updateMessage(message: Message, newText: String) {
        viewModelScope.launch {
            repo.updateMessage(message, newText)
        }
    }

    fun toggleReaction(chatId: String, messageId: String, emoji: String) {
        val userId = curUserId.value.ifEmpty { return }
        viewModelScope.launch {
            repo.toggleReaction(chatId, messageId, userId, emoji)
        }
    }

    fun updateMediaMessage(messageId: String, otherUserId: String, media: Media) {
        viewModelScope.launch {
            repo.updateMediaMessage(messageId, otherUserId, media)
        }
    }

    fun blockUser(chatId: String, userId: String) {
        viewModelScope.launch {
            _responseState.value = Response.Loading
            val res = repo.blockUser(chatId, userId)
            _responseState.value = res
        }
    }

    fun unblockUser(chatId: String, userId: String) {
        viewModelScope.launch {
            _responseState.value = Response.Loading
            val res = repo.unblockUser(chatId, userId)
            _responseState.value = res
        }
    }

    fun sendReportToAdmin(
        curUserId: String,
        reportedUserId: String,
        reason: String,
        proofText: String?,
        proofImageUrl: String?
    ) {
        viewModelScope.launch {
            _responseState.value = Response.Loading
            val res =
                repo.sendReportToAdmin(curUserId, reportedUserId, reason, proofText, proofImageUrl)
            _responseState.value = res
        }
    }


    fun getBlockDetails(chatId: String) {
        viewModelScope.launch {
            repo.getBlockDetails(chatId).collect {
                _blockDetails.value = it
            }
        }
    }

    // ── Message Request ────────────────────────────────────────────────────

    private val _messageRequest = MutableStateFlow<Response<MessageRequest>>(Response.Loading)
    val messageRequest: StateFlow<Response<MessageRequest>> = _messageRequest.asStateFlow()

    fun observeMessageRequest(chatId: String) {
        viewModelScope.launch {
            repo.observeMessageRequest(chatId).collect { _messageRequest.value = it }
        }
    }

    fun approveMessageRequest(chatId: String) {
        viewModelScope.launch { repo.approveMessageRequest(chatId) }
    }

    fun rejectMessageRequest(chatId: String) {
        viewModelScope.launch { repo.rejectMessageRequest(chatId) }
    }

    fun reEnableMessageRequest(chatId: String) {
        viewModelScope.launch { repo.reEnableMessageRequest(chatId) }
    }
}