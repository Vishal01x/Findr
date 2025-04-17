import java.security.Timestamp


//  package com.exa.android.reflekt.loopit.util.model

data class profileUser(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: String = "",
    val isStudent: Boolean = false,
    val createdAt: Timestamp? = null,
    val collegeName: String? = null,
    val year: Int? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0
) {
    // Custom getter for Firestore deserialization
    constructor() : this(
        uid = "",
        email = "",
        firstName = "",
        lastName = "",
        role = "",
        isStudent = false,
        createdAt = null,
        collegeName = null,
        year = null,
        lat = 0.0,
        lng = 0.0
    ) {
        // No need for manual parsing if Firestore stores it as Double
    }

    companion object {
        // Custom deserializer (if needed)
        fun fromFirestore(map: Map<String, Any>): profileUser {
            return profileUser(
                uid = map["uid"] as? String ?: "",
                email = map["email"] as? String ?: "",
                firstName = map["firstName"] as? String ?: "",
                lastName = map["lastName"] as? String ?: "",
                role = map["role"] as? String ?: "",
                isStudent = map["isStudent"] as? Boolean ?: false,
                createdAt = map["createdAt"] as? Timestamp,
                collegeName = map["collegeName"] as? String,
                year = map["year"] as? Int,
                lat = (map["lat"] as? String)?.toDoubleOrNull() ?: (map["lat"] as? Double ?: 0.0),
                lng = (map["lng"] as? String)?.toDoubleOrNull() ?: (map["lng"] as? Double ?: 0.0)
            )
        }
    }
}