package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

/**
 * A single option within a Post's poll.
 * [votes] holds the userIds who voted for this option (mirrors how [Post.likes] tracks likers).
 */
data class PollOption(
    val text: String = "",
    val votes: List<String> = emptyList()
) {
    fun isVotedBy(userId: String): Boolean = votes.contains(userId)
}
