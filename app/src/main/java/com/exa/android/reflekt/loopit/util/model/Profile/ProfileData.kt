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
    val grade: String = "",
    val year: String  = ""
)

data class ExperienceInfo(
    val title: String = "",
    val employmentType: String = "",
    val companyName: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String? = null, // nullable if 'Currently Working Here' is true
    val currentlyWorking: Boolean = false,
    val description: String = "",
    val experience : String = "",
    val ctc: String = ""
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


//data class ProfileData(
//    val profileHeader: ProfileHeaderData = ProfileHeaderData(),
//    val about: About = About(),
//    val skill: String = "Add skills like Kotlin, UI/UX, Python...",
//    val collegeInfo: CollegeInfo = CollegeInfo(),
//    val experienceInfo: ExperienceInfo = ExperienceInfo(),
//    val activities: List<ExtraActivity> = listOf(
//        ExtraActivity(
//            name = "Mention extracurricular activities",
//            link = "Add relevant links (optional)",
//            description = "Describe what you did, your impact, or key learnings",
//            media = "Upload an image or video (optional)",
//            domain = "e.g. Hackathons, Competitions, Community Work"
//        )
//    )
//)
//
//data class ProfileHeaderData(
//    val uid: String = "",
//    val email: String = "your.email@example.com",
//    val isStudent: Boolean = true,
//    val createdAt: Timestamp? = null,
//    val collegeName: String? = "Your College/University Name",
//    val year: String? = "e.g. Final Year",
//    val lat: Double = 0.0,
//    val lng: Double = 0.0,
//    val location: String = "City, Country",
//    val companyName: String = "Current Company (if any)",
//    val ctc: String = "e.g. 10 LPA",
//    val experience: String = "e.g. 2 years",
//    var name: String = "Your Full Name",
//    var headline: String = "Write a catchy headline about your role/goal",
//    var role: String = "e.g. Android Developer, Product Designer",
//    var profileImageUrl: String = "", // You can use a default placeholder image here
//    var bannerImageUrl: String = "",  // Same here
//    val skills: List<String> = listOf("Add a few skills you’re confident in"),
//    val socialLinks: SocialLinks = SocialLinks()
//)
//
//data class About(
//    val description: String = "Describe who you are, what you do, and what you're passionate about"
//)
//
//data class CollegeInfo(
//    val instituteName: String = "Enter college/institution name",
//    val stream: String = "Enter your stream/department",
//    val startDate: String = "Start Date (e.g. Aug 2020)",
//    val endDate: String = "End Date (e.g. May 2024)",
//    val grade: String = "e.g. 8.2 CGPA",
//    val year: String = "e.g. 3rd Year"
//)
//
//data class ExperienceInfo(
//    val title: String = "Job Title (e.g. Software Intern)",
//    val employmentType: String = "e.g. Full-time, Internship",
//    val companyName: String = "Enter company/organization name",
//    val location: String = "e.g. Remote, Bangalore",
//    val startDate: String = "Start Date",
//    val endDate: String? = "End Date or leave blank if still working",
//    val currentlyWorking: Boolean = false,
//    val description: String = "Briefly describe your work, achievements, or responsibilities",
//    val experience: String = "e.g. 6 months",
//    val ctc: String = "Compensation (if applicable)"
//)
//
//data class ExtraActivity(
//    val id: String = UUID.randomUUID().toString(),
//    val name: String = "Activity Name (e.g. Technical Fest Volunteer)",
//    val link: String = "Provide link to details (if any)",
//    val description: String = "What did you do, organize, contribute, or learn?",
//    val media: String = "",
//    val domain: String = "e.g. Event Management, Public Speaking"
//)
//
//data class SocialLinks(
//    var linkedin: String? = "https://linkedin.com/in/yourprofile",
//    var youtube: String? = "https://youtube.com/yourchannel",
//    var email: String? = "your.contact@email.com",
//    var portfolio: String? = "https://yourwebsite.dev"
//)
