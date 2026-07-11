package com.exa.android.reflekt.loopit.presentation.main.placement.model

// ─── New Enums ───────────────────────────────────────────────────────────────────────────────

enum class DifficultyLevel { EASY, MEDIUM, HARD }
enum class RoleType { INTERN, FULLTIME, BOTH }

data class ReferenceLink(
    val label: String = "",   // "Striver Sheet", "LeetCode Tag"
    val url: String = ""
)

data class PdfAttachment(
    val name: String = "",    // "Google Interview Guide.pdf"
    val storageUrl: String = "",
    val sizeKb: Long = 0
)

// ─── Domain Models ───────────────────────────────────────────────────────────────────────────

data class CompanyDetail(
    val id: String = "",
    val name: String = "",
    val logoUrl: String = "",
    val tagline: String = "",           // "Fintech Infrastructure • San Francisco, CA"
    val description: String = "",       // Company overview / about
    val website: String = "",           // Company website URL
    val location: String = "",          // "Bangalore, India" or "Remote"
    val employeeCount: String = "",     // "10,000+" or "500-1000"
    val isHiring: Boolean = false,
    val jobRole: String = "",           // "SDE Intern" / "Software Development Engineer I"
    val roleType: RoleType = RoleType.BOTH, // INTERN / FULLTIME / BOTH
    val packageLpa: String? = null,     // "32 LPA" (null if not applicable)
    val stipendPerMonth: String? = null,// "$9,500/mo"
    val difficulty: String? = null,     // "4.5 ★"
    val level: DifficultyLevel = DifficultyLevel.MEDIUM, // EASY / MEDIUM / HARD
    val preparationPercent: Int = 0,    // 65
    val skillsMatched: Int = 0,         // 3
    val totalSkills: Int = 0,           // 5
    val cgpaCutoff: String = "",        // "7.5+"
    val yourCgpa: String = "",          // "8.2"
    val activeBacklogs: Int = 0,
    val allowedBacklogs: Int = 0,
    val allowedBranches: List<String> = emptyList(),
    val minCgpa: String = "",
    val tenth: String = "",             // Min 10th % cutoff
    val twelfth: String = "",           // Min 12th % cutoff
    val hiresCount: Int = 0,            // Approx number of hires per season
    val workCulture: String = "",       // Life/work culture description
    val internDeadline: String = "",    // When intern hiring opens/closes
    val fulltimeDeadline: String = "",  // When FTE hiring opens/closes
    val deadline: String = "",          // "Oct 24, 11:59 PM"
    val recruitmentStages: List<RecruitmentStage> = emptyList(),
    val eligibilityCriteria: EligibilityCriteria = EligibilityCriteria(),
    val preparationResources: List<PrepResource> = emptyList(),
    val referenceLinks: List<ReferenceLink> = emptyList(), // named external links
    val pdfAttachments: List<PdfAttachment> = emptyList(), // Firebase Storage PDFs
    val placementContacts: List<PlacementContact> = emptyList(),
    val alumniInsights: List<AlumniInsight> = emptyList(),
    val recentFeedback: List<AlumniFeedback> = emptyList(),
    val isLiveProcess: Boolean = false,
    val createdBy: String = "",         // UID of admin who added
    val lastUpdated: Long = 0L,         // Epoch millis
    val isEligible: Boolean? = null     // null = unchecked, true/false after check
)

data class RecruitmentStage(
    val order: Int = 0,
    val title: String = "",
    val status: StageStatus = StageStatus.UPCOMING,
    val date: String = "",              // "Oct 12"
    val platform: String = "",
    val durationMins: Int = 0,
    val tags: List<String> = emptyList(),
    val description: String = ""
)

enum class StageStatus { CLEARED, CURRENT, UPCOMING }

data class EligibilityCriteria(
    val minCgpa: String = "",
    val noActiveBacklogs: Boolean = true,
    val allowedBranches: List<String> = emptyList()
)

data class PrepResource(
    val title: String = "",
    val subtitle: String = "",          // "Curated PDF • 2.4 MB" / "External Link"
    val icon: PrepResourceIcon = PrepResourceIcon.DOCUMENT,
    val url: String = ""
)

enum class PrepResourceIcon { DOCUMENT, LINK, CODE }

data class PlacementContact(
    val name: String = "",
    val role: String = "",              // "University Recruiter @ Stripe"
    val avatarUrl: String = "",
    val linkedinUrl: String = "",
    val emailUrl: String = ""
)

data class AlumniInsight(
    val name: String = "",
    val batchInfo: String = "",         // "SDE-1  '22 Batch"
    val avatarUrl: String = "",
    val quote: String = "",
    val rating: Float = 0f             // used in feedback
)

data class AlumniFeedback(
    val quote: String = "",
    val rating: Int = 0,               // 1-5
    val daysAgo: Int = 0,
    val batch: String = ""             // "2025 Batch Student"
)

// ─── Sample / Preview Data ────────────────────────────────────────────────────

object SampleCompanyData {
    val stripe = CompanyDetail(
        id = "stripe",
        name = "Stripe",
        logoUrl = "",
        tagline = "Fintech Infrastructure • San Francisco, CA",
        isHiring = true,
        jobRole = "SDE Intern",
        stipendPerMonth = "\$9,500/mo",
        preparationPercent = 65,
        skillsMatched = 3,
        totalSkills = 5,
        cgpaCutoff = "7.5+",
        yourCgpa = "8.2",
        activeBacklogs = 0,
        allowedBacklogs = 0,
        deadline = "Oct 24, 11:59 PM",
        isLiveProcess = true,
        recruitmentStages = listOf(
            RecruitmentStage(
                order = 1, title = "Resume Shortlisting",
                status = StageStatus.CLEARED, date = "Oct 12"
            ),
            RecruitmentStage(
                order = 2, title = "Online Assessment",
                status = StageStatus.CURRENT,
                platform = "HackerRank",
                durationMins = 90,
                tags = listOf("DSA"),
                description = "3 Coding questions (Easy-Medium) + 10 MCQs on OS/DBMS."
            ),
            RecruitmentStage(
                order = 3, title = "Technical Interview",
                status = StageStatus.UPCOMING,
                description = "Scheduled for shortlisted candidates"
            )
        ),
        eligibilityCriteria = EligibilityCriteria(
            minCgpa = "7.5+",
            noActiveBacklogs = true,
            allowedBranches = listOf("CSE", "IT", "ECE", "EEE")
        ),
        placementContacts = listOf(
            PlacementContact(
                name = "Sarah Jenkins",
                role = "University Recruiter @ Stripe",
                avatarUrl = ""
            )
        ),
        alumniInsights = listOf(
            AlumniInsight(
                name = "Alex M.",
                batchInfo = "SDE-1  '22 Batch",
                quote = "\"Focus heavily on Dynamic Programming for Round 1. The interviewers are super friendly but expect clean code.\""
            ),
            AlumniInsight(
                name = "Priya S.",
                batchInfo = "Frontend '23",
                quote = "\"System Design is crucial. Make sure you read up on luck!\""
            )
        )
    )

    val google = CompanyDetail(
        id = "google",
        name = "Google",
        logoUrl = "",
        tagline = "Software Development Engineer I",
        isHiring = true,
        jobRole = "SDE I",
        packageLpa = "32 LPA",
        difficulty = "4.5 ★",
        preparationPercent = 72,
        skillsMatched = 4,
        totalSkills = 5,
        minCgpa = "8.0 and above",
        activeBacklogs = 0,
        allowedBacklogs = 0,
        deadline = "Nov 10, 11:59 PM",
        isLiveProcess = false,
        recruitmentStages = listOf(
            RecruitmentStage(
                order = 1, title = "Online Coding Assessment",
                status = StageStatus.CURRENT,
                durationMins = 90,
                description = "Usually focuses on DP, Graphs, or Greedy algorithms. Hosted on HackerRank.",
                tags = listOf("2 Problems")
            ),
            RecruitmentStage(
                order = 2, title = "Technical Interview I",
                status = StageStatus.UPCOMING,
                durationMins = 45,
                description = "Deep dive into DSA concepts. Expect live coding on Google Docs or equivalent.",
                tags = listOf("Virtual")
            ),
            RecruitmentStage(
                order = 3, title = "Managerial / HR Round",
                status = StageStatus.UPCOMING,
                durationMins = 30,
                description = "Behavioral questions, project discussions, and 'Googleness' check."
            )
        ),
        eligibilityCriteria = EligibilityCriteria(
            minCgpa = "8.0",
            noActiveBacklogs = true,
            allowedBranches = listOf("CSE", "IT", "ECE", "EEE")
        ),
        preparationResources = listOf(
            PrepResource(
                title = "Top 50 Interview Qs",
                subtitle = "Curated PDF • 2.4 MB",
                icon = PrepResourceIcon.DOCUMENT
            ),
            PrepResource(
                title = "DSA Sheet (Striver)",
                subtitle = "External Link",
                icon = PrepResourceIcon.LINK,
                url = "https://takeuforward.org"
            ),
            PrepResource(
                title = "System Design Primer",
                subtitle = "GitHub Repo",
                icon = PrepResourceIcon.CODE,
                url = "https://github.com"
            )
        ),
        alumniInsights = listOf(
            AlumniInsight(
                name = "R. K.",
                batchInfo = "SDE-1 '23",
                avatarUrl = "",
                quote = ""
            )
        ),
        recentFeedback = listOf(
            AlumniFeedback(
                quote = "\"The coding round was tougher than expected. Make sure to revise Graph algorithms thoroughly. Interviewers were very supportive though.\"",
                rating = 4,
                daysAgo = 2,
                batch = "2025 Batch Student"
            )
        ),
        isEligible = null
    )
}
