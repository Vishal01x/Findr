package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get comments for a post with pagination support
 * Returns enriched comments with user information
 */
class GetCommentsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(
        postId: String,
        lastLikes: Long? = null,
        lastTimestamp: Timestamp? = null,
        limit: Long = 20
    ): Flow<Response<List<Comment>>> {
        return repository.getComments(
            postId = postId,
            lastLikes = lastLikes,
            lastTimestamp = lastTimestamp,
            limit = limit
        )
    }
}
