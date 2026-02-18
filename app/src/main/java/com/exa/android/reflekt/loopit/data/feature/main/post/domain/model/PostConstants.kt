package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

/**
 * Constants for Post module
 * Centralizes all field names, collection names, and limits
 */
object PostConstants {
    
    // Firestore Collections
    object Collections {
        const val PROJECTS = "projects"
        const val COMMENTS = "comments"
        const val METADATA = "metadata"
        const val REPLIES = "replies"
    }
    
    // Firestore Documents
    object Documents {
        const val FILTERS = "projectFilters"
    }
    
    // Post Fields
    object PostFields {
        const val ID = "id"
        const val TITLE = "title"
        const val TITLE_LOWER = "title_lower"
        const val DESCRIPTION = "description"
        const val CREATED_AT = "createdAt"
        const val CREATED_BY = "createdBy"
        const val CREATED_BY_NAME = "createdByName"
        const val TYPE = "type"
        const val LINKS = "links"
        const val IMAGE_URLS = "imageUrls"
        const val VIDEO_URL = "videoUrl"
        const val LIKES_COUNT = "likesCount"
        const val LIKES = "likes"
        const val COMMENTS_COUNT = "commentsCount"
        const val COMMENT_COUNT = "commentCount"
        const val SHARES_COUNT = "sharesCount"
        const val TAGS = "tags"
        const val ROLES_NEEDED = "rolesNeeded"
        const val ENROLLED_PERSONS = "enrolledPersons"
        const val REQUESTED_PERSONS = "requestedPersons"
        const val PROJECT_ROLES = "projectRoles" // New
        const val EVENT_DETAILS = "eventDetails" // New
        const val FILE_URLS = "fileUrls" // New
        const val VIEWS_COUNT = "viewsCount" // New
    }
    
    // Comment Fields
    object CommentFields {
        const val ID = "id"
        const val POST_ID = "postId"
        const val TEXT = "text"
        const val SENDER_ID = "senderId"
        const val SENDER_NAME = "senderName"
        const val SENDER_IMAGE_URL = "senderImageUrl"
        const val TIMESTAMP = "timestamp"
        const val LIKES = "likes"
        const val LIKES_COUNT = "likesCount"
        const val REPLY_COUNT = "replyCount"
        const val PARENT_COMMENT_ID = "parentCommentId"
        const val NESTING_LEVEL = "nestingLevel"
        const val EMBEDDED_REPLY = "embeddedReply"
    }
    
    // Reply Fields
    object ReplyFields {
        const val REPLY_ID = "replyId"
        const val POST_ID = "postId"
        const val COMMENT_ID = "commentId"
        const val PARENT_REPLY_ID = "parentReplyId"
        const val USER_ID = "userId"
        const val USER_NAME = "userName"
        const val TEXT = "text"
        const val TIMESTAMP = "timestamp"
    }
    
    // Filter Fields
    object FilterFields {
        const val AVAILABLE_ROLES = "availableRoles"
        const val AVAILABLE_TAGS = "availableTags"
    }
    
    // Limits
    object Limits {
        const val DEFAULT_PAGE_SIZE = 20L
        const val MAX_IMAGES = 5  // Updated from 10 to 5 for post creation
        const val MAX_FILES = 3   // New: Maximum file attachments
        const val MAX_FILE_SIZE = 10 * 1024 * 1024L  // New: 10MB in bytes
        const val MAX_LINKS = 5
        const val MAX_TITLE_LENGTH = 200
        const val MAX_DESCRIPTION_LENGTH = 5000
        const val MAX_COMMENT_LENGTH = 500
        const val MAX_COMMENT_NESTING_LEVEL = 3
        const val MAX_PROJECT_ROLES = 10  // New: Maximum roles per project
    }
    
    // Validation Messages
    object ValidationMessages {
        const val TITLE_REQUIRED = "Title cannot be empty"
        const val TITLE_TOO_LONG = "Title is too long"
        const val DESCRIPTION_TOO_LONG = "Description is too long"
        const val USER_NOT_AUTHENTICATED = "User must be authenticated"
        const val UNAUTHORIZED_UPDATE = "Only post owner can update"
        const val UNAUTHORIZED_DELETE = "Only post owner can delete"
        const val POST_NOT_FOUND = "Post not found"
    }
}
