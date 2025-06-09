package com.exa.android.reflekt.loopit.data.remote.main.api.fcm

//data class FCMRequest(
//    val to: String, // FCM Token of receiver
//    val notification: NotificationData
//)
/*
data class NotificationData(
    val chatId : String,
    val senderId : String,
    val title: String, //senderName
    val body: String, // message Text
    val imageUrl : String,
    val isChat : String
)

data class FCMResponse(
    val success: Int
)

data class FCMRequest(
    val message: MessageData
)

data class MessageData(
    val token: String,
    val data: NotificationData
)*/



import com.exa.android.reflekt.loopit.fcm.NotificationType
import com.google.gson.annotations.SerializedName

// Top-level FCM request format
data class FcmRequest(
    @SerializedName("message")
    val message: FcmMessage
)

// Message structure containing target and payload
data class FcmMessage(
    @SerializedName("token")
    val deviceToken: String?,

    @SerializedName("topic")
    val topic : String?,

    @SerializedName("data")
    val data: Map<String, String>,

    @SerializedName("android")
    val androidConfig: FcmAndroidConfig? = null
)

// Android-specific configuration
data class FcmAndroidConfig(
    @SerializedName("priority")
    val priority: String = "high"
)

// Server response structure
data class FcmResponse(
    @SerializedName("name")
    val name: String
)

// Notification content container
data class NotificationContent(
    val type: NotificationType,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val mediaUrl : String? = null,
    val link : String? = null,
    val targetId: String,
    val senderId: String,
    val postcategory: String = "",
    val metadata: Map<String, String> = emptyMap(),
    val fcm : String? = null
)

//// Notification type enum
//enum class NotificationType {
//    CHAT_MESSAGE,
//    PROJECT_UPDATE,
//    SYSTEM_ALERT,
//    COMMENT,
//    PROFILE_VIEW
//}