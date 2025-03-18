package com.exa.android.reflekt.loopit.mvvm.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.generateChatId
import com.exa.android.reflekt.loopit.util.model.Status
import com.exa.android.reflekt.loopit.util.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val rdb: FirebaseDatabase // Realtime Database
) {

    val currentUser = auth.currentUser?.uid

    private val userCollection = db.collection("users")
    private val chatCollection = db.collection("chats")
    private val userStatusRef = rdb.getReference("/status/$currentUser")

    suspend fun updateUserStatus(curUser: String, isOnline: Boolean) {
        val data = mapOf(
            "isOnline" to if(isOnline)true else false,
            "lastSee" to if(isOnline)null else Timestamp.now()
        )
        try {
            userStatusRef.updateChildren(data).await()
            Log.d("Firebase Operation", "User status updated for $curUser.")
        } catch (e: Exception) {
            Log.e("Firebase Operation", "Failed to update user status", e)
        }
    }

    fun observeUserConnectivity() {
        val onlineStatus = mapOf(
            "isOnline" to true,
            "lastSeen" to null,
            "typingTo" to ""
        )
        val offlineStatus = mapOf(
            "isOnline" to false,
            "lastSeen" to Timestamp.now(),
            "typingTo" to ""
        )

        val connectRef = rdb.getReference(".info/connected")
        connectRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    userStatusRef.setValue(onlineStatus)
                    userStatusRef.onDisconnect().setValue(offlineStatus)
                    Log.d("Firebase Operation", "User is online, status updated.")
                } else {
                    userStatusRef.setValue(offlineStatus)
                    Log.d("Firebase Operation", "User is offline, status updated.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase Operation", "Error observing connection status", error.toException())
            }
        })
    }

    suspend fun setTypingStatus(curUser: String, typingTo: String?) {
        val data = mapOf("typingTo" to typingTo)
        try {
            userStatusRef.updateChildren(data).await()
            Log.d("Firebase Operation", "Typing status updated for $curUser.")
        } catch (e: Exception) {
            Log.e("Firebase Operation", "Failed to update typing status", e)
        }
    }

    suspend fun updateUnreadMessages(curUser: String, otherUser: String) {
        val chatID = generateChatId(curUser, otherUser)
        val chatDocRef = chatCollection.document(chatID)
        try {
            chatDocRef.update("unreadMessages.$curUser", 0).await()
            Log.d("Firebase Operation", "Unread messages for $curUser successfully reset to 0.")
        } catch (e: Exception) {
            Log.e("Firebase Operation", "Failed to reset unread messages", e)
        }
    }

    suspend fun getUserDetail(userId: String): Flow<Response<User?>> = callbackFlow {
        try {
            trySend(Response.Loading).isFailure // Emit loading state

            val listenerRegistration = userCollection.document(userId)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        trySend(Response.Error(exception.localizedMessage ?: "Error fetching user")).isFailure
                    } else if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject(User::class.java)
                        if (user != null) {
                            trySend(Response.Success(user)).isFailure
                        } else {
                            trySend(Response.Error("Failed to parse user data")).isFailure
                        }
                    } else {
                        trySend(Response.Error("User not found")).isFailure
                    }
                }

            awaitClose { listenerRegistration.remove() }
        } catch (e: Exception) {
            trySend(Response.Error(e.localizedMessage ?: "Unknown error")).isFailure
        }
    }

    fun getUserStatus(userId: String): LiveData<Status?> {
        val path = rdb.getReference("/status/$userId")
        val liveData = MutableLiveData<Status?>()
        path.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(Status::class.java) ?: Status() // Default to Status()
                liveData.postValue(status)
            }

            override fun onCancelled(error: DatabaseError) {
                liveData.postValue(null) // Handle the error by posting null
            }
        })
        return liveData
    }
}