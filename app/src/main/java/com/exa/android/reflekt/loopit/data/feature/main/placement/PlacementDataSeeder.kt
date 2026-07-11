package com.exa.android.reflekt.loopit.data.feature.main.placement

import com.exa.android.reflekt.loopit.presentation.main.placement.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-shot bulk seeder.  Call [seedAll] from the admin Placement Stats screen.
 *
 * All writes go through the SAME repository methods used by the UI forms:
 *  - [PlacementRepository.addCompany]      → identical Firestore map / path
 *  - [BatchFeedbackRepository.addBatchReport] → identical sub-collection write
 *  - [BatchFeedbackRepository.seedFeedbackDirect] → preserves APPROVED / PENDING status
 *
 * This guarantees the Firestore document structure is 100 % consistent with
 * data entered manually through the AddCompanyFormScreen.
 *
 * Idempotency note: [PlacementRepository.addCompany] always generates a NEW
 * Firestore document ID, so running seedAll() twice creates duplicate companies.
 * Guard the button with a confirmation dialog or check if data already exists.
 */
@Singleton
class PlacementDataSeeder @Inject constructor(
    private val placementRepo: PlacementRepository,
    private val feedbackRepo: BatchFeedbackRepository
) {
    // ─────────────────────────────────────────────────────────────────────────
    //  Public entry point
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun seedAll(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()

            companySeedList(now).forEach { company ->
                // 1. Write via PlacementRepository → same Firestore path & serialization as the form
                val result = placementRepo.addCompany(company)
                if (result.isFailure) return@withContext Result.failure(
                    result.exceptionOrNull() ?: Exception("Failed to add ${company.name}")
                )
                val companyId = result.getOrThrow()   // auto-generated Firestore doc ID

                // 2. Batch reports → same method as the admin "Add Batch" panel
                batchReportSeedList(companyId, company.name, now).forEach { report ->
                    feedbackRepo.addBatchReport(companyId, report)
                }

                // 3. Feedback → seedFeedbackDirect preserves APPROVED / PENDING status
                feedbackSeedList(companyId, company.name, now).forEach { fb ->
                    feedbackRepo.seedFeedbackDirect(companyId, fb)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Company definitions
    //  (id field left blank — PlacementRepository.addCompany() assigns the real id)
    // ─────────────────────────────────────────────────────────────────────────

    private fun companySeedList(now: Long): List<CompanyDetail> = listOf(

        // ── EPAM ────────────────────────────────────────────────────────────
        CompanyDetail(
            name           = "EPAM Systems",
            tagline        = "Engineering / IT Services • Global",
            description    = "EPAM is a global provider of digital platform engineering and development services. " +
                    "At LNCT they typically conduct two drives per academic year — July (Indore campus, ~40 hires) " +
                    "and February (~30 hires). Offers include a 6-month internship at ₹24k/month followed by a " +
                    "full-time role at 8.5 LPA.",
            website        = "https://www.epam.com",
            location       = "Indore, India",
            employeeCount  = "50,000+",
            isHiring       = true,
            jobRole        = "Software Engineer / SDE Intern",
            roleType       = RoleType.BOTH,
            packageLpa     = "8.5 LPA",
            stipendPerMonth = "₹24,000/mo",
            level          = DifficultyLevel.MEDIUM,
            hiresCount     = 70,
            minCgpa        = "6.5",
            tenth          = "60",
            twelfth        = "60",
            allowedBacklogs = 0,
            allowedBranches = listOf("CSE", "IT", "EC", "EE", "Mech"),
            eligibilityCriteria = EligibilityCriteria(
                minCgpa          = "6.5",
                noActiveBacklogs = true,
                allowedBranches  = listOf("CSE", "IT", "EC", "EE", "Mech")
            ),
            recruitmentStages = listOf(
                RecruitmentStage(
                    order = 1, title = "Online Aptitude Test",
                    status = StageStatus.UPCOMING, date = "July 2025",
                    platform = "EPAM Portal", durationMins = 60,
                    tags = listOf("Aptitude", "Reasoning", "English"),
                    description = "Quantitative aptitude, logical reasoning, and English communication. Moderate difficulty."
                ),
                RecruitmentStage(
                    order = 2, title = "Technical Assessment",
                    status = StageStatus.UPCOMING, durationMins = 90,
                    tags = listOf("DSA", "OOP", "Coding"),
                    description = "2 DSA coding questions (Easy-Medium). Focus on arrays, strings, and basic DP."
                ),
                RecruitmentStage(
                    order = 3, title = "Technical Interview",
                    status = StageStatus.UPCOMING, tags = listOf("Virtual"),
                    description = "Technical interview covering CS fundamentals, project discussion, and basic DSA."
                ),
                RecruitmentStage(
                    order = 4, title = "HR Interview",
                    status = StageStatus.UPCOMING,
                    description = "Behavioral & cultural fit round."
                )
            ),
            preparationResources = listOf(
                PrepResource(
                    title = "Striver A2Z DSA Sheet", subtitle = "External Link",
                    icon = PrepResourceIcon.LINK,
                    url = "https://takeuforward.org/strivers-a2z-dsa-course/strivers-a2z-dsa-course-sheet-2"
                )
            ),
            referenceLinks = listOf(ReferenceLink(label = "EPAM Careers", url = "https://careers.epam.com")),
            workCulture    = "Strong focus on learning & development. Flat hierarchy. Work from office at Indore.",
            internDeadline = "July 2025 (Drive 1), February 2026 (Drive 2)",
            fulltimeDeadline = "Post internship conversion",
            isLiveProcess  = false,
            lastUpdated    = now
        ),

        // ── Quantiphi ───────────────────────────────────────────────────────
        CompanyDetail(
            name           = "Quantiphi",
            tagline        = "AI-First Company • Mumbai / Remote",
            description    = "Quantiphi is an award-winning applied AI & data science company. " +
                    "Campus drive at LNCT happens late July (around 30 August) offering 8.5 LPA for full-time " +
                    "and ₹25k/month for a 6-month internship. Very selective — usually hires under 10 students.",
            website        = "https://quantiphi.com",
            location       = "Mumbai / Hybrid",
            employeeCount  = "3,000+",
            isHiring       = true,
            jobRole        = "ML Engineer / Data Engineer",
            roleType       = RoleType.BOTH,
            packageLpa     = "8.5 LPA",
            stipendPerMonth = "₹25,000/mo",
            level          = DifficultyLevel.HARD,
            hiresCount     = 8,
            minCgpa        = "7.0",
            tenth          = "65",
            twelfth        = "65",
            allowedBacklogs = 0,
            allowedBranches = listOf("CSE", "IT", "Data Science"),
            eligibilityCriteria = EligibilityCriteria(
                minCgpa = "7.0", noActiveBacklogs = true,
                allowedBranches = listOf("CSE", "IT")
            ),
            recruitmentStages = listOf(
                RecruitmentStage(
                    order = 1, title = "Aptitude + Analytical Test",
                    status = StageStatus.UPCOMING, date = "~30 Aug 2025", durationMins = 60,
                    tags = listOf("Aptitude", "Analytical"),
                    description = "Aptitude, logical reasoning, and analytical questions. Moderate difficulty."
                ),
                RecruitmentStage(
                    order = 2, title = "Technical Interview",
                    status = StageStatus.UPCOMING, tags = listOf("ML", "DSA", "Python"),
                    description = "ML fundamentals, Python coding, SQL, and statistics. Projects will be discussed."
                ),
                RecruitmentStage(
                    order = 3, title = "HR Interview",
                    status = StageStatus.UPCOMING,
                    description = "Cultural fit and career goals discussion."
                )
            ),
            preparationResources = listOf(
                PrepResource(
                    title = "ML Crash Course (Google)", subtitle = "External Link",
                    icon = PrepResourceIcon.LINK,
                    url = "https://developers.google.com/machine-learning/crash-course"
                )
            ),
            workCulture    = "Fast-paced AI-focused environment. Exposure to cutting-edge ML projects.",
            internDeadline = "Late July / August 2025",
            fulltimeDeadline = "Post-internship or direct hire",
            isLiveProcess  = false,
            lastUpdated    = now
        ),

        // ── Infosys HackWithInfy (SP / DS) ──────────────────────────────────
        CompanyDetail(
            name           = "Infosys (SP / HackWithInfy)",
            tagline        = "IT Services • Pan India | 3.6 / 6.25 / 9 LPA",
            description    = "Infosys conducts HackWithInfy, a competitive coding program around July 12. " +
                    "Roles: System Engineer (3.6 LPA), Specialist Programmer (6.25 LPA), Digital Specialist (9 LPA). " +
                    "Online assessment is HARD — DSA heavy (CP-style). Written test is pan-India. " +
                    "Students who qualify get a campus interview at Indore (Easy-Medium, Striver Sheet level). " +
                    "Final tech interview is tech-based. ~15 students hired from LNCT. " +
                    "Interns offered a 6-month paid internship at Mussoorie at ₹30k/month.",
            website        = "https://www.infosys.com/careers/hack-with-infy.html",
            location       = "Pan India (Assessment) → Indore (Interview)",
            employeeCount  = "300,000+",
            isHiring       = true,
            jobRole        = "System Engineer / Specialist Programmer / Digital Specialist",
            roleType       = RoleType.BOTH,
            packageLpa     = "3.6 / 6.25 / 9 LPA",
            stipendPerMonth = "₹30,000/mo (Mussoorie)",
            level          = DifficultyLevel.HARD,
            hiresCount     = 15,
            minCgpa        = "6.0",
            tenth          = "60",
            twelfth        = "60",
            allowedBacklogs = 0,
            allowedBranches = listOf("CSE", "IT", "EC", "EE", "Mech", "Civil"),
            eligibilityCriteria = EligibilityCriteria(
                minCgpa = "6.0", noActiveBacklogs = true,
                allowedBranches = listOf("CSE", "IT", "EC", "EE", "Mech", "Civil")
            ),
            recruitmentStages = listOf(
                RecruitmentStage(
                    order = 1, title = "HackWithInfy Online Assessment",
                    status = StageStatus.UPCOMING, date = "~12 July 2025",
                    platform = "HackerRank / Infosys Portal", durationMins = 180,
                    tags = listOf("CP", "DSA", "Hard"),
                    description = "Competitive programming style — very hard. Advanced DSA: Segment Trees, DP, Graph algorithms. Pan-India."
                ),
                RecruitmentStage(
                    order = 2, title = "Campus Interview – Written Test",
                    status = StageStatus.UPCOMING, platform = "Indore Campus", durationMins = 60,
                    tags = listOf("Easy-Medium", "Striver Sheet"),
                    description = "For shortlisted students. Assessment is Easy to Medium (Striver Sheet level)."
                ),
                RecruitmentStage(
                    order = 3, title = "Technical Interview",
                    status = StageStatus.UPCOMING, tags = listOf("Tech Round"),
                    description = "Technology-based interview. Projects, core CS, and coding questions."
                ),
                RecruitmentStage(
                    order = 4, title = "HR Interview",
                    status = StageStatus.UPCOMING,
                    description = "Final HR round for offer."
                )
            ),
            preparationResources = listOf(
                PrepResource(
                    title = "Striver's A2Z DSA Sheet", subtitle = "Must-do for campus interview",
                    icon = PrepResourceIcon.LINK,
                    url = "https://takeuforward.org/strivers-a2z-dsa-course/strivers-a2z-dsa-course-sheet-2"
                ),
                PrepResource(
                    title = "Codeforces — CP Practice", subtitle = "For HackWithInfy OA",
                    icon = PrepResourceIcon.LINK, url = "https://codeforces.com"
                )
            ),
            workCulture    = "Large MNC. Good learning programs. Mussoorie internship is a unique experience.",
            internDeadline = "~July 2025",
            fulltimeDeadline = "Post-internship / direct campus hire",
            isLiveProcess  = false,
            lastUpdated    = now
        ),

        // ── Infosys November Drive (Senior Roles) ───────────────────────────
        CompanyDetail(
            name           = "Infosys (Nov Drive – Senior Roles)",
            tagline        = "IT Services • Campus | 11 / 16 / 21 LPA",
            description    = "Infosys returns in late November for senior engineer packages: 11 LPA, 16 LPA, 21 LPA. " +
                    "Assessment is Medium–Hard (Striver Sheet based). Direct on-campus interview. " +
                    "Very few students selected (under 5) with 11 LPA as base offer.",
            website        = "https://www.infosys.com/careers/",
            location       = "Indore, India",
            employeeCount  = "300,000+",
            isHiring       = false,
            jobRole        = "Senior System Engineer / Tech Lead Associate",
            roleType       = RoleType.FULLTIME,
            packageLpa     = "11 / 16 / 21 LPA",
            level          = DifficultyLevel.HARD,
            hiresCount     = 5,
            minCgpa        = "7.0",
            tenth          = "60",
            twelfth        = "60",
            allowedBacklogs = 0,
            allowedBranches = listOf("CSE", "IT", "EC"),
            eligibilityCriteria = EligibilityCriteria(
                minCgpa = "7.0", noActiveBacklogs = true,
                allowedBranches = listOf("CSE", "IT", "EC")
            ),
            recruitmentStages = listOf(
                RecruitmentStage(
                    order = 1, title = "Online Assessment",
                    status = StageStatus.UPCOMING, date = "~November 2025", durationMins = 90,
                    tags = listOf("Medium-Hard", "DSA", "Striver"),
                    description = "Medium to hard DSA: Trees, Graphs, DP, Backtracking. Based on Striver Sheet."
                ),
                RecruitmentStage(
                    order = 2, title = "On-Campus Technical Interview",
                    status = StageStatus.UPCOMING, tags = listOf("On-Campus", "Direct Interview"),
                    description = "Direct technical interview on campus. CS fundamentals, DSA problem solving, project discussion."
                )
            ),
            preparationResources = listOf(
                PrepResource(
                    title = "Striver's A2Z DSA Sheet", subtitle = "Core preparation resource",
                    icon = PrepResourceIcon.LINK,
                    url = "https://takeuforward.org/strivers-a2z-dsa-course/strivers-a2z-dsa-course-sheet-2"
                )
            ),
            workCulture    = "Fast-track growth in Infosys for top performers.",
            internDeadline = "N/A",
            fulltimeDeadline = "November 2025",
            isLiveProcess  = false,
            lastUpdated    = now
        ),

        // ── Cognizant ────────────────────────────────────────────────────────
        CompanyDetail(
            name           = "Cognizant",
            tagline        = "IT Services • Pan India | GenC / GenC Next / GenC Pro",
            description    = "Cognizant conducts two waves at LNCT:\n" +
                    "1. Digital Nurture Drive (May–Sep): GenC (4 LPA) and GenC Pro (6.75 LPA) — pipeline round.\n" +
                    "2. Actual Campus Drive (October): GenC (4 LPA), GenC Next (5.5 LPA), GenC Pro (6.75 LPA). ~200 students selected.\n" +
                    "Tech round: choose Java/Python/C#; round 2 has 2 easy DSA questions + stream MCQs. Final: interview.",
            website        = "https://www.cognizant.com/us/en/careers/campus",
            location       = "Pan India",
            employeeCount  = "300,000+",
            isHiring       = true,
            jobRole        = "GenC / GenC Next / GenC Pro Programmer Analyst",
            roleType       = RoleType.FULLTIME,
            packageLpa     = "4 / 5.5 / 6.75 LPA",
            level          = DifficultyLevel.EASY,
            hiresCount     = 200,
            minCgpa        = "6.0",
            tenth          = "60",
            twelfth        = "60",
            allowedBacklogs = 0,
            allowedBranches = listOf("CSE", "IT", "EC", "EE", "Mech", "Civil"),
            eligibilityCriteria = EligibilityCriteria(
                minCgpa = "6.0", noActiveBacklogs = true,
                allowedBranches = listOf("CSE", "IT", "EC", "EE", "Mech", "Civil")
            ),
            recruitmentStages = listOf(
                RecruitmentStage(
                    order = 1, title = "Aptitude Test",
                    status = StageStatus.UPCOMING, date = "Oct 2025",
                    platform = "AMCAT / Cognizant Portal", durationMins = 90,
                    tags = listOf("Aptitude", "Reasoning", "English"),
                    description = "Quantitative aptitude, logical reasoning, verbal ability. Standard MNC-level."
                ),
                RecruitmentStage(
                    order = 2, title = "Technical Assessment (Stream-based)",
                    status = StageStatus.UPCOMING, durationMins = 60,
                    tags = listOf("Java", "Python", "C#", "DSA", "Easy"),
                    description = "Choose stream: Java / Python / C#. 2 easy DSA questions + MCQs on chosen language."
                ),
                RecruitmentStage(
                    order = 3, title = "Technical + HR Interview",
                    status = StageStatus.UPCOMING,
                    description = "Final interview covering chosen tech stack, projects, and behavioral questions."
                )
            ),
            preparationResources = listOf(
                PrepResource(
                    title = "IndiaBix Aptitude", subtitle = "Aptitude Practice",
                    icon = PrepResourceIcon.LINK, url = "https://www.indiabix.com"
                )
            ),
            workCulture    = "Large MNC. Good training programs. GenC Pro has faster growth.",
            internDeadline = "N/A (direct full-time)",
            fulltimeDeadline = "October 2025",
            isLiveProcess  = false,
            lastUpdated    = now
        ),

        // ── Capgemini ────────────────────────────────────────────────────────
        CompanyDetail(
            name           = "Capgemini",
            tagline        = "IT Services & Consulting • Pan India | 4.25 – 7.5 LPA",
            description    = "Capgemini is one of the largest campus recruiters at LNCT. Packages: " +
                    "4.25 LPA (Analyst), 5.75 LPA (Senior Analyst), 7.5 LPA (Specialist). ~250 students selected.",
            website        = "https://www.capgemini.com/careers/",
            location       = "Pan India",
            employeeCount  = "350,000+",
            isHiring       = true,
            jobRole        = "Analyst / Senior Analyst / Specialist",
            roleType       = RoleType.FULLTIME,
            packageLpa     = "4.25 / 5.75 / 7.5 LPA",
            level          = DifficultyLevel.EASY,
            hiresCount     = 250,
            minCgpa        = "6.0",
            tenth          = "60",
            twelfth        = "60",
            allowedBacklogs = 0,
            allowedBranches = listOf("CSE", "IT", "EC", "EE", "Mech", "Civil"),
            eligibilityCriteria = EligibilityCriteria(
                minCgpa = "6.0", noActiveBacklogs = true,
                allowedBranches = listOf("CSE", "IT", "EC", "EE", "Mech", "Civil")
            ),
            recruitmentStages = listOf(
                RecruitmentStage(
                    order = 1, title = "Aptitude Test (AMCAT-style)",
                    status = StageStatus.UPCOMING,
                    platform = "Capgemini Portal / AMCAT", durationMins = 90,
                    tags = listOf("Aptitude", "Quant", "Reasoning"),
                    description = "Standard aptitude: quantitative, logical reasoning, verbal. Easy to moderate."
                ),
                RecruitmentStage(
                    order = 2, title = "Technical Interview",
                    status = StageStatus.UPCOMING, tags = listOf("Tech", "Projects", "CS Fundamentals"),
                    description = "Core CS: OOPS, DBMS, OS, Networks. Project discussion. Basic coding may be asked."
                ),
                RecruitmentStage(
                    order = 3, title = "HR Interview",
                    status = StageStatus.UPCOMING,
                    description = "Behavioral questions, salary negotiation, and joining preference."
                )
            ),
            preparationResources = listOf(
                PrepResource(
                    title = "PrepInsta Capgemini Mock", subtitle = "Free Mock Tests",
                    icon = PrepResourceIcon.LINK, url = "https://prepinsta.com/capgemini/"
                )
            ),
            workCulture    = "Large MNC. Good work-life balance. Strong L&D platform.",
            internDeadline = "N/A",
            fulltimeDeadline = "2025–2026",
            isLiveProcess  = false,
            lastUpdated    = now
        ),

        // ── TCS ──────────────────────────────────────────────────────────────
        CompanyDetail(
            name           = "TCS",
            tagline        = "IT Services • Pan India | Ninja / Digital / Prime",
            description    = "TCS conducts two drives at LNCT:\n" +
                    "1. October: Only LNCT Main campus students allowed. Ninja (3.36 LPA), Digital (7 LPA), Prime (9+ LPA).\n" +
                    "2. Jan–Feb: Remaining students allowed. ~250 students hired total.\n" +
                    "Assessment through TCS NQT (National Qualifier Test) — pan-India.",
            website        = "https://ievolve.tcs.com/nqt",
            location       = "Pan India",
            employeeCount  = "600,000+",
            isHiring       = true,
            jobRole        = "Ninja / Digital / Prime Engineer",
            roleType       = RoleType.FULLTIME,
            packageLpa     = "3.36 / 7 / 9+ LPA",
            level          = DifficultyLevel.MEDIUM,
            hiresCount     = 250,
            minCgpa        = "6.0",
            tenth          = "60",
            twelfth        = "60",
            allowedBacklogs = 0,
            allowedBranches = listOf("CSE", "IT", "EC", "EE", "Mech", "Civil"),
            eligibilityCriteria = EligibilityCriteria(
                minCgpa = "6.0", noActiveBacklogs = true,
                allowedBranches = listOf("CSE", "IT", "EC", "EE", "Mech", "Civil")
            ),
            recruitmentStages = listOf(
                RecruitmentStage(
                    order = 1, title = "TCS NQT (National Qualifier Test)",
                    status = StageStatus.UPCOMING, date = "October 2025",
                    platform = "TCS iEvolve / NQT Portal", durationMins = 180,
                    tags = listOf("Aptitude", "Coding", "Verbal", "NQT"),
                    description = "Pan-India: Numerical, Verbal, Reasoning, Programming Logic (Ninja), Advanced Coding (Digital/Prime). " +
                            "Drive 1 (Oct): LNCT Main only. Drive 2 (Jan–Feb): All remaining."
                ),
                RecruitmentStage(
                    order = 2, title = "Technical Interview",
                    status = StageStatus.UPCOMING,
                    description = "CS fundamentals: OOP, DBMS, OS, and coding problem discussion."
                ),
                RecruitmentStage(
                    order = 3, title = "Managerial / HR Interview",
                    status = StageStatus.UPCOMING,
                    description = "Behavioral round. Team fit, project discussion, joining preferences."
                )
            ),
            preparationResources = listOf(
                PrepResource(
                    title = "TCS NQT Previous Papers", subtitle = "PrepInsta Collection",
                    icon = PrepResourceIcon.LINK, url = "https://prepinsta.com/tcs-nqt/"
                )
            ),
            workCulture    = "Massive MNC. Stable job. BPS/Digital roles offer better growth.",
            internDeadline = "N/A",
            fulltimeDeadline = "Drive 1: Oct 2025 | Drive 2: Jan–Feb 2026",
            isLiveProcess  = false,
            lastUpdated    = now
        ),

        // ── CarWale ──────────────────────────────────────────────────────────
        CompanyDetail(
            name           = "CarWale / CarTrade",
            tagline        = "Automotive Tech • Mumbai | SDE Roles",
            description    = "CarWale (part of CarTrade Tech) recruits SDE roles from LNCT campus. " +
                    "Known for a tech-focused interview process. Mid-size product company with competitive packages.",
            website        = "https://www.carwale.com/careers/",
            location       = "Mumbai / Pune, India",
            employeeCount  = "1,000–5,000",
            isHiring       = false,
            jobRole        = "Software Development Engineer",
            roleType       = RoleType.FULLTIME,
            packageLpa     = "6–10 LPA (est.)",
            level          = DifficultyLevel.MEDIUM,
            hiresCount     = 10,
            minCgpa        = "6.5",
            tenth          = "60",
            twelfth        = "60",
            allowedBacklogs = 0,
            allowedBranches = listOf("CSE", "IT"),
            eligibilityCriteria = EligibilityCriteria(
                minCgpa = "6.5", noActiveBacklogs = true,
                allowedBranches = listOf("CSE", "IT")
            ),
            recruitmentStages = listOf(
                RecruitmentStage(
                    order = 1, title = "Aptitude Test",
                    status = StageStatus.UPCOMING, durationMins = 60,
                    tags = listOf("Aptitude", "Reasoning"),
                    description = "Standard aptitude screening."
                ),
                RecruitmentStage(
                    order = 2, title = "Technical Assessment",
                    status = StageStatus.UPCOMING, durationMins = 90,
                    tags = listOf("DSA", "Coding", "Medium"),
                    description = "2–3 DSA coding questions (Easy-Medium). OOP and system design basics."
                ),
                RecruitmentStage(
                    order = 3, title = "Technical Interview",
                    status = StageStatus.UPCOMING,
                    description = "Deep dive into DSA, OOP concepts, backend tech, and project discussion."
                ),
                RecruitmentStage(
                    order = 4, title = "HR Interview",
                    status = StageStatus.UPCOMING,
                    description = "Behavioral and cultural fit discussion."
                )
            ),
            preparationResources = listOf(
                PrepResource(
                    title = "Striver's DSA Sheet", subtitle = "Core preparation",
                    icon = PrepResourceIcon.LINK, url = "https://takeuforward.org"
                )
            ),
            workCulture    = "Product company. Good exposure to automotive tech. Agile environment.",
            internDeadline = "N/A",
            fulltimeDeadline = "TBD",
            isLiveProcess  = false,
            lastUpdated    = now
        ),

        // ── LTIMindTree ──────────────────────────────────────────────────────
        CompanyDetail(
            name           = "LTIMindTree",
            tagline        = "IT Services & Consulting • Pan India",
            description    = "LTIMindTree recruits from LNCT. Aptitude and DSA rounds. " +
                    "Some students had virtual interviews with Boat (LTI client), others with LTI directly. " +
                    "Final round with actual LTI team. Process is notoriously slow — results delayed. " +
                    "Many students describe selection as luck-based due to poor communication.",
            website        = "https://www.ltimindtree.com/careers/",
            location       = "Pan India",
            employeeCount  = "80,000+",
            isHiring       = false,
            jobRole        = "Graduate Engineer Trainee",
            roleType       = RoleType.FULLTIME,
            packageLpa     = "6.5 LPA (est.)",
            level          = DifficultyLevel.MEDIUM,
            hiresCount     = 20,
            minCgpa        = "6.0",
            tenth          = "60",
            twelfth        = "60",
            allowedBacklogs = 0,
            allowedBranches = listOf("CSE", "IT", "EC", "EE"),
            eligibilityCriteria = EligibilityCriteria(
                minCgpa = "6.0", noActiveBacklogs = true,
                allowedBranches = listOf("CSE", "IT", "EC", "EE")
            ),
            recruitmentStages = listOf(
                RecruitmentStage(
                    order = 1, title = "Aptitude Test",
                    status = StageStatus.UPCOMING, durationMins = 60,
                    tags = listOf("Aptitude", "Reasoning"),
                    description = "Quantitative aptitude, logical reasoning, verbal."
                ),
                RecruitmentStage(
                    order = 2, title = "DSA / Technical Test",
                    status = StageStatus.UPCOMING, durationMins = 60,
                    tags = listOf("DSA", "Coding"),
                    description = "DSA coding round. Medium difficulty. Arrays, strings, trees."
                ),
                RecruitmentStage(
                    order = 3, title = "Virtual Interview (Client / LTI)",
                    status = StageStatus.UPCOMING, tags = listOf("Virtual", "Client Round"),
                    description = "Random allocation — some get Boat client, others LTI directly. Final round with actual LTI."
                ),
                RecruitmentStage(
                    order = 4, title = "HR / Final Interview",
                    status = StageStatus.UPCOMING,
                    description = "Final HR round. Results often delayed — follow up with placement cell."
                )
            ),
            preparationResources = listOf(
                PrepResource(
                    title = "Striver's DSA Sheet", subtitle = "Core DSA prep",
                    icon = PrepResourceIcon.LINK, url = "https://takeuforward.org"
                )
            ),
            workCulture    = "Large IT services company post-merger. Good project exposure. Slow hiring process.",
            internDeadline = "N/A",
            fulltimeDeadline = "TBD",
            isLiveProcess  = false,
            lastUpdated    = now
        ),

        // ── Tejas Networks ───────────────────────────────────────────────────
        CompanyDetail(
            name           = "Tejas Networks",
            tagline        = "Networking / Telecom Hardware • Bangalore",
            description    = "Tejas Networks (a Tata Group company) manufactures telecom equipment. " +
                    "Hires from LNCT for embedded systems, networking, and software roles. " +
                    "Good for students interested in hardware/networking/core engineering.",
            website        = "https://www.tejasnetworks.com/careers",
            location       = "Bangalore, India",
            employeeCount  = "1,000–5,000",
            isHiring       = false,
            jobRole        = "Junior Engineer / Software Engineer",
            roleType       = RoleType.FULLTIME,
            packageLpa     = "5–8 LPA (est.)",
            level          = DifficultyLevel.MEDIUM,
            hiresCount     = 10,
            minCgpa        = "6.5",
            tenth          = "60",
            twelfth        = "60",
            allowedBacklogs = 0,
            allowedBranches = listOf("CSE", "IT", "EC", "EE"),
            eligibilityCriteria = EligibilityCriteria(
                minCgpa = "6.5", noActiveBacklogs = true,
                allowedBranches = listOf("CSE", "IT", "EC", "EE")
            ),
            recruitmentStages = listOf(
                RecruitmentStage(
                    order = 1, title = "Aptitude Test",
                    status = StageStatus.UPCOMING, durationMins = 60,
                    tags = listOf("Aptitude", "Technical MCQ"),
                    description = "Aptitude + basic technical MCQs on Networking, OS, C/C++."
                ),
                RecruitmentStage(
                    order = 2, title = "Technical Interview",
                    status = StageStatus.UPCOMING,
                    description = "Networking concepts, embedded C, data structures, OS fundamentals."
                ),
                RecruitmentStage(
                    order = 3, title = "HR Interview",
                    status = StageStatus.UPCOMING,
                    description = "Behavioral questions and joining discussion."
                )
            ),
            preparationResources = listOf(
                PrepResource(
                    title = "Computer Networks — GFG", subtitle = "External Link",
                    icon = PrepResourceIcon.LINK,
                    url = "https://www.geeksforgeeks.org/computer-network-tutorials/"
                )
            ),
            workCulture    = "Core networking domain. Good for EC/EE students. Tata Group credibility.",
            internDeadline = "N/A",
            fulltimeDeadline = "TBD",
            isLiveProcess  = false,
            lastUpdated    = now
        ),

        // ── HSBC Bank ────────────────────────────────────────────────────────
        CompanyDetail(
            name           = "HSBC Bank",
            tagline        = "Banking & Financial Services • Technology | 6–10 LPA",
            description    = "HSBC Technology division recruits Software Engineers and Analysts who work on " +
                    "banking systems, fintech platforms, and data analytics.",
            website        = "https://www.hsbc.com/careers",
            location       = "Hyderabad / Pune / Bangalore, India",
            employeeCount  = "220,000+",
            isHiring       = false,
            jobRole        = "Software Engineer (Technology Division)",
            roleType       = RoleType.FULLTIME,
            packageLpa     = "6–10 LPA (est.)",
            level          = DifficultyLevel.MEDIUM,
            hiresCount     = 15,
            minCgpa        = "6.5",
            tenth          = "60",
            twelfth        = "60",
            allowedBacklogs = 0,
            allowedBranches = listOf("CSE", "IT", "EC"),
            eligibilityCriteria = EligibilityCriteria(
                minCgpa = "6.5", noActiveBacklogs = true,
                allowedBranches = listOf("CSE", "IT", "EC")
            ),
            recruitmentStages = listOf(
                RecruitmentStage(
                    order = 1, title = "Aptitude Test",
                    status = StageStatus.UPCOMING, platform = "HSBC Portal / HireVue", durationMins = 60,
                    tags = listOf("Aptitude", "Quant", "Banking Awareness"),
                    description = "Quantitative aptitude, logical reasoning, and basic banking/finance awareness."
                ),
                RecruitmentStage(
                    order = 2, title = "Technical Interview",
                    status = StageStatus.UPCOMING, tags = listOf("Tech", "Banking Domain"),
                    description = "CS fundamentals, SQL, basic DSA, and banking technology discussion."
                ),
                RecruitmentStage(
                    order = 3, title = "HR Interview",
                    status = StageStatus.UPCOMING,
                    description = "Behavioral questions, career goals, and compliance awareness."
                )
            ),
            preparationResources = listOf(
                PrepResource(
                    title = "SQL for Interviews", subtitle = "W3Schools / GFG",
                    icon = PrepResourceIcon.LINK, url = "https://www.w3schools.com/sql/"
                )
            ),
            workCulture    = "Global bank. Strong compliance culture. Good benefits and international exposure.",
            internDeadline = "N/A",
            fulltimeDeadline = "TBD",
            isLiveProcess  = false,
            lastUpdated    = now
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    //  Batch Reports  (companyId is the REAL Fairestore doc ID from addCompany())
    // ─────────────────────────────────────────────────────────────────────────

    /** Picks the correct seed batch reports by company display name. */
    fun batchReportSeedList(companyId: String, companyName: String, now: Long): List<BatchReport> {
        val key = companyName.lowercase()
        return when {
            key.contains("epam") -> listOf(
                BatchReport(batchYear = "2025", shortlistedCount = 90, selectedCount = 70,
                    questionsAsked = listOf("Reverse a linked list", "Find maximum subarray sum (Kadane's)", "String palindrome check", "OOP: Polymorphism vs Inheritance"),
                    otherInfo = "Drive 1 (July): 40 hired. Drive 2 (Feb): 30 hired. Internship 6 months ₹24k/mo → full-time 8.5 LPA. Smooth process."),
                BatchReport(batchYear = "2026", shortlistedCount = 85, selectedCount = 65,
                    questionsAsked = listOf("Binary search on rotated array", "Level order traversal", "Find duplicates in array", "Explain SOLID principles"),
                    otherInfo = "Two drives expected. Similar to 2025. Strong preference for Java/Python skills.")
            )
            key.contains("quantiphi") -> listOf(
                BatchReport(batchYear = "2025", shortlistedCount = 22, selectedCount = 8,
                    questionsAsked = listOf("Bias-variance tradeoff", "Linear regression from scratch", "SQL: 2nd highest salary", "Python: list comprehension vs generator"),
                    otherInfo = "Very selective. Drive ~30 Aug. Strong ML/Python essential. 8 students selected for intern + PPO."),
                BatchReport(batchYear = "2026", shortlistedCount = 18, selectedCount = 7,
                    questionsAsked = listOf("CNN architecture", "What is gradient descent?", "Pandas DataFrame operations", "Find missing number in 1-N"),
                    otherInfo = "Similar to 2025. ML fundamentals heavily tested. Intern at ₹25k/mo for 6 months.")
            )
            key.contains("infosys") && key.contains("sp") -> listOf(
                BatchReport(batchYear = "2025", shortlistedCount = 200, selectedCount = 15,
                    questionsAsked = listOf("LCS (Longest Common Subsequence)", "Dijkstra's algorithm", "N-Queens (backtracking)", "Matrix chain multiplication", "Segment tree range sum"),
                    otherInfo = "HackWithInfy OA ~July 12 — very hard CP. ~200 appeared. 15 selected. Intern at Mussoorie ₹30k/mo. Campus round at Indore was much easier (Striver Sheet)."),
                BatchReport(batchYear = "2026", shortlistedCount = 180, selectedCount = 12,
                    questionsAsked = listOf("0/1 Knapsack", "Topological sort", "Merge K sorted arrays", "Median of data stream"),
                    otherInfo = "Same HackWithInfy process. Codeforces + Striver Sheet recommended.")
            )
            key.contains("infosys") && key.contains("nov") -> listOf(
                BatchReport(batchYear = "2025", shortlistedCount = 30, selectedCount = 4,
                    questionsAsked = listOf("Binary tree to doubly linked list", "Word break (DP)", "LRU Cache", "System design: URL shortener"),
                    otherInfo = "November drive. Very few selected (<5). Direct on-campus interview. 11 LPA base. Medium-hard Striver Sheet."),
                BatchReport(batchYear = "2026", shortlistedCount = 25, selectedCount = 3,
                    questionsAsked = listOf("Minimum spanning tree (Prim/Kruskal)", "Clone graph", "Trapping rain water"),
                    otherInfo = "Expected late Nov 2026. Very competitive — only top performers shortlisted.")
            )
            key.contains("cognizant") -> listOf(
                BatchReport(batchYear = "2025", shortlistedCount = 350, selectedCount = 200,
                    questionsAsked = listOf("Reverse string without library", "Balanced parentheses", "Factorial using recursion", "Java: interface vs abstract class", "Python: mutable vs immutable"),
                    otherInfo = "Two drives: Digital Nurture (May-Sep) + Actual (Oct). ~200 selected. Stream-based tech round."),
                BatchReport(batchYear = "2026", shortlistedCount = 320, selectedCount = 180,
                    questionsAsked = listOf("Bubble sort implementation", "Largest element in array", "String to integer (atoi)", "OOP: what is encapsulation?"),
                    otherInfo = "Similar process expected. Prepare aptitude and chosen stream language.")
            )
            key.contains("capgemini") -> listOf(
                BatchReport(batchYear = "2025", shortlistedCount = 400, selectedCount = 250,
                    questionsAsked = listOf("What is polymorphism?", "SQL JOIN types", "What is DBMS normalization?", "Explain SDLC phases", "What is REST API?"),
                    otherInfo = "Mass hiring. ~250 selected. Aptitude is the main filter. Tech interview covers core CS."),
                BatchReport(batchYear = "2026", shortlistedCount = 380, selectedCount = 240,
                    questionsAsked = listOf("What is cloud computing?", "Explain OSI model", "What is deadlock in OS?", "Find prime numbers up to N"),
                    otherInfo = "Similar process. 4.25/5.75/7.5 LPA. Prepare AMCAT mock tests.")
            )
            key.contains("tcs") -> listOf(
                BatchReport(batchYear = "2025", shortlistedCount = 400, selectedCount = 250,
                    questionsAsked = listOf("Fibonacci series (recursion)", "Swap without temp variable", "Primary key vs foreign key", "Reverse array in-place", "Process vs thread"),
                    otherInfo = "Drive 1 (Oct): LNCT Main only. Drive 2 (Jan-Feb): Remaining. NQT covers aptitude + coding. Ninja 3.36, Digital 7, Prime 9+ LPA."),
                BatchReport(batchYear = "2026", shortlistedCount = 380, selectedCount = 240,
                    questionsAsked = listOf("2nd largest in array", "Check Armstrong number", "What is inheritance?", "Binary search implementation"),
                    otherInfo = "Same two-wave structure. Prepare NQT mocks on PrepInsta and TCS portal.")
            )
            key.contains("carwale") || key.contains("cartrade") -> listOf(
                BatchReport(batchYear = "2025", shortlistedCount = 30, selectedCount = 10,
                    questionsAsked = listOf("Implement LRU cache", "Two sum (HashMap)", "REST vs GraphQL", "OOP design: parking lot"),
                    otherInfo = "Product company drive. ~10 selected. Good tech process. Focus on problem-solving."),
                BatchReport(batchYear = "2026", shortlistedCount = 25, selectedCount = 8,
                    questionsAsked = listOf("Merge intervals", "Valid parentheses", "Design basic social feed"),
                    otherInfo = "CarWale prefers candidates with good coding fundamentals and project experience.")
            )
            key.contains("lti") || key.contains("mindtree") -> listOf(
                BatchReport(batchYear = "2025", shortlistedCount = 60, selectedCount = 20,
                    questionsAsked = listOf("Reverse a linked list", "Find loop in linked list", "Microservices architecture", "SQL: GROUP BY, HAVING"),
                    otherInfo = "Process was very slow — results took months. Some got Boat (client), others LTI directly. Many called it luck-based."),
                BatchReport(batchYear = "2026", shortlistedCount = 50, selectedCount = 18,
                    questionsAsked = listOf("Stack using queues", "Find all string permutations", "What is Spring Boot?"),
                    otherInfo = "Similar experience expected. Be patient. Follow up with placement cell.")
            )
            key.contains("tejas") -> listOf(
                BatchReport(batchYear = "2025", shortlistedCount = 25, selectedCount = 10,
                    questionsAsked = listOf("What is TCP/IP model?", "TCP vs UDP", "What is an embedded system?", "Pointers in C — example"),
                    otherInfo = "Niche networking/telecom company. Good for EC students. Interview heavily covers networking."),
                BatchReport(batchYear = "2026", shortlistedCount = 20, selectedCount = 8,
                    questionsAsked = listOf("OSI model — 7 layers", "Interrupt handling in embedded", "What is VLAN?"),
                    otherInfo = "Tata Group. Good brand. Primarily EC/EE/CSE roles.")
            )
            key.contains("hsbc") -> listOf(
                BatchReport(batchYear = "2025", shortlistedCount = 40, selectedCount = 15,
                    questionsAsked = listOf("ACID in databases", "SQL: salary > department average", "What is a forex transaction?", "REST API with example"),
                    otherInfo = "Banking tech roles. SQL and basic finance awareness important. Good fintech/banking domain exposure."),
                BatchReport(batchYear = "2026", shortlistedCount = 35, selectedCount = 12,
                    questionsAsked = listOf("What is KYC in banking?", "SQL: GROUP BY with HAVING", "Explain OAuth 2.0", "What is AML?"),
                    otherInfo = "HSBC values SQL skills and basic banking awareness. Hyderabad primary location.")
            )
            else -> emptyList()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Feedback  (companyId = real Firestore doc ID from addCompany())
    // ─────────────────────────────────────────────────────────────────────────

    fun feedbackSeedList(companyId: String, companyName: String, now: Long): List<FeedbackSubmission> {
        val key = companyName.lowercase()
        val approved = when {
            key.contains("epam") -> listOf(
                FeedbackSubmission(id = "${companyId}_fb_1", companyId = companyId,
                    userId = "user_001", userEmail = "rahul.s@lncte.in", userName = "Rahul S.",
                    batchYear = "2025", rating = 4, status = FeedbackStatus.APPROVED,
                    experience = "The process was well-organized. Aptitude was straightforward — IndiaBix practice was enough. Tech round had 2 easy DSA questions. Interview was friendly. Got the offer in a week!",
                    questionsEncountered = listOf("Reverse linked list", "Max subarray", "OOP concepts"),
                    submittedAt = now - 86400000L * 30, reviewedAt = now - 86400000L * 28, reviewedBy = "admin"),
                FeedbackSubmission(id = "${companyId}_fb_2", companyId = companyId,
                    userId = "user_002", userEmail = "priya.m@lncte.in", userName = "Priya M.",
                    batchYear = "2025", rating = 5, status = FeedbackStatus.APPROVED,
                    experience = "EPAM is great to start your career! The internship at Indore was well-structured with a real project. Converted to full-time 8.5 LPA. February drive had an easier process than July.",
                    questionsEncountered = listOf("Binary search", "String manipulation", "SOLID principles"),
                    submittedAt = now - 86400000L * 20, reviewedAt = now - 86400000L * 18, reviewedBy = "admin")
            )
            key.contains("quantiphi") -> listOf(
                FeedbackSubmission(id = "${companyId}_fb_1", companyId = companyId,
                    userId = "user_010", userEmail = "neha.v@lncte.in", userName = "Neha V.",
                    batchYear = "2025", rating = 5, status = FeedbackStatus.APPROVED,
                    experience = "Quantiphi is amazing for ML enthusiasts! Deep interview — explained ML project in detail, Python coding tested. Read bias-variance, regularization, train/test splits.",
                    questionsEncountered = listOf("Bias-variance tradeoff", "Linear regression scratch", "Pandas ops", "SQL 2nd highest salary"),
                    submittedAt = now - 86400000L * 45, reviewedAt = now - 86400000L * 43, reviewedBy = "admin"),
                FeedbackSubmission(id = "${companyId}_fb_2", companyId = companyId,
                    userId = "user_011", userEmail = "rohan.g@lncte.in", userName = "Rohan G.",
                    batchYear = "2025", rating = 3, status = FeedbackStatus.APPROVED,
                    experience = "Good company but very selective. Cleared aptitude but couldn't crack ML interview. Interviewer expected deep ML theory — just knowing sklearn is not enough. Practice from scratch.",
                    questionsEncountered = listOf("CNN architecture", "Gradient descent math", "Python generators"),
                    submittedAt = now - 86400000L * 40, reviewedAt = now - 86400000L * 38, reviewedBy = "admin")
            )
            key.contains("infosys") && key.contains("sp") -> listOf(
                FeedbackSubmission(id = "${companyId}_fb_1", companyId = companyId,
                    userId = "user_020", userEmail = "ankit.t@lncte.in", userName = "Ankit T.",
                    batchYear = "2025", rating = 4, status = FeedbackStatus.APPROVED,
                    experience = "HackWithInfy OA was brutally hard — solved only 1.5/3. Still shortlisted (partial marks count)! Campus interview at Indore much easier — Striver Sheet level. Got SP at 6.25 LPA. Mussoorie internship unforgettable!",
                    questionsEncountered = listOf("LCS", "Dijkstra shortest path", "Campus: binary tree height", "Two sum"),
                    submittedAt = now - 86400000L * 60, reviewedAt = now - 86400000L * 58, reviewedBy = "admin"),
                FeedbackSubmission(id = "${companyId}_fb_2", companyId = companyId,
                    userId = "user_021", userEmail = "sonal.j@lncte.in", userName = "Sonal J.",
                    batchYear = "2025", rating = 5, status = FeedbackStatus.APPROVED,
                    experience = "Got Digital Specialist at 9 LPA! Mussoorie internship was the best experience of college life. Focus heavily on advanced DSA for OA. Company treats you really well.",
                    questionsEncountered = listOf("N-Queens", "Matrix chain multiplication", "Segment tree"),
                    submittedAt = now - 86400000L * 55, reviewedAt = now - 86400000L * 53, reviewedBy = "admin")
            )
            key.contains("infosys") && key.contains("nov") -> listOf(
                FeedbackSubmission(id = "${companyId}_fb_1", companyId = companyId,
                    userId = "user_030", userEmail = "vikram.s@lncte.in", userName = "Vikram S.",
                    batchYear = "2025", rating = 4, status = FeedbackStatus.APPROVED,
                    experience = "Got 11 LPA in November drive! Assessment was medium-hard — practiced Striver's complete sheet. On-campus interview asked LRU cache and a DP problem. Very few seats so competition is high.",
                    questionsEncountered = listOf("LRU Cache", "Word break (DP)", "BT to DLL"),
                    submittedAt = now - 86400000L * 50, reviewedAt = now - 86400000L * 48, reviewedBy = "admin")
            )
            key.contains("cognizant") -> listOf(
                FeedbackSubmission(id = "${companyId}_fb_1", companyId = companyId,
                    userId = "user_040", userEmail = "divya.r@lncte.in", userName = "Divya R.",
                    batchYear = "2025", rating = 4, status = FeedbackStatus.APPROVED,
                    experience = "Got GenC Pro at 6.75 LPA! Chose Python stream. Tech round had 2 easy DSA questions and Python MCQs. Aptitude was straightforward. Very smooth process.",
                    questionsEncountered = listOf("Balanced parentheses", "Reverse string", "Python: mutable vs immutable"),
                    submittedAt = now - 86400000L * 35, reviewedAt = now - 86400000L * 33, reviewedBy = "admin"),
                FeedbackSubmission(id = "${companyId}_fb_2", companyId = companyId,
                    userId = "user_041", userEmail = "harsh.b@lncte.in", userName = "Harsh B.",
                    batchYear = "2025", rating = 3, status = FeedbackStatus.APPROVED,
                    experience = "Selected in GenC at 4 LPA. The Digital Nurture drive in May was just a pipeline — actual offer came in October. Process a bit slow but results came. Prepare chosen stream well (I chose Java).",
                    questionsEncountered = listOf("Factorial using recursion", "Java: interface vs abstract class"),
                    submittedAt = now - 86400000L * 25, reviewedAt = now - 86400000L * 23, reviewedBy = "admin")
            )
            key.contains("capgemini") -> listOf(
                FeedbackSubmission(id = "${companyId}_fb_1", companyId = companyId,
                    userId = "user_050", userEmail = "pooja.k@lncte.in", userName = "Pooja K.",
                    batchYear = "2025", rating = 4, status = FeedbackStatus.APPROVED,
                    experience = "Got 5.75 LPA Senior Analyst. Aptitude was the main filter — practice PrepInsta and IndiaBix. Tech interview covered DBMS, OS, OOP. HR was friendly. Good for freshers.",
                    questionsEncountered = listOf("SQL JOINs", "What is normalization?", "SDLC phases", "REST API"),
                    submittedAt = now - 86400000L * 40, reviewedAt = now - 86400000L * 38, reviewedBy = "admin"),
                FeedbackSubmission(id = "${companyId}_fb_2", companyId = companyId,
                    userId = "user_051", userEmail = "arjun.n@lncte.in", userName = "Arjun N.",
                    batchYear = "2025", rating = 3, status = FeedbackStatus.APPROVED,
                    experience = "Selected at 4.25 LPA. Good start. If you prepare aptitude and core CS you'll clear it. Onboarding took time but worked out.",
                    questionsEncountered = listOf("Polymorphism example", "Cloud computing basics", "OSI model"),
                    submittedAt = now - 86400000L * 30, reviewedAt = now - 86400000L * 28, reviewedBy = "admin")
            )
            key.contains("tcs") -> listOf(
                FeedbackSubmission(id = "${companyId}_fb_1", companyId = companyId,
                    userId = "user_060", userEmail = "kiran.p@lncte.in", userName = "Kiran P.",
                    batchYear = "2025", rating = 4, status = FeedbackStatus.APPROVED,
                    experience = "Cleared TCS Digital at 7 LPA! NQT was the key — practice on TCS portal itself. Aptitude + coding doable with consistent prep. Tech interview: OOP and simple SQL.",
                    questionsEncountered = listOf("Fibonacci recursion", "Array reverse", "Primary vs Foreign key", "Thread vs Process"),
                    submittedAt = now - 86400000L * 45, reviewedAt = now - 86400000L * 43, reviewedBy = "admin"),
                FeedbackSubmission(id = "${companyId}_fb_2", companyId = companyId,
                    userId = "user_061", userEmail = "meera.s@lncte.in", userName = "Meera S.",
                    batchYear = "2025", rating = 4, status = FeedbackStatus.APPROVED,
                    experience = "Got TCS Ninja at 3.36 LPA in Drive 1 (LNCT Main). NQT aptitude easy but coding had 2 medium questions. Prepare NQT mocks from PrepInsta.",
                    questionsEncountered = listOf("Swap without temp", "Binary search", "What is encapsulation?"),
                    submittedAt = now - 86400000L * 38, reviewedAt = now - 86400000L * 36, reviewedBy = "admin")
            )
            key.contains("carwale") || key.contains("cartrade") -> listOf(
                FeedbackSubmission(id = "${companyId}_fb_1", companyId = companyId,
                    userId = "user_070", userEmail = "dev.m@lncte.in", userName = "Dev M.",
                    batchYear = "2025", rating = 5, status = FeedbackStatus.APPROVED,
                    experience = "CarWale was my dream company! Product company with real impact. Interview challenging — LRU Cache design and REST vs GraphQL. Projects played a big role.",
                    questionsEncountered = listOf("LRU Cache", "Two sum with HashMap", "REST vs GraphQL", "Parking lot design"),
                    submittedAt = now - 86400000L * 55, reviewedAt = now - 86400000L * 53, reviewedBy = "admin")
            )
            key.contains("lti") || key.contains("mindtree") -> listOf(
                FeedbackSubmission(id = "${companyId}_fb_1", companyId = companyId,
                    userId = "user_080", userEmail = "saurav.d@lncte.in", userName = "Saurav D.",
                    batchYear = "2025", rating = 2, status = FeedbackStatus.APPROVED,
                    experience = "Process took almost 4 months from interview to offer letter. Very poor communication. Had interview with Boat (client) first, then LTI team. Offer came but uncertainty was stressful.",
                    questionsEncountered = listOf("Reverse linked list", "Loop in LL", "Microservices vs Monolith"),
                    submittedAt = now - 86400000L * 60, reviewedAt = now - 86400000L * 58, reviewedBy = "admin"),
                FeedbackSubmission(id = "${companyId}_fb_2", companyId = companyId,
                    userId = "user_081", userEmail = "anjali.v@lncte.in", userName = "Anjali V.",
                    batchYear = "2025", rating = 3, status = FeedbackStatus.APPROVED,
                    experience = "Got selected eventually! Wait was painful but the offer was decent. DSA round was medium. My friend and I cleared but got different client projects — it really does feel random.",
                    questionsEncountered = listOf("Stack using queues", "String permutations", "Spring Boot basics"),
                    submittedAt = now - 86400000L * 50, reviewedAt = now - 86400000L * 48, reviewedBy = "admin")
            )
            key.contains("tejas") -> listOf(
                FeedbackSubmission(id = "${companyId}_fb_1", companyId = companyId,
                    userId = "user_090", userEmail = "rohit.ec@lncte.in", userName = "Rohit (EC Dept)",
                    batchYear = "2025", rating = 4, status = FeedbackStatus.APPROVED,
                    experience = "Great for EC students! Interview heavily focused on networking — TCP/IP, protocols, embedded C. Not much DSA. If you know networking fundamentals you'll do well.",
                    questionsEncountered = listOf("TCP vs UDP", "OSI model", "Pointers in C", "Interrupt handling"),
                    submittedAt = now - 86400000L * 42, reviewedAt = now - 86400000L * 40, reviewedBy = "admin")
            )
            key.contains("hsbc") -> listOf(
                FeedbackSubmission(id = "${companyId}_fb_1", companyId = companyId,
                    userId = "user_100", userEmail = "tanvi.s@lncte.in", userName = "Tanvi S.",
                    batchYear = "2025", rating = 4, status = FeedbackStatus.APPROVED,
                    experience = "HSBC is great if you're interested in fintech! SQL was heavily tested — practice complex queries. Interviewer also asked basic banking: KYC, SWIFT. Good international exposure.",
                    questionsEncountered = listOf("SQL: salary > dept avg", "ACID properties", "What is KYC?", "REST API example"),
                    submittedAt = now - 86400000L * 35, reviewedAt = now - 86400000L * 33, reviewedBy = "admin")
            )
            else -> emptyList()
        }

        // PENDING feedback (stays in admin queue)
        val pending = when {
            key.contains("epam") -> listOf(
                FeedbackSubmission(id = "${companyId}_pending_1", companyId = companyId,
                    userId = "user_p01", userEmail = "nikhil.l@lncte.in", userName = "Nikhil L.",
                    batchYear = "2026", rating = 4, status = FeedbackStatus.PENDING,
                    experience = "Just attended the drive last week. Assessment was easy. Interview felt good — asked about internship project and basic DSA. Waiting for results.",
                    questionsEncountered = listOf("Find duplicate in array", "What is REST?"),
                    submittedAt = now - 86400000L * 2)
            )
            key.contains("cognizant") -> listOf(
                FeedbackSubmission(id = "${companyId}_pending_1", companyId = companyId,
                    userId = "user_p02", userEmail = "sakshi.r@lncte.in", userName = "Sakshi R.",
                    batchYear = "2026", rating = 5, status = FeedbackStatus.PENDING,
                    experience = "Just got selected in GenC Pro! Chose Python stream. Round 2 MCQs were tricky — Python decorators and list comprehension asked.",
                    questionsEncountered = listOf("Python decorators", "List comprehension", "Palindrome check"),
                    submittedAt = now - 86400000L * 1)
            )
            key.contains("tcs") -> listOf(
                FeedbackSubmission(id = "${companyId}_pending_1", companyId = companyId,
                    userId = "user_p03", userEmail = "lucky.m@lncte.in", userName = "Lucky M.",
                    batchYear = "2026", rating = 3, status = FeedbackStatus.PENDING,
                    experience = "Appeared for Drive 2 (Jan-Feb). Got TCS Ninja. NQT coding a bit tricky but aptitude was fine. HR was good. Waiting for joining date.",
                    questionsEncountered = listOf("2nd largest in array", "Armstrong number", "Binary search"),
                    submittedAt = now - 86400000L * 3)
            )
            key.contains("infosys") && key.contains("sp") -> listOf(
                FeedbackSubmission(id = "${companyId}_pending_1", companyId = companyId,
                    userId = "user_p04", userEmail = "ritesh.p@lncte.in", userName = "Ritesh P.",
                    batchYear = "2026", rating = 4, status = FeedbackStatus.PENDING,
                    experience = "HackWithInfy was insanely hard! Solved only 1 question fully. Got shortlisted somehow. Campus interview at Indore was smooth. Waiting for final result.",
                    questionsEncountered = listOf("Knapsack (HackWithInfy)", "Topological sort (HackWithInfy)", "Campus: Striver easy DP"),
                    submittedAt = now - 86400000L * 5)
            )
            else -> emptyList()
        }

        return approved + pending
    }
}
