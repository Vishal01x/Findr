package com.exa.android.reflekt.loopit.presentation.main.post.screen

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostUserInfo

/**
 * State for Post Home Screen
 * Holds the list of posts, loading state, filters, and UI state
 */
data class PostHomeState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: PostType? = null, // Post type filter (PROJECT, COLLABORATION, etc.)
    
    // Comment sheet state
    val showCommentSheet: Boolean = false,
    val selectedPostId: String? = null,
    val selectedPostComments: List<Comment> = emptyList(),
    
    // Image viewer state
    val showImageViewer: Boolean = false,
    val selectedImages: List<String> = emptyList(),
    val selectedImageIndex: Int = 0,
    val selectedImageUser : PostUserInfo? = null,
    val selectedImageCaption : String? = null,

    // Post Edit and Delete state
    val showDeleteDialog: Boolean = false,
    val postToDelete: Post? = null
)
