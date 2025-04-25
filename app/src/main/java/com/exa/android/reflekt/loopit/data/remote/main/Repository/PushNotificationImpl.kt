package com.exa.android.reflekt.loopit.data.remote.main.Repository

import android.content.Context
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

fun sendPushNotification(context : Context,receiverToken: String, message: String, title: String, imageUrl : String?) {
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
                        title = title,
                        senderId = "",
                        chatId = "",
                        body = message,
                        imageUrl = imageUrl?: "",
                        isChat = "No"
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
}