package com.exa.android.reflekt.loopit.data.remote.main.Repository

import android.content.Context
import com.exa.android.reflekt.loopit.data.remote.main.api.fcm.NotificationContent
import com.exa.android.reflekt.loopit.fcm.NotificationSender
import com.exa.android.reflekt.loopit.fcm.NotificationType
import com.exa.android.reflekt.loopit.fcm.Topics
import com.exa.android.reflekt.loopit.util.model.PostType
import com.exa.android.reflekt.loopit.util.model.Project
import com.exa.android.reflekt.loopit.util.model.profileUser

fun sendProjectNotification(
    context: Context,
    receiverToken: String?,
    body: String,
    title: String,
    projectId: String?,
    senderId: String,
    imageUrl: String?
) {
    val projectNotification = NotificationContent(
        type = NotificationType.PROJECT_UPDATE,
        title = title, // project title
        body = body,
        imageUrl = imageUrl, // user enrolled in project
        targetId = projectId ?: "Not Found",
        senderId = senderId, // the person enrolled in project
//            metadata = mapOf(
//                "messageId" to message.messageId,
//                "isMedia" to (message.media != null).toString()
//            )
    )

    NotificationSender(context).sendNotification(
        deviceToken = receiverToken ?: "",
        topics = Topics.NULL,
        content = projectNotification
    )
}

fun sendRequestUpdate(context: Context, receiverToken: String?,project : Project,profile: profileUser, action : String){

    val imageUrl =  if(project.imageUrls.isNotEmpty())project.imageUrls[0] else profile.imageUrl

        val projectNotification = NotificationContent(
            type = NotificationType.PROFILE,
            title = project.title, // project title
            body = "${profile.name} has $action your enroll request. Keep going",
            imageUrl = imageUrl, // post or creator of project
            targetId = project.id ?: "Not Found",
            senderId = "", // the person enrolled in project
//            metadata = mapOf(
//                "messageId" to message.messageId,
//                "isMedia" to (message.media != null).toString()
//            )
        )

        NotificationSender(context).sendNotification(
            deviceToken = receiverToken ?: "",
            topics = Topics.NULL,
            content = projectNotification
        )
}


fun sendComment(context: Context, receiverToken: String?,project : Project,profile: profileUser, comment : String){

    val imageUrl =  if(project.imageUrls.isNotEmpty())project.imageUrls[0] else profile.imageUrl

    val projectNotification = NotificationContent(
        type = NotificationType.POST,
        title = "${profile.name} has commented on your post", // project title
        body = comment,
        imageUrl = imageUrl, // post or creator of project
        targetId = project.id ?: "Not Found",
        senderId = "", // the person enrolled in project
//            metadata = mapOf(
//                "messageId" to message.messageId,
//                "isMedia" to (message.media != null).toString()
//            )
    )

    NotificationSender(context).sendNotification(
        deviceToken = receiverToken ?: "",
        topics = Topics.NULL,
        content = projectNotification
    )
}


fun sendPostNotification(context: Context, topic:Topics, project: Project, profile : profileUser){

    val (title, body) = getPostNotificationText(profile.name,project.title,project.type)


    // Limit length for body description (e.g., max 50 chars)
//    val maxBodyLength = 75
//
//    // Truncate postTitle if too long
//    val truncatedBody = if (body.length > maxBodyLength) {
//        body.take(maxBodyLength) + "..."
//    } else {
//        body
//    }

    val imageUrl =  if(project.imageUrls.isNotEmpty())project.imageUrls[0] else profile.imageUrl

    val postNotification = NotificationContent(
        type = NotificationType.POST,
        title = title,  //"${profile.name} has posted ${project.type}",//type -> Post,Bug
        body = body,    //project.title,
        imageUrl = profile.imageUrl, //user who post
        mediaUrl = if(project.imageUrls.isNotEmpty())project.imageUrls[0] else null,
        link = if(project.links.isNotEmpty())project.links[0] else null,
        targetId = project.id,
        senderId = project.createdBy, // the person post
        postcategory = project.type
    )

    NotificationSender(context).sendNotification(
        deviceToken = "",
        topics = topic,
        content = postNotification
    )

}

fun sendProfileNotification(context: Context, fcm : String?, userId : String, viewCount : Int, profileImageUrl: String){
    val viewerNotification = NotificationContent(
        type = NotificationType.PROFILE,
        title = "You're getting noticed! 👀",  //"${profile.name} has posted ${project.type}",//type -> Post,Bug
        body = "Your profile is gaining attention – $viewCount new views! \nTap to see your viewers.",    //project.title,
        imageUrl = profileImageUrl, // post else user who post
        targetId = userId,
        senderId = userId, // the person post
        postcategory = "viewer"
    )

    NotificationSender(context).sendNotification(
        deviceToken = fcm ?: "",
        topics = Topics.NULL,
        content = viewerNotification
    )
}


fun sendVerifyNotification(context: Context, fcm: String?, verifierUserId: String, targetUserId: String) {
        val verifyNotification = NotificationContent(
            type = NotificationType.PROFILE,
            title = "Someone just verified you!",  //"${profile.name} has posted ${project.type}",//type -> Post,Bug
            body = "Your profile is gaining trust— someone has confirmed your authenticity \nTap to see verifier.",    //project.title,
            imageUrl = "", // post else user who post
            targetId = targetUserId,
            senderId = verifierUserId, // the person post
            postcategory = "verify"
        )

        NotificationSender(context).sendNotification(
            deviceToken = fcm ?: "",
            topics = Topics.NULL,
            content = verifyNotification
        )
    }




    fun getPostNotificationText(type: PostType): Pair<String, String> = when (type) {
    PostType.PROJECT -> "A new project needs your skills!" to "Build, collaborate, and innovate—check out the latest project post."
    PostType.BUG -> "Bug spotted in the system!" to "A bug is affecting project progress—see what’s up."
    PostType.COLLABORATION -> "Let’s team up!" to "Help build something great—join this collaboration."
    PostType.POST -> "A new post is live!" to "Check out the latest community post—your input matters."
    PostType.OTHER -> "Something new is happening." to "Explore the latest shared content from the community."
}

fun getPostNotificationText(userName: String, projectTitle: String, displayName: String): Pair<String, String> {
    return when (displayName) {
        "Project" -> "🚀 A new project is calling!" to
                "$userName is looking for \"$projectTitle\""

        "Bug Fix" -> "⚠️ Can you fix this bug?" to
                "$userName needs you to solve \"$projectTitle\""

        "Collab" -> "🤝 Let’s team up!" to
                "$userName requested to collab on \"$projectTitle\""

        "Post" -> "📝 New post from $userName" to
                "Share your thoughts on \"$projectTitle\""

        "Other" -> "🔍 Something new to explore" to
                "Check out \"$projectTitle\""

        else -> "📢 New activity alert!" to
                "Something new just landed in your feed—check it out."
    }
}



