package com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model

import com.google.firebase.Timestamp

data class PlacementReview(
    val id: String = "",
    val companyName: String,
    val companyLogo: String? = null,
    val packageInfo: Package,
    val visitDate: Timestamp,
    val month: String,
    val year: Int,
    val academicYear: String,
    val eligibilityCriteria: EligibilityCriteria,
    val studentsShortlisted: Int,
    val studentsSelected: Int,
    val rounds: List<Round>,
    val overallExperience: String,
    val tips: List<String> = emptyList(),
    val difficulty: Difficulty,
    val submittedBy: String,
    val submittedByName: String,
    val submittedAt: Timestamp,
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_VERIFIED,
    val verifiedAt: Timestamp? = null,
    val upvotes: Int = 0
)
