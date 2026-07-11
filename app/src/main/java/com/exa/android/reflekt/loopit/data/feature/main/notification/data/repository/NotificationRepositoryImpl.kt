package com.exa.android.reflekt.loopit.data.feature.main.notification.data.repository

import com.exa.android.reflekt.loopit.core.env.AppCollections
import com.exa.android.reflekt.loopit.data.feature.main.notification.data.local.NotificationDao
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.Notification
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val notificationDao: NotificationDao
) : NotificationRepository {

    private val userId: String?
        get() = auth.currentUser?.uid

    private fun notifCollection(uid: String) =
        firestore.collection("users").document(uid).collection("notifications")

    override fun getNotifications(): Flow<List<Notification>> =
        notificationDao.getAllNotifications()

    override fun getUnreadCount(): Flow<Int> =
        notificationDao.getUnreadCount()

    /**
     * Attaches a Firestore snapshot listener that continuously syncs remote notifications
     * into Room. Before each insert we preserve any locally-read state so a server snapshot
     * with isRead=false never un-marks something the user already read.
     */
    override suspend fun syncNotificationsRealTime() {
        val uid = userId ?: return
        suspendCancellableCoroutine<Unit> { cont ->
            val listener = notifCollection(uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val incoming = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Notification::class.java)?.copy(id = doc.id)
                    }
                    if (incoming.isEmpty()) return@addSnapshotListener
                    CoroutineScope(Dispatchers.IO).launch {
                        // Fetch the IDs that are already marked read locally.
                        // If the server snapshot still has isRead=false for them (Firestore
                        // write in-flight / not yet confirmed), keep the local truth.
                        val localReadIds = notificationDao.getReadIds().toHashSet()
                        val merged = incoming.map { n ->
                            if (!n.isRead && localReadIds.contains(n.id)) n.copy(isRead = true)
                            else n
                        }
                        notificationDao.insertNotifications(merged)
                    }
                }
            cont.invokeOnCancellation { listener.remove() }
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        val uid = userId ?: return
        try {
            // Local update first for immediate UI response
            notificationDao.markAsRead(notificationId)
            notifCollection(uid).document(notificationId).update("isRead", true).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun markAllAsRead() {
        val uid = userId ?: return
        try {
            // Mark all locally read first
            notificationDao.markAllAsRead()
            // Use local IDs instead of a Firestore filter — avoids missing docs where
            // the isRead field is absent/null (which whereEqualTo("isRead", false) skips)
            val localIds = notificationDao.getReadIds()
            if (localIds.isNotEmpty()) {
                val batch = firestore.batch()
                localIds.forEach { id ->
                    batch.update(notifCollection(uid).document(id), "isRead", true)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun acceptInvitation(notificationId: String, postId: String): Result<Unit> {
        val uid = userId ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val userDoc = firestore.collection(AppCollections.USERS).document(uid).get().await()
            val userName = userDoc.getString("name") ?: uid
            firestore.collection(AppCollections.PROJECTS)
                .document(postId)
                .update("requestedPersons.$uid", userName)
                .await()
            markAsRead(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectInvitation(notificationId: String): Result<Unit> {
        markAsRead(notificationId)
        return Result.success(Unit)
    }
}
