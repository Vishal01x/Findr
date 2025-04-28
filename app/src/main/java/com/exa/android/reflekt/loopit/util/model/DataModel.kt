package com.exa.android.reflekt.loopit.util.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

data class User(
    var userId : String = "",
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



data class Project(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val rolesNeeded: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    val createdBy: String = "", // User ID
    val createdByName: String = "",
    val enrolledPersons: Map<String, String> = emptyMap(),
    val requestedPersons: Map<String, String> = emptyMap(),
    val type: String = PostType.PROJECT.displayName,
    val imageUrls: List<String> = emptyList(),
    val links: List<String> = emptyList(),
    val likes: List<String> = emptyList(),
    val comments: List<Comment> = emptyList()
) {
    companion object {
        const val FIELD_TITLE = "title"
        const val FIELD_CREATED_AT = "createdAt"
    }
}


enum class PostType(
    val displayName: String,
    val icon: ImageVector
) {
    PROJECT("Project", Icons.Outlined.Work),
    COLLABORATION("Collaboration", Icons.Outlined.Group),
    JOB("Job Opportunity", Icons.Outlined.BusinessCenter),
    EVENT("Event", Icons.Outlined.Event),
    OTHER("Other", Icons.Outlined.MoreHoriz)
}

data class Comment(
    val id: String,
    val text: String,
    val senderId: String,
    val timestamp: Timestamp = Timestamp.now()
)

enum class ReplyType{
    YOU, OTHER, NEWMESSAGE
}