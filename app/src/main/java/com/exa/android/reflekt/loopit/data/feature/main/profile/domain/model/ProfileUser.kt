package com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model

import androidx.compose.runtime.saveable.Saver
import com.google.firebase.Timestamp
// this is the data class that we are using in map so keep it else make changes in code

data class profileUser(
    val uid: String = "",
    val email: String = "",
//    val firstName: String = "",
//    val lastName: String = "",
    val name : String = "",
    val role: String = "",
    val headline: String? = null,
    val profilePicture : String = "",
    val isStudent: Boolean = false,
    val createdAt: Timestamp? = null,
    val collegeName: String? = null,
    val year: String? = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val location: String = "",
    val companyName: String = "",
    val ctc: String = "",
    val experience: String = "",
    val rating: Float = 0f,
    // Enriched from profileData.collegeInfo when read for post display
    val branch: String? = null,
    val section: String? = null,
    val passoutYear: String? = null  // Last 2 digits of endDate, e.g. "26" from "May 2026"
) {
    // Custom getter for Firestore deserialization
    constructor() : this(
        uid = "",
        email = "",
        name = "",
        role = "",
        headline = null,
        profilePicture = "",
        isStudent = false,
        createdAt = null,
        collegeName = null,
        year = "",
        lat = 0.0,
        lng = 0.0,
        location = "",
        companyName = "",
        ctc = "",
        experience = "",
        rating = 0f
    ) {
        // No need for manual parsing if Firestore stores it as Double
    }

    companion object {
        // Custom deserializer (if needed)
        fun fromFirestore(map: Map<String, Any>): profileUser {
            return profileUser(
                uid = map["uid"] as? String ?: "",
                email = map["email"] as? String ?: "",
                name = map["name"] as? String ?: "",
                role = map["role"] as? String ?: "",
                isStudent = map["isStudent"] as? Boolean ?: false,
                createdAt = map["createdAt"] as? Timestamp,
                collegeName = map["collegeName"] as? String,
                year = map["year"] as? String,
                lat = (map["lat"] as? String)?.toDoubleOrNull() ?: (map["lat"] as? Double ?: 0.0),
                lng = (map["lng"] as? String)?.toDoubleOrNull() ?: (map["lng"] as? Double ?: 0.0),
                location = map["location"] as? String ?: "",
                companyName = map["companyName"] as? String ?: "",
                ctc = map["ctc"] as? String ?: "",
                experience = map["experience"] as? String ?: "",
                rating = (map["rating"] as? Number)?.toFloat() ?: 0f,
                headline = map["headline"] as? String
            )
        }
        val Saver: Saver<profileUser?, *> = Saver(
            save = { user ->
                user?.let {
                    listOf(
                        it.uid,
                        it.email,
                        it.name,
                        it.role,
                        it.headline,
                        it.profilePicture,
                        it.isStudent,
                        it.collegeName,
                        it.year,
                        it.lat,
                        it.lng,
                        it.location,
                        it.companyName,
                        it.ctc,
                        it.experience,
                        it.rating
                    )
                }
            },
            restore = { data ->
                if (data is List<*> && data.size >= 16) {
                    profileUser(
                        uid = data[0] as String,
                        email = data[1] as String,
                        name = data[2] as String,
                        role = data[3] as String,
                        headline = data[4] as String,
                        profilePicture = data[5] as String,
                        isStudent = data[6] as Boolean,
                        collegeName = data[7] as? String,
                        year = data[8] as String,
                        lat = data[9] as Double,
                        lng = data[10] as Double,
                        location = data[11] as String,
                        companyName = data[12] as String,
                        ctc = data[13] as String,
                        experience = data[14] as String,
                        rating = (data[15] as Number).toFloat()
                    )
                } else null
            }
        )

    }

}
