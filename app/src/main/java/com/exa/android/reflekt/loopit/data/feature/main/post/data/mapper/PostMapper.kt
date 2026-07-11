package com.exa.android.reflekt.loopit.data.feature.main.post.data.mapper

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostConstants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

/**
 * Mapper for converting between Post domain model and Firestore representation
 */
object PostMapper {
    
    /**
     * Convert Post domain model to Firestore map for storage
     */
    fun toMap(post: Post): Map<String, Any?> {
        val projectRolesList = post.projectRoles.map { role ->
            mapOf(
                "id" to role.id,
                "roleName" to role.roleName,
                "technology" to role.technology,
                "positionsNeeded" to role.positionsNeeded,
                "positionsFilled" to role.positionsFilled,
                "description" to role.description
            )
        }

        val pollOptionsList = post.pollOptions.map { option ->
            mapOf(
                "text" to option.text,
                "votes" to option.votes
            )
        }

        val eventDetailsMap = post.eventDetails?.let { details ->
            mapOf(
                "eventDate" to details.eventDate,
                "eventTime" to details.eventTime,
                "location" to details.location,
                "venue" to details.venue,
                "capacity" to details.capacity,
                "registrationDeadline" to details.registrationDeadline,
                "isVirtual" to details.isVirtual,
                "meetingLink" to details.meetingLink,
                "registeredCount" to details.registeredCount
            )
        }

        return mapOf(
            PostConstants.PostFields.TITLE to post.title,
            PostConstants.PostFields.DESCRIPTION to post.description,
            PostConstants.PostFields.ROLES_NEEDED to post.rolesNeeded,
            PostConstants.PostFields.TAGS to post.tags,
            PostConstants.PostFields.CREATED_AT to (post.createdAt ?: FieldValue.serverTimestamp()),
            PostConstants.PostFields.CREATED_BY to post.createdBy,
            PostConstants.PostFields.CREATED_BY_NAME to post.createdByName,
            PostConstants.PostFields.ENROLLED_PERSONS to post.enrolledPersons,
            PostConstants.PostFields.REQUESTED_PERSONS to post.requestedPersons,
            PostConstants.PostFields.TYPE to post.type,
            PostConstants.PostFields.IMAGE_URLS to post.imageUrls,
            PostConstants.PostFields.FILE_URLS to post.fileUrls, // New
            PostConstants.PostFields.LINKS to post.links,
            PostConstants.PostFields.VIDEO_URL to post.videoUrl,
            PostConstants.PostFields.LIKES to post.likes,
            PostConstants.PostFields.TITLE_LOWER to post.title.lowercase(),
            PostConstants.PostFields.PROJECT_ROLES to projectRolesList, // New
            PostConstants.PostFields.EVENT_DETAILS to eventDetailsMap, // New
            PostConstants.PostFields.POLL_OPTIONS to pollOptionsList, // New
            PostConstants.PostFields.VIEWS_COUNT to post.viewsCount,
            PostConstants.PostFields.IS_ANONYMOUS to post.isAnonymous
        )
    }
    
    /**
     * Convert Firestore map to Post domain model
     */
    @Suppress("UNCHECKED_CAST")
    fun fromMap(map: Map<String, Any?>): Post {
        val projectRolesList = (map[PostConstants.PostFields.PROJECT_ROLES] as? List<Map<String, Any?>>)?.map { roleMap ->
            com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole(
                id = roleMap["id"] as? String ?: "",
                roleName = roleMap["roleName"] as? String ?: "",
                technology = roleMap["technology"] as? String,
                positionsNeeded = (roleMap["positionsNeeded"] as? Long)?.toInt() ?: 1,
                positionsFilled = (roleMap["positionsFilled"] as? Long)?.toInt() ?: 0,
                description = roleMap["description"] as? String,
                isManuallyClosed = roleMap["isManuallyClosed"] as? Boolean ?: false
            )
        } ?: emptyList()

        val pollOptionsList = (map[PostConstants.PostFields.POLL_OPTIONS] as? List<Map<String, Any?>>)?.map { optionMap ->
            com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PollOption(
                text = optionMap["text"] as? String ?: "",
                votes = (optionMap["votes"] as? List<String>) ?: emptyList()
            )
        } ?: emptyList()

        val eventDetailsMap = map[PostConstants.PostFields.EVENT_DETAILS] as? Map<String, Any?>
        val eventDetails = eventDetailsMap?.let { details ->
            com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails(
                eventDate = details["eventDate"] as? Timestamp,
                eventTime = details["eventTime"] as? String,
                location = details["location"] as? String,
                venue = details["venue"] as? String,
                capacity = (details["capacity"] as? Long)?.toInt(),
                registrationDeadline = details["registrationDeadline"] as? Timestamp,
                isVirtual = details["isVirtual"] as? Boolean ?: false,
                meetingLink = details["meetingLink"] as? String,
                registeredCount = (details["registeredCount"] as? Long)?.toInt() ?: 0
            )
        }

        return Post(
            id = map[PostConstants.PostFields.ID] as? String ?: "",
            title = map[PostConstants.PostFields.TITLE] as? String ?: "",
            description = map[PostConstants.PostFields.DESCRIPTION] as? String ?: "",
            createdAt = map[PostConstants.PostFields.CREATED_AT] as? Timestamp,
            createdBy = map[PostConstants.PostFields.CREATED_BY] as? String ?: "",
            createdByName = map[PostConstants.PostFields.CREATED_BY_NAME] as? String ?: "",
            type = map[PostConstants.PostFields.TYPE] as? String ?: "",
            links = map[PostConstants.PostFields.LINKS] as? List<String> ?: emptyList(),
            imageUrls = map[PostConstants.PostFields.IMAGE_URLS] as? List<String> ?: emptyList(),
            fileUrls = map[PostConstants.PostFields.FILE_URLS] as? List<String> ?: emptyList(), // New
            videoUrl = map[PostConstants.PostFields.VIDEO_URL] as? String ?: "",
            likesCount = (map[PostConstants.PostFields.LIKES_COUNT] as? Long)?.toInt() ?: 0,
            viewsCount = (map[PostConstants.PostFields.VIEWS_COUNT] as? Long)?.toInt() ?: 0, // New
            commentsCount = (map[PostConstants.PostFields.COMMENTS_COUNT] as? Long)?.toInt() 
                ?: (map[PostConstants.PostFields.COMMENT_COUNT] as? Long)?.toInt() ?: 0,
            sharesCount = (map[PostConstants.PostFields.SHARES_COUNT] as? Long)?.toInt() ?: 0,
            tags = map[PostConstants.PostFields.TAGS] as? List<String> ?: emptyList(),
            rolesNeeded = map[PostConstants.PostFields.ROLES_NEEDED] as? List<String> ?: emptyList(),
            projectRoles = projectRolesList, // New
            eventDetails = eventDetails, // New
            pollOptions = pollOptionsList, // New
            likes = map[PostConstants.PostFields.LIKES] as? List<String> ?: emptyList(),
            enrolledPersons = map[PostConstants.PostFields.ENROLLED_PERSONS] as? Map<String, String> ?: emptyMap(),
            requestedPersons = map[PostConstants.PostFields.REQUESTED_PERSONS] as? Map<String, String> ?: emptyMap(),
            isAnonymous = map[PostConstants.PostFields.IS_ANONYMOUS] as? Boolean ?: false
        )
    }
    
    /**
     * Convert Firestore DocumentSnapshot to Post
     */
    fun fromDocument(document: DocumentSnapshot): Post? {
        return document.data?.let { data ->
            fromMap(data.plus(PostConstants.PostFields.ID to document.id))
        }
    }
}
