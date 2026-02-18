package com.exa.android.reflekt.loopit.data.feature.main.chat.domain

import com.google.firebase.Timestamp
import java.util.UUID

data class User(
    var userId : String = "",
    val bio : String = "",
    val name : String = "",
    val phone : String = "",
    val profilePicture : String? = "",
    val fcmToken : String? = "",
    val isCurBlock: Boolean = false,
    val isOtherBlock : Boolean = false
)

data class Message(
    var messageId: String = UUID.randomUUID().toString(),
    val chatId : String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val messageType: Int = 3, // 2 = PreKeySignalMessage, 3 = SignalMessage (default)
    val media : Media? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "sent", // Status could be "sent", "delivered", or "read"
    val replyTo: Message? = null,
    val members: List<String?> = emptyList()
)

data class Media(
    var mediaType: MediaType = MediaType.IMAGE,
    var mediaUrl: String = "",
    val uri: String? = null,
    var uploadStatus: UploadStatus = UploadStatus.UPLOADING
)

enum class MediaType {
    IMAGE, VIDEO, AUDIO, DOCUMENT, LOCATION, CONTACT, UNKNOWN
}

enum class UploadStatus{
    SUCCESS, FAILED, UPLOADING, NOTSUPPORTED
}

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
    val unreadMessages : Long = 0,
    val fcmToken : String? = "",
    val isCurUserBlock : Boolean = false,
    val isOtherBlock: Boolean = false
)

data class Status(
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val typingTo: String = ""
)


enum class ReplyType{
    YOU, OTHER, NEWMESSAGE
}
