package com.exa.android.reflekt.loopit.presentation.main.placement.model

data class PlacedStudent(
    val id: String = "",
    val name: String = "",
    val company: String = "",
    val companyId: String = "",          // optional FK to placement_companies
    val role: String = "",
    val packageLpa: Double = 0.0,        // used for sorting; 0 = unparsed
    val packageDisplay: String = "",     // "12.5 LPA" or "₹45K/mo" (intern)
    val batchYear: Int = 0,              // e.g. 2024
    val type: String = "FTE",            // "FTE" | "Intern"
    val branch: String = "",
    val linkedinUrl: String = "",        // public — always shown
    val email: String = "",              // stripped for non-admins in repo layer
    val phone: String = "",              // admin-only, never shown to students
    val hasPhone: Boolean = false        // public boolean indicating if a phone number exists
)

data class BatchStatistics(
    val batchYear: Int = 0,
    val totalPlaced: Int = 0,
    val highestPackage: Double = 0.0,
    val averagePackage: Double = 0.0,
    val above4LpaCount: Int = 0,
    val above8LpaCount: Int = 0,
    val topCompany: String = "",
    val packageDistribution: Map<String, Int> = emptyMap(),
    val topStudents: List<PlacedStudent> = emptyList()
)
