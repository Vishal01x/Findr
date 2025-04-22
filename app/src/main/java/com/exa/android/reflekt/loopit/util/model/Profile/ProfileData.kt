package com.exa.android.reflekt.loopit.util.model.Profile

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
    val name: String = "",
    val headline: String = "",
    val role: String = "",
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
    val linkedin: String? = "",
    val youtube: String? = "",
    val email: String? = "",
    val portfolio: String? = ""
)
