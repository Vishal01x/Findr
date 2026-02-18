package com.exa.android.reflekt.loopit.presentation.main.post.screen

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post

/**
 * UI State for Post Detail Screen
 */
data class PostDetailState(
    val post: Post? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val enrollmentStatuses: Map<String, EnrollmentStatus> = emptyMap(), // roleId -> status
    val eventRegistrationStatus: EnrollmentStatus? = null,
    val isRegistered: Boolean = false,
    val toastMessage: String? = null,
    val showEnrollmentDialog: Boolean = false,
    val selectedRoleId: String? = null,
    val showEventRegDialog: Boolean = false,
    val isProcessingEnrollment: Boolean = false
) {
    val isAuthor: Boolean
        get() = post?.createdBy != null && post.createdBy.isNotEmpty()
        
    fun getEnrollmentStatus(roleId: String): EnrollmentStatus {
        return enrollmentStatuses[roleId] ?: EnrollmentStatus.NOT_ENROLLED
    }
}
