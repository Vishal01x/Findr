package com.exa.android.reflekt.model

data class ChatUser(
    val id: String,
    val name: String,
    val imageUrl: String,
    val lastMessage: String,
    val unreadCount: Int,
    val isOnline: Boolean,
    val lastSeen: String
)

data class Message(
    val id: String,
    val text: String,
    val senderId: String,
    val timestamp: String,
    val isSent: Boolean // true if sent by current user
)