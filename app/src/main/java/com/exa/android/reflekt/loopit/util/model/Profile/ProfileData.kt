package com.exa.android.reflekt.loopit.util.model.Profile

import com.google.firebase.Timestamp
import java.util.UUID



data class ProfileData(
    val profileHeader: ProfileHeaderData = ProfileHeaderData(),
    val about : About = About(),
    val skill : String = "",
    val collegeInfo: CollegeInfo = CollegeInfo(),
    val experienceInfo: ExperienceInfo = ExperienceInfo(),
    val activities : List<ExtraActivity> = emptyList()
)


data class ProfileHeaderData(
    val uid: String = "",
    val email: String = "",
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
    var name: String = "",
    var headline: String = "",
    var role: String = "",
    var profileImageUrl: String = "",
    var bannerImageUrl: String = "",
    val skills: List<String> = emptyList(),
    val socialLinks: SocialLinks = SocialLinks()
)

data class About(
    val description : String = ""
)

data class CollegeInfo(
    val instituteName: String = "",
    val stream: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val grade: String = ""
)

data class ExperienceInfo(
    val title: String = "",
    val employmentType: String = "",
    val companyName: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String? = null, // nullable if 'Currently Working Here' is true
    val currentlyWorking: Boolean = false,
    val description: String = ""
)

data class ExtraActivity(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val link : String = "",
    val description :String = "",
    val media : String = "",
    val domain : String = ""
)

data class SocialLinks(
    var linkedin: String? = "",
    var youtube: String? = "",
    var email: String? = "",
    var portfolio: String? = ""
)
