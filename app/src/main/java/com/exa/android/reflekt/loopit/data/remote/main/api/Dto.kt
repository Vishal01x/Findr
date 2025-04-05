package com.exa.android.reflekt.loopit.data.remote.main.api

//data class FCMRequest(
//    val to: String, // FCM Token of receiver
//    val notification: NotificationData
//)

data class NotificationData(
    val chatId : String,
    val senderId : String,
    val title: String, //senderName
    val body: String, // message Text
    val imageUrl : String
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
)

