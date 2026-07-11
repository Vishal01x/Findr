package com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository

import android.content.Context
import android.util.Log
import com.exa.android.reflekt.loopit.core.env.AppCollections
import com.exa.android.reflekt.loopit.fcm.api.NotificationContent
import com.exa.android.reflekt.loopit.fcm.api.NotificationSender
import com.exa.android.reflekt.loopit.fcm.NotificationType
import com.exa.android.reflekt.loopit.fcm.Topics
import com.exa.android.reflekt.loopit.core.utils.ChatCryptoUtil
import com.exa.android.reflekt.loopit.core.utils.ChatCryptoUtil.encrypt
import com.exa.android.reflekt.loopit.core.utils.CurChatManager.activeChatId
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.core.utils.generateChatId
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.ChatList
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Media
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.Message
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.Companion.MAX_REQUEST_MESSAGES
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.Companion.STATUS_ACCEPTED
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.Companion.STATUS_NONE
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.Companion.STATUS_PENDING
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.MessageRequest.Companion.STATUS_REJECTED
import com.exa.android.reflekt.loopit.data.feature.main.chat.domain.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.InAppNotificationHelper
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationType as AppNotificationType
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) {
    private val userCollection get() = db.collection(AppCollections.USERS)
    private val chatCollection get() = db.collection(AppCollections.CHATS)
    val currentUser get() = auth.currentUser?.uid

    suspend fun getCurUser(): User? {
        val user = userCollection.document(currentUser!!).get().await()
        return user.toObject(User::class.java)
    }

    fun registerFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                // Log.d("FCM", "Token: $token")
                updateToken(token) // Store in Firestore
            }
            .addOnFailureListener {
                // Log.e("FCM", "Failed to get token", it)
            }
    }

    fun updateToken(token: String?) {
        if (token == null || currentUser.isNullOrEmpty()) return

        val userRef = userCollection.document(currentUser!!)

        val data = mapOf("fcmToken" to token)

        userRef.set(data, SetOptions.merge())
            .addOnSuccessListener {
                // Log.d("FCM", "Token set/updated in Firestore")
            }
            .addOnFailureListener {
                // Log.e("FCM", "Failed to set/update token", it)
            }
    }

    suspend fun createChatAndSendMessage(
        userId2: String,
        isCurUserBlocked: Boolean,
        text: String,
        replyTo: Message?,
        media: Media?,
        receiverToken: String?,
        curUser: User? = null,
        messageId: String? = null
    ): String? {
        val userId1 = auth.currentUser?.uid ?: return null
        val chatId = generateChatId(userId1, userId2)
        val finalMessageId = messageId ?: UUID.randomUUID().toString()

        val members = if (isCurUserBlocked) listOf(userId1) else listOf(userId1, userId2)

        val message = Message(
            chatId = chatId,
            senderId = userId1,
            receiverId = userId2,
            message = text,
            media = media,
            members = members,
            messageId = finalMessageId,
            replyTo = replyTo,
            status = if (isCurUserBlocked) "sent" else "delivered"
        )

        Log.d("FCM", "$text $chatId, $userId1, $userId2, $receiverToken, $curUser")

        val encryptedMessage: Message

        if (media != null) {
            val encryptedUrl = encrypt(media.mediaUrl, chatId)
            val encryptedMedia = media.copy(mediaUrl = encryptedUrl)
            encryptedMessage = message.copy(media = encryptedMedia)
        } else {
            val encryptedText = encrypt(text, chatId)
            encryptedMessage = message.copy(message = encryptedText)
        }

        try {
            withContext(Dispatchers.IO) {
                val chatDoc = chatCollection.document(chatId)
                val snapshot = chatDoc.get().await()

                // ── Message Request enforcement ──────────────────────────────
                val requestMap = snapshot.get("messageRequest") as? Map<*, *>
                val reqStatus = requestMap?.get("status") as? String ?: STATUS_NONE
                val reqSenderId = requestMap?.get("senderId") as? String ?: ""
                val reqCount = (requestMap?.get("senderMessageCount") as? Long)?.toInt() ?: 0

                // Block sender if request is pending/rejected and they are the initiator
                val isInitiator = reqSenderId == userId1 || reqSenderId.isEmpty()
                if (isInitiator && reqStatus == STATUS_PENDING) return@withContext
                if (isInitiator && reqStatus == STATUS_REJECTED) return@withContext

                // Write message
                val messageRef = chatCollection.document(chatId).collection("messages")
                messageRef.document(finalMessageId).set(encryptedMessage).await()

                val displayText = if (message.media != null) message.media.mediaType.name else message.message

                // ── Chat-level metadata ──────────────────────────────────────
                if (snapshot.exists()) {
                    chatDoc.update(
                        mapOf(
                            "lastMessage" to displayText,
                            "lastMessageTimestamp" to message.timestamp,
                            "unreadMessages.$userId2" to if (!isCurUserBlocked) FieldValue.increment(1) else FieldValue.increment(0),
                            "unreadMessages.$userId1" to 0
                        )
                    ).await()
                } else {
                    chatDoc.set(
                        mapOf(
                            "lastMessage" to displayText,
                            "lastMessageTimestamp" to message.timestamp,
                            "unreadMessages" to mapOf(userId1 to 0, userId2 to 1)
                        )
                    ).await()
                }
                updateUserList(userId1, userId2)
                updateUserList(userId2, userId1)

                chatCollection.document(chatId)
                    .set(mapOf("users" to listOf(userId1, userId2)), SetOptions.merge()).await()

                // ── Update request tracking (only if not already accepted) ──
                if (reqStatus != STATUS_ACCEPTED) {
                    val newCount = reqCount + 1
                    val newStatus = when {
                        newCount >= MAX_REQUEST_MESSAGES -> STATUS_PENDING
                        else -> STATUS_NONE
                    }
                    chatDoc.set(
                        mapOf(
                            "messageRequest" to mapOf(
                                "status" to newStatus,
                                "senderId" to userId1,
                                "receiverId" to userId2,
                                "senderMessageCount" to newCount,
                                "timestamp" to Timestamp.now()
                            )
                        ),
                        SetOptions.merge()
                    ).await()
                }
            }

            if (!receiverToken.isNullOrEmpty()) {
                sendPushNotification(receiverToken, message, curUser)
            }
            if (!userRepository.isUserOnline(userId2)) {
                InAppNotificationHelper.sendNotification(
                    firestore = db,
                    recipientId = userId2,
                    type = AppNotificationType.MESSAGE,
                    category = NotificationCategory.SOCIAL,
                    title = curUser?.name ?: "User",
                    message = if (message.media != null) message.media.mediaType.name else message.message,
                    senderName = curUser?.name,
                    senderImageUrl = curUser?.profilePicture,
                    senderId = userId1,
                    targetId = message.chatId,
                    route = "chat_detail/$userId1"
                )
            }

            return finalMessageId
        } catch (e: Exception) {
            // Log.e("FirestoreService", "Error sending message: ${e.localizedMessage}", e)
            return null
        }
    }

    // ── Message Request API ──────────────────────────────────────────────────

    fun observeMessageRequest(chatId: String): Flow<Response<MessageRequest>> = callbackFlow {
        trySend(Response.Loading)
        val listener = chatCollection.document(chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    trySend(Response.Success(MessageRequest()))
                    return@addSnapshotListener
                }
                val map = snapshot.get("messageRequest") as? Map<*, *>
                val req = if (map == null) MessageRequest() else MessageRequest(
                    status = map["status"] as? String ?: STATUS_NONE,
                    senderId = map["senderId"] as? String ?: "",
                    receiverId = map["receiverId"] as? String ?: "",
                    senderMessageCount = (map["senderMessageCount"] as? Long)?.toInt() ?: 0,
                    timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now()
                )
                trySend(Response.Success(req))
            }
        awaitClose { listener.remove() }
    }

    suspend fun approveMessageRequest(chatId: String): Response<Boolean> {
        return try {
            chatCollection.document(chatId)
                .set(mapOf("messageRequest" to mapOf("status" to STATUS_ACCEPTED)), SetOptions.merge())
                .await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Failed to approve request")
        }
    }

    suspend fun rejectMessageRequest(chatId: String): Response<Boolean> {
        return try {
            chatCollection.document(chatId)
                .set(mapOf("messageRequest" to mapOf("status" to STATUS_REJECTED)), SetOptions.merge())
                .await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Failed to reject request")
        }
    }

    suspend fun reEnableMessageRequest(chatId: String): Response<Boolean> {
        // Receiver explicitly allows messaging again after rejection
        return try {
            chatCollection.document(chatId)
                .set(mapOf("messageRequest" to mapOf("status" to STATUS_ACCEPTED)), SetOptions.merge())
                .await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Failed to re-enable messaging")
        }
    }

    private fun sendPushNotification(receiverToken: String, message: Message, curUser: User?) {
        val chatNotification = NotificationContent(
            type = NotificationType.CHAT_MESSAGE,
            title = curUser?.name ?: "User",
            body = if (message.media != null) message.media.mediaType.name else message.message,
            imageUrl = curUser?.profilePicture,
            targetId = message.chatId,
            senderId = message.senderId,
            fcm = curUser?.fcmToken,
            metadata = mapOf(
                "messageId" to message.messageId,
                "isMedia" to (message.media != null).toString()
            )
        )

        NotificationSender(context).sendNotification(
            deviceToken = receiverToken,
            topics = Topics.NULL,
            content = chatNotification
        )
    }

    private fun updateUserList(currentUser: String, newUser: String) {
        val userRef = userCollection.document(currentUser)
        userRef.update("user_list", FieldValue.arrayUnion(newUser))
            .addOnSuccessListener { }
            .addOnFailureListener { }
    }

    suspend fun updateMessage(message: Message, newText: String) {
        val chatId = message.chatId
        val batch = db.batch()
        val messageRef =
            chatCollection.document(chatId).collection("messages").document(message.messageId)
        try {
            val encryptedText = encrypt(newText,chatId)
            batch.update(messageRef, mapOf("message" to encryptedText))
            batch.commit().await()
            // Log.d("FireStore Operation", "Messages Edited Successfully")
        } catch (e: Exception) {
            // Log.d("FireStore Operation", "Error in Message Edition - ${e.message}")
        }
    }

    suspend fun toggleReaction(chatId: String, messageId: String, userId: String, emoji: String) {
        val messageRef = chatCollection.document(chatId).collection("messages").document(messageId)
        try {
            val existing = messageRef.get().await().getString("reactions.$userId")
            if (existing == emoji) {
                messageRef.update("reactions.$userId", FieldValue.delete()).await()
            } else {
                messageRef.update("reactions.$userId", emoji).await()
            }
        } catch (_: Exception) {}
    }

    suspend fun updateMediaMessage(messageId: String, otherUserId: String, media: Media?) {
        val chatId = generateChatId(currentUser!!, otherUserId)
        val batch = db.batch()
        val messageRef =
            chatCollection.document(chatId).collection("messages").document(messageId)
        try {
            batch.update(messageRef, "media", media)
            batch.commit().await()
            // Log.d("FireStore Operation", "Messages Edited Successfully")
        } catch (e: Exception) {
            // Log.d("FireStore Operation", "Error in Message Edition - ${e.message}")
        }
    }


    suspend fun deleteMessages(
        messages: List<String>,
        chatId: String,
        deleteFor: Int = 1,
        onCleared: () -> Unit
    ) {
        val chatRef = chatCollection.document(chatId).collection("messages")

        try {
            val batch = db.batch()
            for (messageId in messages) {
                val messageRef = chatRef.document(messageId)
                if (deleteFor == 2) {
                    batch.update(
                        messageRef,
                        "message", "deleted",
                        "reactions", emptyMap<String, String>()
                    )
                } else {
                    batch.update(messageRef, "members", FieldValue.arrayRemove(currentUser))
                }
            }
            batch.commit().await()
            onCleared()

            // For delete for me, we also want to clean up documents that have empty members.
            // We do this after the batch commit to ensure consistency.
            if (deleteFor == 1) {
                coroutineScope {
                    messages.map { messageId ->
                        async {
                            try {
                                val messageRef = chatRef.document(messageId)
                                val members = messageRef.get().await().get("members") as? List<*> ?: emptyList<Any>()
                                if (members.isEmpty()) messageRef.delete().await()
                            } catch (e: Exception) {
                                // Ignore cleanup failures
                            }
                        }
                    }.awaitAll()
                }
            }
        } catch (e: Exception) {
            // Log.e("Firestore", "Error in message deletion: ${e.message}")
        }
    }

    suspend fun clearLastMessage(chatId: String) {
        val chatRef = chatCollection.document(chatId)

        val batch = db.batch()

        try {
            batch.update(chatRef, mapOf("lastMessage" to ""))
            batch.update(chatRef, mapOf("lastMessageTimestamp" to Timestamp(0, 0)))
            batch.commit().await()
            // Log.d("FireStore Operation", "Messages Edited Successfully")
        } catch (e: Exception) {
            // Log.d("FireStore Operation", "Error in Message Edition - ${e.message}")
        }
    }


    suspend fun deleteAllMessages(
        chatId: String
    ) = coroutineScope {
        val chatRef = chatCollection.document(chatId).collection("messages")

        try {
            val snapshot = chatRef.get().await()
            val documents = snapshot.documents
            val chunks = documents.chunked(500)

            // Launch parallel deletions
            val jobs = chunks.map { chunk ->
                async {
                    val batch = db.batch()
                    chunk.forEach { doc ->
                        val members = doc.get("members") as? List<String> ?: emptyList()

                        batch.update(
                            doc.reference,
                            mapOf("members" to FieldValue.arrayRemove(currentUser))
                        )
                        if (members.size == 1 && members.contains(currentUser)) {
                            batch.delete(doc.reference)
                        }
                    }
                    batch.commit().await()
                }
            }

            jobs.awaitAll() // Wait for all parallel deletions to finish
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun updateMessageStatusToSeen(chatId: String, messages: List<Message>) {
        val batch = db.batch()
        for (message in messages.reversed()) {
            if (message.senderId == currentUser || message.status == "seen") break
            val messageRef = chatCollection
                .document(chatId)
                .collection("messages")
                .document(message.messageId)
            //chatCollection.document(chatId).update("unreadMessages.$currentUser", 0)
            if (message.members.contains(currentUser)) {
                batch.update(messageRef, "status", "seen")
            }
        }

        batch.commit().addOnCompleteListener {
            if (it.isSuccessful) {
                // Log.d("FireStore Operation", "All messages status updated to 'seen'")
            } else {
                // Log.e("FireStore Operation", "Error updating messages status", it.exception)
            }
        }
    }

    fun updateUnreadMessages(chatId: String) {
        chatCollection.document(chatId).update("unreadMessages.$currentUser", 0)
    }

    fun getMessages(user1Id: String, user2Id: String): Flow<Response<List<Message>>> = flow {
        emit(Response.Loading)

        val chatId = generateChatId(user1Id, user2Id)
        try {
            val snapshotFlow = callbackFlow {
                val listenerRegistration = chatCollection.document(chatId)
                    .collection("messages")
                    .orderBy("timestamp")
                    .addSnapshotListener { snapshot, exception ->
                        if (exception != null) {
                            trySend(Response.Error(exception.message ?: "Unknown error")).isFailure
                        } else {
                            val encryptedMessages =
                                snapshot?.toObjects(Message::class.java) ?: emptyList()

                            val decryptedMessages = encryptedMessages.filter { it.members.contains(currentUser) }.map { msg ->
                                try {
                                    if (msg.media != null) {
                                        val decryptedMedia = msg.media?.copy(
                                            mediaUrl = ChatCryptoUtil.decrypt(
                                                msg.media.mediaUrl,
                                                msg.chatId
                                            )
                                        )
                                        msg.copy(media = decryptedMedia)
                                    } else if(msg.message != "deleted"){
                                        var decryptedText = ChatCryptoUtil.decrypt(
                                            msg.message,
                                            msg.chatId
                                        )
                                        if(decryptedText.isNullOrEmpty()){
                                            decryptedText = msg.message
                                        }
                                        msg.copy(
                                            message = decryptedText.ifBlank { msg.message }
                                        )
                                    } else {
                                        msg
                                    }
                                } catch (e: Exception) {
                                    Message()
                                }
                            }
                            if (!activeChatId.isNullOrEmpty()) {
                                updateUnreadMessages(chatId)
                                updateMessageStatusToSeen(chatId, decryptedMessages)
                            }
                            val result = trySend(Response.Success(decryptedMessages))
                            if (result.isFailure) {
                                // Log or handle the failure (optional)
                                // Log.e("Firestore", "Failed to send messages to the flow.")
                            }

                        }
                    }
                awaitClose { listenerRegistration.remove() }
            }
            emitAll(snapshotFlow)
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Failed to load messages"))
        }
    }

    /*suspend fun getChatList(userId: String): Flow<Response<List<ChatList>>> = callbackFlow {
        trySend(Response.Loading)

        val userDocument = userCollection.document(userId)
        val chatListeners = mutableListOf<ListenerRegistration>()
        val chatList = mutableListOf<ChatList>()

        // Step 1: Listen for updates on the user's document to fetch user list
        val userDocumentListener = userDocument.addSnapshotListener { userSnapshot, userException ->
            if (userException != null) {
                trySend(
                    Response.Error(
                        userException.message ?: "Error fetching user list"
                    )
                ).isFailure
                return@addSnapshotListener
            }

            val userIds = userSnapshot?.get("user_list") as? List<String> ?: emptyList()
            Log.d("FireStoreService", "Document data: ${userSnapshot?.data}")
            Log.d("FireStoreService", "users -  $userIds")
            if (userIds.isEmpty()) {
                trySend(Response.Success(emptyList())).isFailure
            } else {
                // Clear previous listeners to avoid duplicate data
//                chatListeners.forEach { it.remove() }
//                chatListeners.clear()
//                chatList.clear()

                // Step 2: Add listeners for each user's chat
                userIds.forEach { otherUserId ->
                    val chatId = generateChatId(userId, otherUserId)
                    val chatDocument = chatCollection.document(chatId)

                    val chatListener =
                        chatDocument.addSnapshotListener { chatSnapshot, chatException ->
                            if (chatException != null) {
                                trySend(
                                    Response.Error(
                                        chatException.message ?: "Error fetching chat data"
                                    )
                                ).isFailure
                                return@addSnapshotListener
                            }

                            if (chatSnapshot != null) {
                                val lastMessage = chatSnapshot.getString("lastMessage")
                                val lastMessageTimestamp =
                                    chatSnapshot.get("lastMessageTimestamp") as? Timestamp
                                val unreadMessage =
                                    chatSnapshot.get("unreadMessages.$userId") as? Long ?: 0

                                userCollection.document(otherUserId).get()
                                    .addOnSuccessListener { userSnapshot ->
                                        val name = userSnapshot.getString("name")
                                        val profilePicture =
                                            userSnapshot.getString("profilePicture")

                                        if (name != null && profilePicture != null && lastMessage != null && lastMessageTimestamp != null) {
                                            val chat = ChatList(
                                                userId = otherUserId,
                                                name = name,
                                                profilePicture = profilePicture,
                                                lastMessage = lastMessage,
                                                lastMessageTimestamp = lastMessageTimestamp,
                                                unreadMessages = unreadMessage
                                            )

                                            // Replace or update the chat entry for this user
                                            chatList.removeIf { it.userId == otherUserId }
                                            chatList.add(chat)

                                            val sortedChatList =
                                                chatList.sortedByDescending { it.lastMessageTimestamp.toDate() }

                                            Log.d("FireStoreService", sortedChatList.toString())

                                            // Send the updated chat list to the flow
                                            trySend(Response.Success(sortedChatList)).isFailure
                                        }
                                    }
//                                    .addOnFailureListener {
//                                        trySend(
//                                            Response.Error(
//                                                it.message ?: "Error fetching user details"
//                                            )
//                                        ).isFailure
//                                    }
                            }
                        }
                    chatListeners.add(chatListener)
                }
            }
        }

        // Cleanup listeners when the flow is canceled
        awaitClose {
            userDocumentListener.remove()
            chatListeners.forEach { it.remove() }
        }
    }*/


    suspend fun blockUser(chatId: String, userId: String): Response<Boolean> {
        return try {
            val chatDoc = chatCollection.document(chatId)
            val snapshot = chatDoc.get().await()

            if (snapshot.exists()) {
                chatDoc.update("blockUsers.$userId", userId).await()
            } else {
                chatDoc.set(
                    mapOf("blockUsers" to mapOf(userId to userId))
                ).await()
            }

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Failed to block user")
        }
    }


    suspend fun unblockUser(chatId: String, userId: String): Response<Boolean> {
        return try {
            val chatDoc = chatCollection.document(chatId)
            val snapshot = chatDoc.get().await()

            if (snapshot.exists() && snapshot.contains("blockUsers.$userId")) {
                chatDoc.update("blockUsers.$userId", FieldValue.delete()).await()
            }

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Failed to unblock user")
        }
    }


    suspend fun sendReportToAdmin(
        curUserId: String,
        reportedUserId: String,
        reason: String,
        proofText: String?,
        proofImageUrl: String?
    ): Response<Boolean> {
        return try {
            val reportData = mutableMapOf<String, Any>(
                "reporterId" to curUserId,
                "reportedUserId" to reportedUserId,
                "reason" to reason.trim(),
                "timestamp" to Timestamp.now()
            )

            proofImageUrl?.takeIf { it.isNotEmpty() }?.let {
                reportData["proofImageUrl"] = it
            }

            proofText?.takeIf { it.isNotBlank() }?.let {
                reportData["proofText"] = it.trim()
            }

            db.collection("admin")
                .document("reports")
                .collection("userReports")
                .add(reportData)
                .await()

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }


    fun getBlockDetails(chatId: String): Flow<Response<Set<String>>> = callbackFlow {
        trySend(Response.Loading)

        val listener = chatCollection.document(chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Unknown error")).isFailure
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val blockUsersMap =
                        snapshot.get("blockUsers") as? Map<*, *> ?: emptyMap<Any, Any>()
                    val blockedUserIds = blockUsersMap.keys.mapNotNull { it?.toString() }.toSet()
                    trySend(Response.Success(blockedUserIds))
                } else {
                    trySend(Response.Success(emptySet()))
                }
            }

        awaitClose { listener.remove() }
    }


    suspend fun getChatList(userId: String): Flow<Response<List<ChatList>>> = callbackFlow {
        trySend(Response.Loading)

        val userDocument = userCollection.document(userId)
        val activeListeners = mutableMapOf<String, ListenerRegistration>()
        val chatCache = mutableMapOf<String, ChatList>()
        val scope = CoroutineScope(Dispatchers.IO + Job())

        // Listen for user list changes
        val userListListener = userDocument.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Response.Error(error.message ?: "User data error"))
                return@addSnapshotListener
            }

            val userIds = snapshot?.get("user_list") as? List<String> ?: emptyList()
            if (userIds.isEmpty()) {
                trySend(Response.Success(emptyList()))
                return@addSnapshotListener
            }
            manageChatListeners(userId, userIds, activeListeners, chatCache, scope, this)
        }

        awaitClose {
            userListListener.remove()
            activeListeners.values.forEach { it.remove() }
            scope.cancel()
        }
    }


    private val updateChannel = Channel<Unit>(Channel.CONFLATED)
    private var lastSentChatList = listOf<ChatList>()
    private val cacheMutex = Mutex() // To synchronize access to the chat cache

    private fun manageChatListeners(
        userId: String,
        userIds: List<String>,
        activeListeners: MutableMap<String, ListenerRegistration>,
        chatCache: MutableMap<String, ChatList>,
        scope: CoroutineScope,
        channel: ProducerScope<Response<List<ChatList>>>
    ) {
        // Debounce updates to ensure we're not sending updates too frequently
        scope.launch {
            updateChannel.consumeEach {
                delay(100)  // Adjust debounce window as needed
                cacheMutex.withLock {
                    val sortedChats =
                        chatCache.values.sortedByDescending { it.lastMessageTimestamp }
                    if (sortedChats != lastSentChatList) {
                        lastSentChatList = sortedChats
                        channel.trySend(Response.Success(sortedChats))
                    }
                }
            }
        }

        // Remove listeners for users no longer in the list
        val removedUsers = activeListeners.keys - userIds.toSet()
        removedUsers.forEach {
            activeListeners.remove(it)?.remove()
            chatCache.remove(it)
        }

        // Add listeners for new users
        userIds.forEach { otherUserId ->
            if (activeListeners.contains(otherUserId)) return@forEach

            val chatId = generateChatId(userId, otherUserId)
            val chatDoc = chatCollection.document(chatId)

            // Add a snapshot listener for each chat that the user is involved in
            activeListeners[otherUserId] = chatDoc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    channel.trySend(Response.Error(error.message ?: "Chat data error"))
                    return@addSnapshotListener
                }

                scope.launch {
                    if (snapshot == null || !snapshot.exists()) return@launch

                    val lastMessage = snapshot.getString("lastMessage") ?: ""
                    val timestamp = snapshot.getTimestamp("lastMessageTimestamp") ?: Timestamp.now()
                    val unread = snapshot.getLong("unreadMessages.$userId") ?: 0
                    val blockUsersMap =
                        snapshot.get("blockUsers") as? Map<*, *> ?: emptyMap<Any, Any>()
                    val blockedUserIds = blockUsersMap.keys.mapNotNull { it?.toString() }.toSet()

                    // Parallel user data fetch
                    val userData = async { fetchUserData(otherUserId) }
                    val profileData = userData.await()

                    profileData?.let {
                        val isCurUserBlocked = blockedUserIds.contains(currentUser)
                        val isOtherUserBlocked = blockedUserIds.contains(otherUserId)

                        val reqMap = snapshot.get("messageRequest") as? Map<*, *>
                        val reqStatus = reqMap?.get("status") as? String ?: STATUS_NONE
                        val reqSenderId = reqMap?.get("senderId") as? String ?: ""

                        val chat = ChatList(
                            userId = otherUserId,
                            name = profileData.name,
                            profilePicture = if (isCurUserBlocked) "" else profileData.profilePicture,
                            lastMessage = if (isOtherUserBlocked) "" else lastMessage,
                            lastMessageTimestamp = if (isOtherUserBlocked) Timestamp(0, 0) else timestamp,
                            unreadMessages = if (isOtherUserBlocked) 0 else unread,
                            fcmToken = profileData.fcmToken,
                            isCurUserBlock = isCurUserBlocked,
                            isOtherBlock = isOtherUserBlocked,
                            requestStatus = reqStatus,
                            requestSenderId = reqSenderId
                        )

                        // Only update if the chat data has changed
                        cacheMutex.withLock {
                            chatCache[otherUserId] = chat
                        }

                        // Send updates via the channel after debouncing
                        updateChannel.trySend(Unit)
                    }
                }
            }
        }

        // Send updated chat list if there's new data
        if (chatCache.isNotEmpty()) {
            scope.launch {
                updateChannel.trySend(Unit)
            }
        }
    }


    /*private fun manageChatListeners(
        userId: String,
        userIds: List<String>,
        activeListeners: MutableMap<String, ListenerRegistration>,
        chatCache: MutableMap<String, ChatList>,
        scope: CoroutineScope,
        channel: ProducerScope<Response<List<ChatList>>>
    ) {
        // Remove listeners for users no longer in the list
        val removedUsers = activeListeners.keys - userIds.toSet()
        removedUsers.forEach {
            activeListeners.remove(it)?.remove()
            chatCache.remove(it)
        }

        // Add listeners for new users
        userIds.forEach { otherUserId ->
            if (activeListeners.contains(otherUserId)) return@forEach

            val chatId = generateChatId(userId, otherUserId)
            val chatDoc = chatCollection.document(chatId)

            activeListeners[otherUserId] = chatDoc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    channel.trySend(Response.Error(error.message ?: "Chat data error"))
                    return@addSnapshotListener
                }

                scope.launch {
                    if (snapshot == null || !snapshot.exists()) return@launch

                    val lastMessage = snapshot.getString("lastMessage") ?: ""
                    val timestamp = snapshot.getTimestamp("lastMessageTimestamp") ?: Timestamp.now()
                    val unread = snapshot.getLong("unreadMessages.$userId") ?: 0
                    val block = snapshot.getString("blockUsers.$userId") ?: ""

                    // Parallel user data fetch
                    val userData = async { fetchUserData(otherUserId) }
                    val profileData = userData.await()

                    profileData?.let {
                        val chat = ChatList(
                            userId = otherUserId,
                            name = profileData.name,
                            profilePicture = profileData.profilePicture,
                            lastMessage = lastMessage,
                            lastMessageTimestamp = timestamp,
                            unreadMessages = unread,
                            fcmToken = profileData.fcmToken,
                            isBlock = block.isNotEmpty()
                        )

                        chatCache[otherUserId] = chat
                        channel.trySend(Response.Success(chatCache.values.sortedByDescending { it.lastMessageTimestamp }))
                    }
                }
            }
        }

        // Send updated list immediately if we have cached data
        if (chatCache.isNotEmpty()) {
            channel.trySend(Response.Success(chatCache.values.sortedByDescending { it.lastMessageTimestamp }))
        }
    }*/

    private suspend fun fetchUserData(userId: String): User? {
        return try {
            val snapshot = userCollection.document(userId).get().await()
            val name = snapshot.getString("name") ?: return null
            val profilePic = snapshot.getString("profilePicture") ?: ""
            val fcmToken = snapshot.getString("fcmToken") ?: ""
            val user = User(name = name, profilePicture = profilePic, fcmToken = fcmToken)
            user
        } catch (e: Exception) {
            null
        }
    }
}
