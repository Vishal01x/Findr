package com.exa.android.reflekt.loopit.fcm

import android.content.Context
import com.exa.android.reflekt.R
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock

object FirebaseAuthHelper {
    private var cachedToken: String? = null
    private val tokenLock = ReentrantLock()

    suspend fun getAccessToken(context: Context): String {
        tokenLock.lock()
        return try {
            cachedToken?.let { return it }

            withContext(Dispatchers.IO) {
                val inputStream = context.resources.openRawResource(R.raw.reflect_firebase_admin_sdk_key)
                val googleCreds = GoogleCredentials.fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

                val token = googleCreds.refreshAccessToken().tokenValue
                cachedToken = token
                token
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to get access token", e)
        } finally {
            tokenLock.unlock()
        }
    }
}