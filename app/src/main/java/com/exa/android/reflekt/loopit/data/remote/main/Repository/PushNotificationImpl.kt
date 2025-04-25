package com.exa.android.reflekt.loopit.data.remote.main.Repository

import com.exa.android.reflekt.loopit.data.remote.main.api.fcm.FCMRequest
import com.exa.android.reflekt.loopit.data.remote.main.api.fcm.MessageData
import com.exa.android.reflekt.loopit.data.remote.main.api.fcm.NotificationData
import com.exa.android.reflekt.loopit.data.remote.main.api.fcm.RetrofitInstance
import com.exa.android.reflekt.loopit.fcm.FirebaseAuthHelper.getAccessToken
import com.exa.android.reflekt.loopit.util.model.Message
import com.exa.android.reflekt.loopit.util.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/*
private fun sendPushNotification(receiverToken: String, message: Message, curUser: User?) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val accessToken = getAccessToken(context)
            val request = FCMRequest(
                message = MessageData(
                    token = receiverToken,
//                        notification = NotificationData(
//                            title = "New Message",
//                            body = message
//                        )
                    data = NotificationData(
                        title = curUser?.name ?: "",
                        senderId = message.senderId,
                        chatId = message.chatId,
                        body = if(message.media != null)message.media.mediaType.name else message.message,
                        imageUrl = curUser?.profilePicture ?: ""
                    )
                )
            )

            val response =
                RetrofitInstance.api.sendNotification("Bearer $accessToken", request).execute()

            if (response.isSuccessful) {
                /*
                Log.d(
                    "FireStore Operation",
                    "Notification sent successfully: ${response.body()}"
                )

                 */
            } else {
                /*
                Log.e(
                    "FireStore Operation",
                    "Error sending notification: ${response.errorBody()?.string()}"
                )

                 */
            }
        } catch (e: Exception) {
            // Log.e("FireStore Operation", "FCM Request Failed", e)
        }
    }
}*/