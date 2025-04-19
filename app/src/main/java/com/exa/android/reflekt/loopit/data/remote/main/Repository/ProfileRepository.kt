package com.exa.android.reflekt.loopit.data.remote.main.Repository

import com.exa.android.reflekt.loopit.util.model.profileUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getUserProfile(userId: String): profileUser {
        return try {
            val document = firestore.collection("profile").document(userId).get().await()
            if (document.exists()) {
                profileUser.fromFirestore(document.data!!)
            } else {
                throw Exception("Profile not found")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch profile: ${e.message}")
        }
    }
}