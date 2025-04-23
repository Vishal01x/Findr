package com.exa.android.reflekt.loopit.data.remote.main.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.exa.android.reflekt.loopit.util.Response
import com.exa.android.reflekt.loopit.util.generateChatId
import com.exa.android.reflekt.loopit.util.model.Chat
import com.exa.android.reflekt.loopit.util.model.Profile.CollegeInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExperienceInfo
import com.exa.android.reflekt.loopit.util.model.Profile.ExtraActivity
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileData
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileHeaderData
import com.exa.android.reflekt.loopit.util.model.Status
import com.exa.android.reflekt.loopit.util.model.User
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
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


    suspend fun getCurUser(): User? {
        val user = userCollection.document(currentUser!!).get().await()
        return user.toObject(User::class.java)
    }

    suspend fun updateUserStatus(curUser: String, isOnline: Boolean) {
        val data = mapOf(
            "isOnline" to if (isOnline) true else false,
            "lastSeen" to if (isOnline) null else Timestamp.now().seconds
        )
        try {
            userStatusRef.updateChildren(data).await()
            // Log.d("Firebase Operation", "User status updated for $curUser to $data.")
        } catch (e: Exception) {
            // Log.e("Firebase Operation", "Failed to update user status", e)
        }
    }

    fun observeUserConnectivity() {
        val onlineStatus = mapOf(
            "isOnline" to true,
            "lastSeen" to Timestamp.now().seconds,
            "typingTo" to ""
        )
        val offlineStatus = mapOf(
            "isOnline" to false,
            "lastSeen" to Timestamp.now().seconds,
            "typingTo" to ""
        )
        // Log.d("Firebase Operation", "User status updated for $currentUser to $onlineStatus.")
        val connectRef = rdb.getReference(".info/connected")
        connectRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    userStatusRef.setValue(onlineStatus)
                    userStatusRef.onDisconnect().setValue(offlineStatus)
                    // Log.d("Firebase Operation", "User is online, status updated.")
                } else {
                    userStatusRef.setValue(offlineStatus)
                    // Log.d("Firebase Operation", "User is offline, status updated.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                /*
                Log.e(
                    "Firebase Operation",
                    "Error observing connection status",
                    error.toException()
                )

                 */
            }
        })
    }

    suspend fun setTypingStatus(id: String, typingTo: String?) {
        val statusRef = rdb.getReference("/status/$id/typingTo")
        try {
            statusRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val curStatus = currentData.getValue(String::class.java) ?: ""
                    // Only update if the conditions are met
                    if (curStatus == "" || typingTo == "") {
                        currentData.value = typingTo
                    }
                    return Transaction.success(currentData)  // Indicate the transaction was successful
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error != null) {
                        //  Log.e("Firebase Operation", "Transaction failed: $error")
                    } else {
                        /*
                        Log.d(
                            "Firebase Operation",
                            "Transaction completed with success: $committed"
                        )

                         */
                    }
                }
            })

            // Set up onDisconnect to clear typing status
            statusRef.onDisconnect().setValue("").await()

            // Log.d("Firebase Operation", "Typing status updated for $id.")
        } catch (e: Exception) {
            // Log.e("Firebase Operation", "Failed to update typing status", e)
        }
    }


    suspend fun updateUnreadMessages(chatId: String) {
        val chatDocRef = chatCollection.document(chatId)
        try {
            chatDocRef.update("unreadMessages.$currentUser", 0).await()
            /*
            Log.d(
                "Firebase Operation",
                "Unread messages for ${currentUser} successfully reset to 0."
            )

             */
        } catch (e: Exception) {
            // Log.e("Firebase Operation", "Failed to reset unread messages", e)
        }
    }

//    fun makeCall(call : Call){
//        val callRef = db.collection(currentUser!!).
//    }


    fun getChatRoomDetail(chatId: String): Flow<Response<Chat?>> = callbackFlow {
        try {
            trySend(Response.Loading) // Emit loading state

            val listenerRegistration = chatCollection.document(chatId)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        trySend(Response.Error(exception.localizedMessage ?: "Error fetching user"))
                    } else if (snapshot != null && snapshot.exists()) {
                        val chat = snapshot.toObject(Chat::class.java)
                        if (chat != null) {
                            trySend(Response.Success(chat))
                        } else {
                            trySend(Response.Error("Failed to parse user data"))
                            // Log.e("Firebae Operation", "Failed to parse user data")
                        }
                    } else {
                        trySend(Response.Error("User not found"))
                        // Log.e("Firebae Operation", "User not found")
                    }
                }

            awaitClose {
                listenerRegistration.remove() // Ensure listener is removed on cancellation
            }
        } catch (e: Exception) {
            trySend(Response.Error(e.localizedMessage ?: "Unknown error"))
            // Log.e("Firebae Operation", e.localizedMessage ?: "Unknown error")
        }
    }

    fun getUserStatus(id: String): LiveData<Status?> {
        val path = rdb.getReference("/status/$id")
        val liveData = MutableLiveData<Status?>()
        path.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Log.d("Firebase Operation", "Firebase Raw Data - ${snapshot.value.toString()}") // Print raw Firebase data

                val dataMap = snapshot.value as? Map<String, Any>

                val isOnline = dataMap?.get("isOnline") as? Boolean ?: false
                val lastSeen = dataMap?.get("lastSeen") as? Long
                val typingTo = dataMap?.get("typingTo") as? String ?: ""

                val status = Status(isOnline, lastSeen, typingTo)// Default to Status()

                liveData.postValue(status)
                // Log.d("Firebase Operation", status.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                liveData.postValue(null) // Handle the error by posting null
            }
        })
        return liveData
    }

    suspend fun getAllUser(): Flow<Response<List<User?>>> = flow {
        // Log.d("FireStore Operation", " users - ")
        emit(Response.Loading)
        try {
            val snapshotFlow = callbackFlow {

                val usersPath = userCollection.addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        // Log.d("FireStore Operation", " users - ${exception.message}")
                        exception.message?.let { Response.Error(it) }
                            ?.let { trySend(it) }?.isFailure
                    } else {
                        val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                        // Log.d("FireStore Operation", " users - $users")
                        val updatedUsers =
                            users.filter { it.userId != currentUser }.sortedBy { it.name }
                        val result = trySend(Response.Success(updatedUsers))
                        if (result.isFailure) {
                            // Log or handle the failure (optional)
                            // Log.e("FireStore Operation", "Failed to send messages to the flow.")
                        }
                    }
                }
                awaitClose {
                    usersPath.remove()
                }
            }
            emitAll(snapshotFlow)
        } catch (e: Exception) {
            e.message?.let { Response.Error(it) }?.let { emit(it) }
        }

    }


    suspend fun getUserDetail(userId: String): Flow<Response<User?>> = callbackFlow {
        try {
            trySend(Response.Loading).isFailure // Emit loading state

            val listenerRegistration = userCollection.document(userId)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        trySend(
                            Response.Error(
                                exception.localizedMessage ?: "Error fetching user"
                            )
                        ).isFailure
                    } else if (snapshot != null && snapshot.exists()) {
                        val name = snapshot.getString("name") ?: "Name"
                        val profilePic = snapshot.getString("profilePicture") ?: ""
                        val fcmToken = snapshot.getString("fcmToken") ?: ""
                        val user = User(
                            name = name,
                            profilePicture = profilePic,
                            fcmToken = fcmToken
                        )
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

    suspend fun updateUnreadMessages(curUser: String, otherUser: String) {
        val chatID = generateChatId(curUser, otherUser)
        val chatDocRef = chatCollection.document(chatID)
        try {
            chatDocRef.update("unreadMessages.$curUser", 0).await()
            // Log.d("Firebase Operation", "Unread messages for $curUser successfully reset to 0.")
        } catch (e: Exception) {
            // Log.e("Firebase Operation", "Failed to reset unread messages", e)
        }
    }


    suspend fun updateOrCreateProfileHeader(profileHeader: ProfileHeaderData) {
        val userDocRef = userCollection.document(currentUser!!)

        Firebase.firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDocRef)

            // Check if document exists
            if (!snapshot.exists()) {
                // Create document with profileHeader
                val newUser = mapOf(
                    "profileData" to mapOf("profileHeader" to profileHeader)
                )
                transaction.set(userDocRef, newUser, SetOptions.merge())
            } else {
                // Document exists, just update the profileHeader
                transaction.update(userDocRef, "profileData.profileHeader", profileHeader)
            }
        }.await()
    }

    suspend fun updateUserNameAndImage(name: String, imageUrl: String): Response<Unit> {
        return try {
            val updates = mapOf(
                "name" to name,
                "profilePicture" to imageUrl
            )

            userCollection
                .document(currentUser!!)
                .update(updates)
                .await()

            Response.Success(Unit)
        } catch (e: Exception) {
            // You can log the error to Crashlytics or a logging system
            // Log.e("Firestore Service", "Failed to update user profile", e)
            Response.Error(e.localizedMessage ?: "Unknown error occurred")
        }

    }


    fun updateUserAbout(description: String) = flow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId.isNullOrBlank()) {
            emit(Response.Error("User not logged in"))
            return@flow
        }

        val userRef = userCollection.document(userId)

        try {
            emit(Response.Loading)

            val snapshot = userRef.get().await()

            val updateMap = mapOf("profileData.about.description" to description)

            if (snapshot.exists()) {
                // Update only about field
                userRef.update(updateMap).await()
                emit(Response.Success(Unit))
            } else {
                // Create whole structure if doc doesn't exist
                val initialData = mapOf(
                    "profileData" to mapOf(
                        "about" to mapOf("description" to description)
                    )
                )
                userRef.set(initialData).await()
                emit(Response.Success(Unit))
            }

        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Unexpected error occurred"))
        }
    }


    fun updateUserSkill(skills: List<String>) = flow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val skill = skills.joinToString(separator = ",") { it.trim() }

        if (userId.isNullOrBlank()) {
            emit(Response.Error("User not logged in"))
            return@flow
        }

        val userRef = userCollection.document(userId)

        try {
            emit(Response.Loading)

            val snapshot = userRef.get().await()

            val updateMap = mapOf("profileData.skill" to skill)

            if (snapshot.exists()) {
                // Update only about field
                userRef.update(updateMap).await()
                emit(Response.Success(Unit))
            } else {
                // Create whole structure if doc doesn't exist
                val initialData = mapOf("profileData" to mapOf("skill" to skill))
                userRef.set(initialData).await()
                emit(Response.Success(Unit))
            }

        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Unexpected error occurred"))
        }
    }

    // Update or Insert Activity
    suspend fun updateExtraActivity(activity: ExtraActivity) {
        try {
            // Check if the activity already exists using its activityId
            val docRef = userCollection
                .document(currentUser!!)
                .collection("extraActivity")
                .document(activity.id)

            // Check if the activity exists
            val documentSnapshot = docRef.get().await()

            if (documentSnapshot.exists()) {
                // If the activity exists, update it
                docRef.set(activity).await()
            } else {
                // If the activity doesn't exist, insert it
                docRef.set(activity).await()
            }
        } catch (e: Exception) {
            throw Exception("Error updating or inserting activity: ${e.message}")
        }
    }

    suspend fun deleteExtraActivity(activityId: String) {
        try {
            val docRef = userCollection
                .document(currentUser!!)
                .collection("extraActivity")
                .document(activityId)

            docRef.delete().await()
        } catch (e: Exception) {
            throw Exception("Error deleting activity: ${e.message}")
        }
    }


    //update User Education
    fun updateUserEducation(collegeInfo: CollegeInfo) = flow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId.isNullOrBlank()) {
            emit(Response.Error("User not logged in"))
            return@flow
        }

        val userRef = userCollection.document(userId)

        try {
            emit(Response.Loading)

            val snapshot = userRef.get().await()

            val updateMap = mapOf("profileData.collegeInfo" to collegeInfo)

            if (snapshot.exists()) {
                // Update only about field
                userRef.update(updateMap).await()
                emit(Response.Success(Unit))
            } else {
                // Create whole structure if doc doesn't exist
                val initialData = mapOf("profileData" to mapOf("collegeInfo" to collegeInfo))
                userRef.set(initialData).await()
                emit(Response.Success(Unit))
            }

        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Unexpected error occurred"))
        }
    }

    //update User Education
    fun updateUserExperience(experienceInfo: ExperienceInfo) = flow {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId.isNullOrBlank()) {
            emit(Response.Error("User not logged in"))
            return@flow
        }

        val userRef = userCollection.document(userId)

        try {
            emit(Response.Loading)

            val snapshot = userRef.get().await()

            val updateMap = mapOf("profileData.experienceInfo" to experienceInfo)

            if (snapshot.exists()) {
                // Update only about field
                userRef.update(updateMap).await()
                emit(Response.Success(Unit))
            } else {
                // Create whole structure if doc doesn't exist
                val initialData = mapOf("profileData" to mapOf("experienceInfo" to experienceInfo))
                userRef.set(initialData).await()
                emit(Response.Success(Unit))
            }

        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Unexpected error occurred"))
        }
    }

    fun getProfileData(userId: String?): Flow<Response<ProfileData>> = callbackFlow {
        trySend(Response.Loading)

        val id: String = if (userId == null && currentUser != null) currentUser else userId ?: ""

        val docRef = userCollection.document(id)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Response.Error(error.message ?: "Unknown Firestore error"))
                return@addSnapshotListener
            }

            val profileWrapper = snapshot?.toObject(ProfileDataWrapper::class.java)
            val profileHeader = profileWrapper?.profileData ?: ProfileData() // Return empty ProfileData if missing

            trySend(Response.Success(profileHeader))
        }

        awaitClose { listener.remove() }
    }

    fun getUserActivity(userId: String?): Flow<Response<List<ExtraActivity>>> = callbackFlow {
        trySend(Response.Loading)

        val userIdToUse = userId ?: currentUser ?: ""

        val collectionRef = userCollection
            .document(userIdToUse)
            .collection("extraActivity")

        val listener = collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Response.Error("Error listening to activity changes: ${e.message}"))
                return@addSnapshotListener
            }

            val activities = snapshot?.toObjects(ExtraActivity::class.java) ?: emptyList()
            trySend(Response.Success(activities))
        }

        // This must be the final statement in callbackFlow
        awaitClose { listener.remove() }
    }


    // Firestore model wrapper
    data class ProfileDataWrapper(
        val profileData: ProfileData? = null
    )


}