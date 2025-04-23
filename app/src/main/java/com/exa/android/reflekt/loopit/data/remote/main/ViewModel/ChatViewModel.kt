package com.exa.android.reflekt.loopit.data.remote.main.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.remote.main.Repository.FirestoreService
import com.exa.android.reflekt.loopit.util.model.User
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.ChatList
import com.exa.android.reflekt.loopit.util.model.Media
import com.exa.android.reflekt.loopit.util.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: FirestoreService
) : ViewModel() {

    private val _searchResult = MutableStateFlow<Response<User?>>(Response.Loading)
    val searchResult: StateFlow<Response<User?>> = _searchResult

    private val _messages = MutableStateFlow<Response<List<Message>>>(Response.Loading)
    val messages: StateFlow<Response<List<Message>>> = _messages

    private val _chatList = MutableStateFlow<Response<List<ChatList>>>(Response.Loading)
    val chatList: StateFlow<Response<List<ChatList>>> = _chatList

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

    fun insertUser(userName: String, phone: String) {
        viewModelScope.launch {
            repo.insertUser(userName, phone)
        }
    }

    fun searchUser(phone: String) {
        viewModelScope.launch {
            repo.searchUser(phone).collect { response ->
                _searchResult.value = response
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
        message: String,
        receiverToken: String?,
        media: Media? = null,
        messageId: String? = null
    ){
        viewModelScope.launch {
            val id = repo.createChatAndSendMessage(
                userId,
                message,
                media,
                receiverToken,
                curUser.value,
                messageId
            )
            messageIdFlow.emit(id)
        }
    }



    fun getMessages(userId1: String, userId2: String) {
        viewModelScope.launch {
            repo.getMessages(userId1, userId2).collect { response ->
                _messages.value = response
            }
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


    fun getChatList() {
        viewModelScope.launch {
            repo.getChatList(curUserId.value)
                .flowOn(Dispatchers.IO)
                .distinctUntilChanged()
                .collect { response ->
                    _chatList.value = response
                }
        }
    }

    fun updateMessage(message: Message, newText: String) {
        viewModelScope.launch {
            repo.updateMessage(message, newText)
        }
    }

    fun updateMediaMessage(messageId: String, otherUserId: String, media: Media){
        viewModelScope.launch {
            repo.updateMediaMessage(messageId, otherUserId, media)
        }
    }

}