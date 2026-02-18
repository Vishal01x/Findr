package com.exa.android.reflekt.loopit.core.utils

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment


fun List<Comment>.updateRepliesRecursively(
    targetId: String,
    newReplies: List<Comment>
): List<Comment> {
    return map { comment ->
        when {
            comment.id == targetId -> {
                comment.copy(replies = newReplies)
            }
            comment.replies.isNotEmpty() -> {
                comment.copy(
                    replies = comment.replies.updateRepliesRecursively(
                        targetId,
                        newReplies
                    )
                )
            }
            else -> comment
        }
    }
}
fun List<Comment>.collectDescendantIds(
    parentId: String
): Set<String> {

    fun collectAllIds(children: List<Comment>): Set<String> {
        val ids = mutableSetOf<String>()
        children.forEach { child ->
            ids.add(child.id)
            ids.addAll(collectAllIds(child.replies))
        }
        return ids
    }

    fun find(commentList: List<Comment>): Set<String> {
        for (comment in commentList) {
            if (comment.id == parentId) {
                return collectAllIds(comment.replies)
            }
            val result = find(comment.replies)
            if (result.isNotEmpty()) return result
        }
        return emptySet()
    }

    return find(this)
}
