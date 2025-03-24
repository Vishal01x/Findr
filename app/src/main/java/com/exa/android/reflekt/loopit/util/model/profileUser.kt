package com.exa.android.reflekt.loopit.util.model

import java.security.Timestamp

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
)