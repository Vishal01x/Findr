package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get replies for a specific comment with pagination support
 * Returns enriched replies with user information
 */
class GetRepliesUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(
        postId: String,
        rootCommentId : String?,
        parentCommentId: String,
        lastLikes: Long? = null,
        lastTimestamp: Timestamp? = null,
        limit: Long = 20
    ): Flow<Response<List<Comment>>> {
        return repository.getReplies(
            postId = postId,
            rootCommentId = rootCommentId,
            parentCommentId = parentCommentId,
            lastLikes = lastLikes,
            lastTimestamp = lastTimestamp,
            limit = limit
        )
    }
}
