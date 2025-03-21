package com.exa.android.reflekt.loopit.util.model

import com.google.firebase.Timestamp
import java.util.UUID

data class User(
    val userId : String = "",
    val name : String = "",
    val phone : String = "",
    val profilePicture : String? = ""
)




data class Message(
    val messageId: String = UUID.randomUUID().toString(),
    val chatId : String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "sent", // Status could be "sent", "delivered", or "read"
    val replyTo: Message? = null,
    val members: List<String?> = emptyList()
)



//data class User(
//    val userId: String = "",
//    val name: String = "",
//    val phone: String = "",
//    val profilePicUrl: String = ""
//)

//data class User(
//    val name: String = "",
//    val phone: String = "",
//    val profilePicUrl: String = "",
//    val users: List<String> = emptyList() // List of other user IDs with whom this user interacts
//)


data class Chat(
    val users: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val unreadMessages: Map<String, Int> = emptyMap(),
    val messages: List<Message> = emptyList()
)


data class ChatList(
    val userId : String = "",
    val name: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val profilePicture: String? = "",
    val unreadMessages : Long = 0
)

data class Status(
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val typingTo: String = ""
)