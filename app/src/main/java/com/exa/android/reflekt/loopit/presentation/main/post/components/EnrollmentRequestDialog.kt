package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentRequestDialog(
    roles: List<ProjectRole>,
    initialSelectedRoleId: String? = null,
    userProfileSkills: String? = null,
    postTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (roleId: String, message: String) -> Unit,
    isLoading: Boolean = false
) {
    val openRoles = remember(roles) { roles.filter { it.isApplicationOpen() && !it.isFilled() } }
    
    // Auto-select logic
    var selectedRoleId by remember {
        mutableStateOf(
            initialSelectedRoleId?.takeIf { id -> openRoles.any { it.id == id } }
                ?: openRoles.firstOrNull { role ->
                    val userSkills = userProfileSkills?.lowercase() ?: ""
                    val roleTech = role.technology?.lowercase() ?: ""
                    val roleName = role.roleName.lowercase()
                    userSkills.isNotBlank() && (userSkills.contains(roleName) || (roleTech.isNotBlank() && userSkills.contains(roleTech)))
                }?.id
                ?: openRoles.firstOrNull()?.id
                ?: ""
        )
    }

    var message by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Light-mode compatible colors via MaterialTheme
    val sheetBg = MaterialTheme.colorScheme.surface
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val inputBg = MaterialTheme.colorScheme.background

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetState = sheetState,
        containerColor = sheetBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(borderColor)
            )
        },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            // ── Header ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Join \u2018${postTitle.take(28)}${if (postTitle.length > 28) "\u2026" else ""}\u2019",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The project lead will review your application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMuted
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                // Rocket icon circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.10f))
                        .border(1.dp, primaryColor.copy(alpha = 0.20f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.RocketLaunch,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Role Selection ──────────────────────────────────────────────────
            Text(
                text = "SELECT YOUR ROLE",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = textMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (openRoles.isEmpty()) {
                Text(
                    text = "No open roles available right now.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    openRoles.forEach { role ->
                        val isSelected = role.id == selectedRoleId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) primaryColor.copy(alpha = 0.08f) else surfaceColor)
                                .border(
                                    1.dp,
                                    if (isSelected) primaryColor.copy(alpha = 0.5f) else borderColor,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedRoleId = role.id }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (isSelected) primaryColor else Color.Transparent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = role.roleName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                                if (!role.technology.isNullOrBlank()) {
                                    Text(
                                        text = role.technology,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textMuted
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(primaryColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${role.remainingPositions()} left",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Message ─────────────────────────────────────────────────────────
            Text(
                text = "MESSAGE TO LEAD (OPTIONAL)",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = textMuted,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Tell the lead about your skills and why you'd be a great fit…",
                        color = textMuted.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                minLines = 4,
                maxLines = 6,
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor.copy(alpha = 0.60f),
                    unfocusedBorderColor = borderColor,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    cursorColor = primaryColor,
                    focusedContainerColor = inputBg,
                    unfocusedContainerColor = inputBg,
                    disabledContainerColor = inputBg
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            // ── Enroll Button ────────────────────────────────────────────────────
            Button(
                onClick = { if (selectedRoleId.isNotEmpty()) onSubmit(selectedRoleId, message) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && selectedRoleId.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = primaryColor.copy(alpha = 0.4f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Enroll Now",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
