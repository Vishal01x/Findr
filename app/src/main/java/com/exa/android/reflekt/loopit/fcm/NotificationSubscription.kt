package com.exa.android.reflekt.loopit.fcm

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val topicManager: TopicManager,
    private val firebaseMessaging: FirebaseMessaging
) {
    suspend fun ensureDefaultSubscription(topics: List<String>): List<String> {
        return withContext(Dispatchers.IO) {
            val currentTopics = topicManager.getSubscribedTopics()
            val newlySubscribed = mutableListOf<String>()

            try {
                topics
                    .filterNot { currentTopics.contains(it) } // Only those not already subscribed
                    .forEach { topic ->
                        firebaseMessaging.subscribeToTopic(topic).await()
                        topicManager.saveSubscription(topic, true)
                        newlySubscribed.add(topic)
                    }
            } catch (e: Exception) {
                // error in subscribing topics
                Log.d("FCM", "failed subscribing to $topics")
            }

            // Return updated total list of topics (existing + new)
            currentTopics + newlySubscribed
        }
    }
}

    // 2. Domain Layer - Topic Manager
class TopicManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("topic_subscriptions", Context.MODE_PRIVATE)

    fun getSubscribedTopics(): List<String> {
        return prefs.all
            .filter { it.value as Boolean }
            .keys
            .toList()
    }

    fun saveSubscription(topic: String, subscribed: Boolean) {
        prefs.edit().putBoolean(topic, subscribed).apply()
    }
}
