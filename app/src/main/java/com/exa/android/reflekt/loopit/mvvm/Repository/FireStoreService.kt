package com.exa.android.reflekt.loopit.mvvm.Repository

import android.util.Log
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.generateChatId
import com.exa.android.reflekt.loopit.util.model.ChatList
import com.exa.android.reflekt.loopit.util.model.Message
import com.exa.android.reflekt.loopit.util.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreService @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private val userCollection = db.collection("users")
    private val chatCollection = db.collection("chats")
    val currentUser = auth.currentUser?.uid

    //search user based on the phone number
    suspend fun searchUser(phone: String): Flow<Response<User?>> = flow {
        emit(Response.Loading)
        try {
            val snapshot = userCollection.whereEqualTo("phone", phone).get().await()
            if (!snapshot.isEmpty) {
                val user = snapshot.documents[0].toObject(User::class.java)
                emit(Response.Success(user))
            } else {
                emit(Response.Success(null)) // No user found
            }
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "An unexpected error occurred"))
        }
    }


    fun insertUser(userName: String, phone: String) {
        val userId = auth.currentUser?.uid
        val user = User(
            name = userName,
            phone = phone,
            profilePicture = "https://example.picture",
            userId = userId!!
        )
        try {
            userCollection.document(user.userId).set(user)
                .addOnSuccessListener { Log.d("FireStoreService", "New user added Successfully") }
                .addOnFailureListener { Log.d("FireStoreService", "New user added Failed") }
        } catch (e: Exception) {
            // handel Exception
        }
    }

    suspend fun createChatAndSendMessage(userId2: String, text: String) {
        val userId1 = auth.currentUser?.uid
        val chatId = generateChatId(userId1!!, userId2)
        val message = Message(
            chatId = chatId,
            senderId = userId1,
            receiverId = userId2,
            message = text,
            members = listOf(userId1, userId2)
        )
        try {

            updateUserList(userId1, userId2)
            updateUserList(userId2, userId1)
            val messageRef = chatCollection.document(chatId).collection("messages")

            messageRef.document(message.messageId).set(message)
                .addOnSuccessListener {
                    Log.d(
                        "FireStoreService",
                        "New message added Successfully"
                    )
                }
                .addOnFailureListener { Log.d("FireStoreService", "New message added Failed") }

            // Update last message and timestamp in the chat document
            val chatDoc = chatCollection.document(chatId)
            val snapshot = chatDoc.get().await()

            if (snapshot.exists()) {
                // Document exists, perform update
                chatDoc.update(
                    "lastMessage", message.message,
                    "lastMessageTimestamp", message.timestamp,
                    "unreadMessages.$userId2", FieldValue.increment(1),
                    "unreadMessages.$userId1", 0
                ).await()
            } else {
                // Document does not exist, create it with initial values
                val data = chatDoc.set(
                    mapOf(
                        "lastMessage" to message.message,
                        "lastMessageTimestamp" to message.timestamp,
                        "unreadMessages" to mapOf(userId1 to 0, userId2 to 1)
                    )

                ).await()
                Log.d("FireStore Operation", data.toString())
            }


            // Insert or update the chat document for both users
            val chat1 = mapOf("users" to listOf(userId1, userId2))
            val chat2 = mapOf("users" to listOf(userId2, userId1))

            val chat1Doc = chatCollection.document(chatId).get().await()
            if (!chat1Doc.exists()) {
                chatCollection.document(chatId).set(chat1).await()
            }

            val chat2Doc = chatCollection.document(chatId).get().await()
            if (!chat2Doc.exists()) {
                chatCollection.document(chatId).set(chat2).await()
            }

        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun updateUserList(currentUser: String, newUser: String) {
        val userRef = userCollection.document(currentUser)
        userRef.update("user_list", FieldValue.arrayUnion(newUser))
            .addOnSuccessListener { println("User chats updated successfully") }
            .addOnFailureListener { e -> println("Error updating user chats: ${e.message}") }
    }

    suspend fun updateMessage(message: Message, newText: String) {
        val chatId = message.chatId
        val batch = db.batch()
        val messageRef =
            chatCollection.document(chatId).collection("messages").document(message.messageId)
        try {
            batch.update(messageRef, mapOf("message" to newText))
            batch.commit().await()
            Log.d("FireStore Operation", "Messages Edited Successfully")
        } catch (e: Exception) {
            Log.d("FireStore Operation", "Error in Message Edition - ${e.message}")
        }
    }


    suspend fun deleteMessages(
        messages: List<String>,
        chatId: String,
        deleteFor: Int = 1,
        onCleared: () -> Unit
    ) {

        val chatRef = chatCollection.document(chatId).collection("messages")

        val batch = db.batch()
        try {
            for (messageId in messages) {
                val messageRef = chatRef.document(messageId)
                if (deleteFor == 2) {
                    batch.update(messageRef, mapOf("message" to "deleted"))
                } else {
                    batch.update(
                        messageRef,
                        mapOf("members" to FieldValue.arrayRemove(currentUser))
                    )
                    val members = messageRef.get().await().get("members") as List<String>
                    if (members.isEmpty()) batch.delete(messageRef)
                }
            }
            batch.commit().await()

            Log.d("FireStore Operation", "Messages Deleted Successfully")
            onCleared()
        } catch (e: Exception) {
            Log.d("FireStore Operation", "Error in Message Deletion - ${e.message}")
        }
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
                            val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                            val result = trySend(Response.Success(messages))
                            if (result.isFailure) {
                                // Log or handle the failure (optional)
                                Log.e("Firestore", "Failed to send messages to the flow.")
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

    suspend fun getChatList(userId: String): Flow<Response<List<ChatList>>> = callbackFlow {
        trySend(Response.Loading)

        val userDocument = userCollection.document(currentUser!!)
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
                chatListeners.forEach { it.remove() }
                chatListeners.clear()
                chatList.clear()

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
                                    .addOnFailureListener {
                                        trySend(
                                            Response.Error(
                                                it.message ?: "Error fetching user details"
                                            )
                                        ).isFailure
                                    }
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
    }

}
