package com.exa.android.reflekt.loopit.mvvm.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.mvvm.Repository.FirestoreService
import com.exa.android.reflekt.loopit.util.model.User
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.model.ChatList
import com.exa.android.reflekt.loopit.util.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    val curUser = MutableStateFlow("")

    init {
        viewModelScope.launch {
            // Set current user and fetch chat list
            repo.currentUser?.let { user ->
                curUser.value = user
                getChatList()
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

    fun createChatAndSendMessage(userId: String, message: String) {
        viewModelScope.launch {
            repo.createChatAndSendMessage(userId, message)
        }
    }

    fun getMessages(userId1: String, userId2: String) {
        viewModelScope.launch {
            repo.getMessages(userId1, userId2).collect { response ->
                _messages.value = response
            }
        }
    }

    fun getChatList() {
        viewModelScope.launch {
            repo.getChatList(curUser.value).collect { response ->
                _chatList.value = response
            }
        }
    }
}