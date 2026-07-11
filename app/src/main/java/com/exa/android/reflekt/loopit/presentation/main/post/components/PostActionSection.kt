package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
import com.exa.android.reflekt.loopit.theme.Spacing

private val SectionBg    = Color(0xFF1B2024)
private val SectionBorder = Color(0xFF3D484F)
private val CyanPrimary  = Color(0xFF71D2FF)
private val CyanAccent   = Color(0xFF13B6EC)
private val DarkSurface  = Color(0xFF252B2E)
private val TextMain     = Color(0xFFDEE3E8)
private val TextMuted    = Color(0xFFBCC8D0)
private val WarningAmber = Color(0xFFEAB308)
private val ErrorRed     = Color(0xFFFFB4AB)

@Composable
fun PostActionSection(
    post: Post,
    currentUserId: String,
    onEnrollInRole: (String) -> Unit,
    onRegisterForEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val postType = PostType.fromDisplayName(post.type)
    val hasAppliedToProject = post.hasUserRequested(currentUserId) || post.isUserEnrolled(currentUserId)

    when (postType) {
        PostType.PROJECT -> {
            if (post.projectRoles.isNotEmpty()) {
                ProjectRolesPreview(
                    roles = post.projectRoles.take(2),
                    totalRoles = post.projectRoles.size,
                    onEnrollClick = onEnrollInRole,
                    hasAppliedToProject = hasAppliedToProject,
                    modifier = modifier
                )
            }
        }
        PostType.EVENT -> {
            post.eventDetails?.let { eventDetails ->
                EventPreview(
                    eventDetails = eventDetails,
                    onRegisterClick = onRegisterForEvent,
                    modifier = modifier
                )
            }
        }
        else -> {}
    }
}

@Composable
private fun ProjectRolesPreview(
    roles: List<ProjectRole>,
    totalRoles: Int,
    onEnrollClick: (String) -> Unit,
    hasAppliedToProject: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Text(
            text = "Open Roles",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = CyanPrimary,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        roles.forEach { role ->
            RoleCard(
                role = role,
                onEnrollClick = { onEnrollClick(role.id) },
                hasAppliedToProject = hasAppliedToProject
            )
        }
        if (totalRoles > 2) {
            Text(
                text = "+${totalRoles - 2} more roles",
                style = MaterialTheme.typography.bodySmall,
                color = CyanPrimary,
                modifier = Modifier.padding(start = Spacing.small)
            )
        }
    }
}

@Composable
private fun RoleCard(
    role: ProjectRole,
    onEnrollClick: () -> Unit,
    hasAppliedToProject: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, SectionBorder.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = role.roleName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextMain
            )
            if (!role.technology.isNullOrBlank()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    role.technology.split(",").take(3).forEach { tech ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(CyanAccent.copy(alpha = 0.15f))
                                .border(1.dp, CyanPrimary.copy(alpha = 0.25f), RoundedCornerShape(9999.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tech.trim(),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Text(
                text = "${role.positionsFilled}/${role.positionsNeeded} filled",
                style = MaterialTheme.typography.labelSmall,
                color = if (role.positionsFilled >= role.positionsNeeded) ErrorRed else CyanPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (!role.isManuallyClosed && role.positionsFilled < role.positionsNeeded && !hasAppliedToProject) {
            Button(
                onClick = onEnrollClick,
                modifier = Modifier.padding(start = Spacing.small),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text(
                    text = "Enroll",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF003547),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun EventPreview(
    eventDetails: com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
            .clip(RoundedCornerShape(16.dp))
            .background(SectionBg)
            .border(1.dp, SectionBorder.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            eventDetails.eventTime?.let { time ->
                MetaChip(icon = Icons.Default.Schedule, text = time, tint = WarningAmber)
            }
            val locationText = if (eventDetails.isVirtual) "Virtual"
            else eventDetails.venue ?: eventDetails.location
            locationText?.let {
                MetaChip(
                    icon = if (eventDetails.isVirtual) Icons.Default.Videocam else Icons.Default.LocationOn,
                    text = it,
                    tint = TextMuted
                )
            }
            val remaining = eventDetails.remainingCapacity()
            when {
                eventDetails.isManuallyClosed -> MetaChip(Icons.Default.Block, "Closed", ErrorRed)
                eventDetails.isFull()         -> MetaChip(Icons.Default.Block, "Full", ErrorRed)
                remaining != null && remaining <= 5 -> MetaChip(Icons.Default.Warning, "$remaining left", WarningAmber)
            }
        }

        val canRegister = !eventDetails.isManuallyClosed && eventDetails.isRegistrationOpen()
        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = canRegister,
            colors = if (canRegister)
                ButtonDefaults.buttonColors(containerColor = CyanAccent)
            else
                ButtonDefaults.buttonColors(
                    disabledContainerColor = DarkSurface,
                    disabledContentColor = TextMuted
                )
        ) {
            if (canRegister) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = Color(0xFF003547)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Register for Event",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF003547)
                )
            } else {
                Text(
                    text = when {
                        eventDetails.isManuallyClosed -> "Registration Closed"
                        eventDetails.isFull()         -> "Event Full"
                        else                          -> "Registration Closed"
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MetaChip(
    icon: ImageVector,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(DarkSurface)
            .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(9999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.SemiBold)
    }
}
