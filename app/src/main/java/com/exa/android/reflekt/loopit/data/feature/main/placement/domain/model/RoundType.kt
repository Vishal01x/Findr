package com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model

enum class RoundType(val displayName: String) {
    // Assessment Rounds
    COMMUNICATION_ASSESSMENT("Communication Assessment"),
    TECHNICAL_ASSESSMENT("Technical Assessment"),
    APTITUDE("Aptitude Test"),
    CODING_ASSESSMENT("Coding Assessment"),
    GROUP_DISCUSSION("Group Discussion"),
    
    // Interview Rounds
    TECHNICAL_INTERVIEW("Technical Interview"),
    HR_INTERVIEW("HR Interview"),
    MANAGERIAL_INTERVIEW("Managerial Interview"),
    BEHAVIORAL_INTERVIEW("Behavioral Interview"),
    CASE_STUDY("Case Study"),
    
    // Other
    OTHER("Other");

    companion object {
        fun fromString(value: String): RoundType {
            return entries.find { it.name == value } ?: OTHER
        }

        fun getAssessmentTypes(): List<RoundType> {
            return listOf(
                COMMUNICATION_ASSESSMENT,
                TECHNICAL_ASSESSMENT,
                APTITUDE,
                CODING_ASSESSMENT,
                GROUP_DISCUSSION
            )
        }

        fun getInterviewTypes(): List<RoundType> {
            return listOf(
                TECHNICAL_INTERVIEW,
                HR_INTERVIEW,
                MANAGERIAL_INTERVIEW,
                BEHAVIORAL_INTERVIEW,
                CASE_STUDY
            )
        }
    }
}
