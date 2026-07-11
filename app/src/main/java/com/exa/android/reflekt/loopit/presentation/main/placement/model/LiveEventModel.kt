package com.exa.android.reflekt.loopit.presentation.main.placement.model

import com.google.firebase.firestore.DocumentId

data class LiveEventModel(
    @DocumentId val id: String = "",
    val companyId: String = "",
    val companyName: String = "",
    val logoUrl: String = "",
    val packageAmount: String = "",
    val role: String = "",
    val location: String = "",
    val time: String = "",
    val description: String = "",
    val linkedPostId: String = ""
)
