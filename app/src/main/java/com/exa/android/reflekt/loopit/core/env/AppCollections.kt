package com.exa.android.reflekt.loopit.core.env

/**
 * Firestore collection names resolved at runtime per environment.
 * Staging uses the "test_" prefix on all top-level collections.
 * Subcollections (messages, comments, replies, etc.) don't need a prefix
 * because they live under an already-prefixed parent document.
 */
object AppCollections {
    private val p get() = if (EnvironmentManager.isStaging) "test_" else ""

    // Top-level collections — prefixed per environment
    val USERS         get() = "${p}users"
    val CHATS         get() = "${p}chats"
    val PROJECTS      get() = "${p}projects"
    val ENROLLMENTS   get() = "${p}enrollments"
    val REGISTRATIONS get() = "${p}registrations"
    val TOPICS        get() = "${p}topics"
    val TRENDING      get() = "${p}trending_profiles"

    // Subcollections — always fixed; they live under an already-prefixed parent
    const val MESSAGES      = "messages"
    const val NOTIFICATIONS = "notifications"
    const val COMMENTS      = "comments"
    const val REPLIES       = "replies"
    const val METADATA      = "metadata"
}
