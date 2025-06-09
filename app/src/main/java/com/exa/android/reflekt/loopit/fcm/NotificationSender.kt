package com.exa.android.reflekt.loopit.fcm


import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.exa.android.reflekt.loopit.data.remote.main.api.fcm.FcmAndroidConfig
import com.exa.android.reflekt.loopit.data.remote.main.api.fcm.FcmMessage
import com.exa.android.reflekt.loopit.data.remote.main.api.fcm.FcmRequest
import com.exa.android.reflekt.loopit.data.remote.main.api.fcm.FcmResponse
import com.exa.android.reflekt.loopit.data.remote.main.api.fcm.NotificationContent
import com.exa.android.reflekt.loopit.data.remote.main.api.fcm.RetrofitInstance.api
import com.exa.android.reflekt.loopit.fcm.FirebaseAuthHelper.getAccessToken
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificationSender(private val context: Context) {

    private val gson = Gson()

    fun sendNotification(
        deviceToken: String,
        topics: Topics,
        content: NotificationContent
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = getFcmAccessToken()
                val fcmRequest = createFcmRequest(deviceToken, topics, content)

                val response = api.sendNotification("Bearer $accessToken", fcmRequest).execute()

                if (response.isSuccessful) {
                    logNotificationSuccess(content)
                } else {
                    handleNotificationError(response, content)
                }
            } catch (e: Exception) {
                logNotificationFailure(e, content)
            }
        }
    }

    private fun createFcmRequest(
        token: String,
        topics: Topics,
        content: NotificationContent
    ): FcmRequest {
        return FcmRequest(
            message = FcmMessage(
                deviceToken = token.ifBlank { null },
                topic = if(topics != Topics.NULL)topics.type else null ,
                data = mapOf(
                    "type" to content.type.channelId,
                    "title" to content.title,
                    "body" to content.body,
                    "imageUrl" to (content.imageUrl ?: ""),
                    "mediaUrl" to (content.mediaUrl ?: ""),
                    "link" to (content.link ?: ""),
                    "targetId" to content.targetId,
                    "senderId" to content.senderId,
                    "fcm" to (content.fcm ?: ""),
                    "postcategory" to content.postcategory,
                    "metadata" to gson.toJson(content.metadata)
                ),
                androidConfig = FcmAndroidConfig()
            )
        )
    }

    private suspend fun getFcmAccessToken(): String {
        return getAccessToken(context)
    }

    private fun logNotificationSuccess(content: NotificationContent) {
        context.getSharedPreferences("notifications", Context.MODE_PRIVATE).edit {
            putLong("last_sent_${content.type}", System.currentTimeMillis())
        }
    }

    private fun handleNotificationError(response: Response<FcmResponse>, content: NotificationContent) {
        val errorBody = response.errorBody()?.string() ?: "Unknown error"
        Log.e("FCM", "Failed to send ${content.type} notification: $errorBody")
    }

    private fun logNotificationFailure(e: Exception, content: NotificationContent) {
        Log.e("FCM", "Failed to send ${content.type} notification", e)
    }
}