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
        bio = null, // Bio not currently in profileUser model
        isVerified = false // Can be extended later
    )
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
