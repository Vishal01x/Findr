package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser

/**
 * Lightweight user information for display in posts and comments
 * Extracted from full profileUser to avoid coupling UI to entire user profile
 */
data class PostUserInfo(
    val uid: String = "",
    val name: String = "",
    val username: String = "", // Derived from name or email if not available
    val avatarUrl: String? = null,
    val bio: String? = null,
    val branch: String? = null,
    val year: String? = null,
    val section: String? = null,
    val isVerified: Boolean = false // Can be extended later
)

/**
 * Extension function to convert profileUser to PostUserInfo
 */
fun profileUser.toPostUserInfo(): PostUserInfo {
    return PostUserInfo(
        uid = this.uid,
        name = this.name,
        username = generateUsername(this.name, this.email),
        avatarUrl = this.profilePicture.ifEmpty { null },
        bio = this.headline?.ifBlank { null },
        branch = this.branch?.ifBlank { null } ?: this.role.ifBlank { null },
        year = this.passoutYear?.ifBlank { null } ?: this.year?.ifBlank { null },
        section = this.section?.ifBlank { null },
        isVerified = false
    )
}

fun abbreviateStream(value: String): String {
    val trimmed = value.trim()
    if (trimmed.length <= 10) return trimmed
    val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
    return if (words.size > 1) {
        words.joinToString("") { it[0].uppercaseChar().toString() }
    } else {
        trimmed.take(5)
    }
}

/**
 * Generate a username from name or email
 * Examples: "John Doe" -> "johndoe", "john@example.com" -> "john"
 */
private fun generateUsername(name: String, email: String): String {
    return when {
        name.isNotBlank() -> name.lowercase()
            .replace(" ", "")
            .replace(Regex("[^a-z0-9]"), "")
            .take(20)
        email.isNotBlank() -> email.substringBefore("@")
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .take(20)
        else -> "user"
    }
}
