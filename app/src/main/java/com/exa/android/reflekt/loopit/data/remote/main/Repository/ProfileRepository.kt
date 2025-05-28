package com.exa.android.reflekt.loopit.data.remote.main.Repository

import com.exa.android.reflekt.loopit.data.remote.main.Repository.UserRepository.ProfileDataWrapper
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileData
import com.exa.android.reflekt.loopit.util.model.profileUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getUserProfile(userId: String): profileUser {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                val profileWrapper = document.toObject(ProfileDataWrapper::class.java)
                val profileData = profileWrapper?.profileData ?: ProfileData()

                val data = profileData.profileHeader
                profileUser(
                    uid = data.uid,
                    email = data.email,
                    name = data.name,
                    role = data.role,
                    imageUrl = data.profileImageUrl,
                    isStudent = data.isStudent,
                    createdAt = data.createdAt,
                    collegeName = data.collegeName,
                    year = data.year,
                    lat = data.lat,
                    lng = data.lng
                )
            } else {
                return profileUser()
                throw Exception("Profile not found")
            }
        } catch (e: Exception) {
            return profileUser()
            throw Exception("Failed to fetch profile: ${e.message}")
        }
    }
}