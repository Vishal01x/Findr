package com.exa.android.reflekt.loopit.data.feature.main.placement.data.model

import com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.*
import com.google.firebase.Timestamp

data class PlacementReviewDto(
    val id: String = "",
    val companyName: String = "",
    val companyLogo: String? = null,
    val packageType: String = "range", // "range" or "discrete"
    val packageMin: Double? = null,
    val packageMax: Double? = null,
    val packageList: List<Double>? = null,
    val visitDate: Timestamp = Timestamp.now(),
    val month: String = "",
    val year: Int = 0,
    val academicYear: String = "",
    val eligibilityCgpa: Double? = null,
    val eligibilityBranches: List<String> = emptyList(),
    val eligibilityMaxBacklogs: Int? = null,
    val eligibilityOther: String = "",
    val studentsShortlisted: Int = 0,
    val studentsSelected: Int = 0,
    val rounds: List<RoundDto> = emptyList(),
    val overallExperience: String = "",
    val tips: List<String> = emptyList(),
    val difficulty: String = "MEDIUM",
    val submittedBy: String = "",
    val submittedByName: String = "",
    val submittedAt: Timestamp = Timestamp.now(),
    val verificationStatus: String = "NOT_VERIFIED",
    val verifiedAt: Timestamp? = null,
    val upvotes: Int = 0
)

data class RoundDto(
    val roundNumber: Int = 0,
    val roundType: String = "",
    val difficulty: String = "MEDIUM",
    val specificTopics: List<String> = emptyList(),
    val commonTopics: List<String> = emptyList(),
    val keyPoints: List<String> = emptyList(),
    val description: String = ""
)

// Mappers
fun PlacementReviewDto.toDomain(): PlacementReview {
    val packageInfo = when (packageType) {
        "discrete" -> Package.Discrete(packageList ?: emptyList())
        else -> Package.Range(packageMin ?: 0.0, packageMax ?: 0.0)
    }

    return PlacementReview(
        id = id,
        companyName = companyName,
        companyLogo = companyLogo,
        packageInfo = packageInfo,
        visitDate = visitDate,
        month = month,
        year = year,
        academicYear = academicYear,
        eligibilityCriteria = EligibilityCriteria(
            cgpa = eligibilityCgpa,
            branches = eligibilityBranches,
            maxBacklogs = eligibilityMaxBacklogs,
            otherCriteria = eligibilityOther
        ),
        studentsShortlisted = studentsShortlisted,
        studentsSelected = studentsSelected,
        rounds = rounds.map { it.toDomain() },
        overallExperience = overallExperience,
        tips = tips,
        difficulty = Difficulty.fromString(difficulty),
        submittedBy = submittedBy,
        submittedByName = submittedByName,
        submittedAt = submittedAt,
        verificationStatus = VerificationStatus.fromString(verificationStatus),
        verifiedAt = verifiedAt,
        upvotes = upvotes
    )
}

fun PlacementReview.toDto(): PlacementReviewDto {
    val (packageType, packageMin, packageMax, packageList) = when (packageInfo) {
        is Package.Range -> listOf("range", packageInfo.min, packageInfo.max, null)
        is Package.Discrete -> listOf("discrete", null, null, packageInfo.packages)
    }

    return PlacementReviewDto(
        id = id,
        companyName = companyName,
        companyLogo = companyLogo,
        packageType = packageType as String,
        packageMin = packageMin as? Double,
        packageMax = packageMax as? Double,
        packageList = packageList as? List<Double>,
        visitDate = visitDate,
        month = month,
        year = year,
        academicYear = academicYear,
        eligibilityCgpa = eligibilityCriteria.cgpa,
        eligibilityBranches = eligibilityCriteria.branches,
        eligibilityMaxBacklogs = eligibilityCriteria.maxBacklogs,
        eligibilityOther = eligibilityCriteria.otherCriteria,
        studentsShortlisted = studentsShortlisted,
        studentsSelected = studentsSelected,
        rounds = rounds.map { it.toDto() },
        overallExperience = overallExperience,
        tips = tips,
        difficulty = difficulty.name,
        submittedBy = submittedBy,
        submittedByName = submittedByName,
        submittedAt = submittedAt,
        verificationStatus = verificationStatus.name,
        verifiedAt = verifiedAt,
        upvotes = upvotes
    )
}

fun RoundDto.toDomain(): Round {
    return Round(
        roundNumber = roundNumber,
        roundType = RoundType.fromString(roundType),
        difficulty = Difficulty.fromString(difficulty),
        specificTopics = specificTopics,
        commonTopics = commonTopics,
        keyPoints = keyPoints,
        description = description
    )
}

fun Round.toDto(): RoundDto {
    return RoundDto(
        roundNumber = roundNumber,
        roundType = roundType.name,
        difficulty = difficulty.name,
        specificTopics = specificTopics,
        commonTopics = commonTopics,
        keyPoints = keyPoints,
        description = description
    )
}
