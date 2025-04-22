package com.exa.android.reflekt.loopit.util.model

import com.google.firebase.Timestamp


data class profileUser(
    val uid: String = "",
    val email: String = "",
//    val firstName: String = "",
//    val lastName: String = "",
    val name : String = "",
    val role: String = "",
    val imageUrl : String = "",
    val isStudent: Boolean = false,
    val createdAt: Timestamp? = null,
    val collegeName: String? = null,
    val year: String? = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val location: String = "",
    val companyName: String = "",
    val ctc: String = "",
    val experience: String = ""
) {
    // Custom getter for Firestore deserialization
    constructor() : this(
        uid = "",
        email = "",
        name = "",
        role = "",
        imageUrl = "",
        isStudent = false,
        createdAt = null,
        collegeName = null,
        year = "",
        lat = 0.0,
        lng = 0.0,
        location = "",
        companyName = "",
        ctc = "",
        experience = ""
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
                experience = map["experience"] as? String ?: ""
            )
        }
    }
}