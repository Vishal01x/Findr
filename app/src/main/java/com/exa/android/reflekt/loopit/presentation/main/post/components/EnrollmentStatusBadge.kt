package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.theme.Spacing

/**
 * Enhanced badge for displaying user's enrollment/registration status
 * Supports theme-aware colors and different sizes
 */
@Composable
fun EnrollmentStatusBadge(
    status: EnrollmentStatus,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val containerColor by animateColorAsState(
        targetValue = when (status) {
            EnrollmentStatus.ACCEPTED -> Color(0xFFE8F5E9) // Light Green
            EnrollmentStatus.PENDING -> Color(0xFFFFF8E1) // Light Yellow
            EnrollmentStatus.REJECTED -> Color(0xFFFFEBEE) // Light Red
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "containerColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when (status) {
            EnrollmentStatus.ACCEPTED -> Color(0xFF2E7D32) // Dark Green
            EnrollmentStatus.PENDING -> Color(0xFFF57F17) // Dark Orange
            EnrollmentStatus.REJECTED -> Color(0xFFC62828) // Dark Red
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "contentColor"
    )
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(100),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, contentColor.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = if (isLarge) 16.dp else 12.dp,
                vertical = if (isLarge) 6.dp else 4.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = status.displayName,
                style = if (isLarge) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
