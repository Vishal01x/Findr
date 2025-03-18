package com.exa.android.reflekt.loopit.util.model

import com.google.firebase.Timestamp

data class User(
    val userId : String = "",
    val name : String = "",
    val phone : String = "",
    val profilePicture : String? = ""
)



data class Message(
    val senderId: String = "",
    val receiverId : String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "sent" // Status could be "sent", "delivered", or "read"
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
    val lastSeen : Timestamp? = Timestamp.now(),
    val typingTo : String = ""
)