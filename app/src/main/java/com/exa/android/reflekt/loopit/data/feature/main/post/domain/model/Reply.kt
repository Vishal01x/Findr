package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Domain model for a Reply to a comment
 * Can be used as embedded reply or as a document in a replies subcollection
 */
data class Reply(
    @DocumentId val replyId: String = "",
    val postId: String = "",
    val commentId: String = "",
    val parentReplyId: String? = null, // null for top-level replies; used when enabling nesting
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
) {
    /**
     * Checks if this is a nested reply
     */
    fun isNestedReply(): Boolean = parentReplyId != null
}
