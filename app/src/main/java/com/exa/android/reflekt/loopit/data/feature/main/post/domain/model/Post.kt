package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Post(
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
    BUG("Bug Fix", Icons.Outlined.Error),
    COLLABORATION("Collab", Icons.Outlined.Group),
    POST("Post", Icons.Outlined.Article),
    OTHER("Other", Icons.Outlined.MoreHoriz)
}

data class Comment(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

enum class ReplyType{
    YOU, OTHER, NEWMESSAGE
}