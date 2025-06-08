package com.exa.android.reflekt.loopit.fcm
import android.net.Uri
import com.exa.android.reflekt.MyApp
import com.exa.android.reflekt.R

//enum class NotificationType(val channelId: String) {
//    CHAT_MESSAGE(MyApp.CHANNEL_CHAT),
//    PROJECT_UPDATE(MyApp.CHANNEL_PROJECTS),
//    COMMENT(MyApp.CHANNEL_SOCIAL),
//    PROFILE_VIEW(MyApp.CHANNEL_SOCIAL),
//    APP_UPDATE(MyApp.CHANNEL_SYSTEM);
//
//    companion object {
//        fun fromString(value: String) = entries.first { it.name == value }
//    }
//}

//// NotificationType.kt
//sealed class NotificationType(val typeName: String) {
//    object ChatMessage : NotificationType("CHAT_MESSAGE")
//    object ProjectUpdate : NotificationType("PROJECT_INVITE")
//    object Comment : NotificationType("COMMENT")
//    object ProfileView : NotificationType("PROFILE_VIEW")
//    object AppUpdate : NotificationType("APP_UPDATE")
//
//    companion object {
//        fun fromString(value: String) = when(value) {
//            "CHAT_MESSAGE" -> ChatMessage
//            "PROJECT_INVITE" -> ProjectUpdate
//            "COMMENT" -> Comment
//            "PROFILE_VIEW" -> ProfileView
//            "APP_UPDATE" -> AppUpdate
//            else -> throw IllegalArgumentException("Unknown notification type")
//        }
//    }
//}
//

enum class NotificationType(
    val channelId: String,
    val defaultTitle: String,
    val deepLinkBase: String,
    val iconResId: Int,
    val actions: List<NotificationAction>
) {
    CHAT_MESSAGE(
        channelId = MyApp.CHANNEL_CHAT,
        defaultTitle = "New Chat Message",
        deepLinkBase = "myapp://chat?chatId=",
        iconResId = R.drawable.send, // use reply
        actions = listOf( NotificationAction("Reply", ActionType.Reply, R.drawable.send, true),
            NotificationAction("Mark As Read", ActionType.Mark_As_Read, R.drawable.send, false),
            NotificationAction("Ignore", ActionType.Ignore, R.drawable.send, false))
    ),
    PROJECT_UPDATE(
        channelId = MyApp.CHANNEL_PROJECTS,
        defaultTitle = "Project Update",
        deepLinkBase = "myapp://project?projectId=",
        iconResId = R.drawable.send, // use accept or reject
        actions =listOf(
            NotificationAction("Accept", ActionType.Accept, R.drawable.send),
            NotificationAction("Reject", ActionType.Reject, R.drawable.send)
        )
    ),
    POST(
        channelId = MyApp.CHANNEL_SOCIAL,
        defaultTitle = "New Comment",
        deepLinkBase = "myapp://comments?postId=",
        iconResId = R.drawable.send, // use comment
        actions = emptyList()
    ),
    PROFILE(
        channelId = MyApp.CHANNEL_PROFILE,
        defaultTitle = "Profile Viewed",
        deepLinkBase = "myapp://profile?userId=",
        iconResId = R.drawable.send, // use profile
        actions = emptyList()
    ),
    APP_UPDATE(
        channelId = MyApp.CHANNEL_SYSTEM,
        defaultTitle = "App Update Available",
        deepLinkBase = "myapp://updates",
        iconResId = R.drawable.send, // use for app update
        actions = listOf( NotificationAction("Update App", ActionType.Update, R.drawable.send, false))
    );

    companion object {
        fun fromChannelId(channelId: String): NotificationType? =
            entries.find { it.channelId == channelId }

        fun fromString(value: String): NotificationType? =
            entries.find { it.name == value }
    }
}



data class NotificationData(
    val type: NotificationType,
    val title: String,
    val body: String,
    val deepLink: Uri,
    val mediaUri : Uri? = null,
    val link : String?=null,
    val actions: List<NotificationAction> = emptyList(),
    val largeIcon: Uri? = null,
    val groupKey: String? = null,
    val senderId : String = "",
    val fcm : String? = null
)

data class NotificationAction(
    val title: String,
    val actionId: ActionType,
    val iconResId: Int,
    val requiresInput: Boolean = false
)

// Enhanced ActionType enum
enum class ActionType(val actionId: String) {
    Reply("action_reply"),
    Mark_As_Read("mark_as_read"),
    Ignore("ignore"),
    Accept("action_accept"),
    Reject("action_reject"),
    Update("action_update");


    companion object {
        fun fromActionId(actionId: String?) = when (actionId) {
            "action_reply" -> Reply
            "mark_as_read" -> Mark_As_Read
            "ignore" -> Ignore
            "action_accept" -> Accept
            "action_reject" -> Reject
            "action_update" -> Update

            else -> null
        }
    }
}

enum class Topics(val type : String){
    Post("post"),
    Event("event"),
    NULL("")
}


