package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

/**
 * Represents different types of posts in the system
 * Domain model - contains only business logic, no UI concerns
 */
enum class PostType(
    val displayName: String
) {
    ALL("All"),
    POST("Post"),
    PROJECT("Project"),
    EVENT("Event"),
    POLL("Poll"),
    HELP("Bug Fix"),
    COLLABORATION("Collab"),
    OTHER("Other");
    //GENERAL("General");
    
    companion object {
        fun fromDisplayName(name: String): PostType? {
            return values().find { it.displayName.equals(name, ignoreCase = true) }
        }
    }
}
