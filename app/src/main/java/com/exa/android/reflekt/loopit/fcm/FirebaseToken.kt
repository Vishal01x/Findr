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
    private var tokenExpiryTime: Long = 0L  // epoch millis when token expires
    private val tokenLock = ReentrantLock()
    private const val TOKEN_EXPIRY_BUFFER_MS = 5 * 60 * 1000L  // refresh 5 min before expiry

    suspend fun getAccessToken(context: Context): String {
        tokenLock.lock()
        return try {
            val now = System.currentTimeMillis()
            // Return cached token if still valid (with buffer)
            if (cachedToken != null && now < tokenExpiryTime - TOKEN_EXPIRY_BUFFER_MS) {
                return cachedToken!!
            }

            withContext(Dispatchers.IO) {
                val inputStream = context.resources.openRawResource(R.raw.reflect_firebase_admin_sdk_key)
                val googleCreds = GoogleCredentials.fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

                val accessToken = googleCreds.refreshAccessToken()
                cachedToken = accessToken.tokenValue
                // Google tokens expire in 1 hour; store that time
                tokenExpiryTime = accessToken.expirationTime?.time
                    ?: (System.currentTimeMillis() + 60 * 60 * 1000L)  // fallback: 1 hour
                cachedToken!!
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to get access token", e)
        } finally {
            tokenLock.unlock()
        }
    }

    fun clearCachedToken() {
        tokenLock.lock()
        try {
            cachedToken = null
            tokenExpiryTime = 0L
        } finally {
            tokenLock.unlock()
        }
    }
}